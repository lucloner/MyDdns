package net.vicp.biggee.kotlin

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.text.DateFormat
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class Service : Service() {
    val binder by lazy { Binder() }

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * [android.os.IBinder] is usually for a complex interface
     * that has been [described using
 * aidl]({@docRoot}guide/components/aidl.html).
     *
     *
     * *Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process*.  More information about the main thread can be found in
     * [Processes and
 * Threads]({@docRoot}guide/topics/fundamentals/processes-and-threads.html).
     *
     * @param intent The Intent that was used to bind to this service,
     * as given to [ Context.bindService][android.content.Context.bindService].  Note that any extras that were included with
     * the Intent at that point will *not* be seen here.
     *
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        addLog("create service:")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        addLog("service start:")
        return super.onStartCommand(intent, flags, startId)
    }

    class Binder : android.os.Binder(), ThreadFactory {
        val queueList = LinkedBlockingQueue<Runnable>()
        val pool = ThreadPoolExecutor(
            1,
            1,
            1,
            TimeUnit.DAYS,
            queueList,
            this,
            ThreadPoolExecutor.AbortPolicy()
        )

        fun adbRemote() {
            addLog("adb")
            pool.execute(adbRunnable)
        }

        fun ddns() {
            addLog("ddns")
            if (::runnable.isLateinit) {
                addLog("ddns inited")
                pool.execute(runnable)
            }
        }

        /**
         * Constructs a new `Thread`.  Implementations may also initialize
         * priority, name, daemon status, `ThreadGroup`, etc.
         *
         * @param r a runnable to be executed by new thread instance
         * @return constructed thread, or `null` if the request to
         * create a thread is rejected
         */
        override fun newThread(r: Runnable?): Thread {
            while (pool.activeCount > 1) {
                Thread.sleep(10000)
            }
            r ?: return Thread()
            val t = Thread(r)
            return t
        }
    }

    companion object {
        val log = StringBuilder()
        lateinit var runnable: Runnable
        val adbRunnable = Runnable {
            addLog("setprop:${Shell.exeCmdByRoot("setprop service.adb.tcp.port 5555")}")
            addLog("stopadbd:${Shell.exeCmdByRoot("stop adbd")}")
            addLog("startadbd:${Shell.exeCmdByRoot("start adbd")}")
            adbRestarted = true
        }
        var adbRestarted = false
        fun getTimeStemp() = DateFormat.getDateTimeInstance().format(Date(System.currentTimeMillis()))
        fun addLog(txt: String) {
            log.append("${getTimeStemp()}:\t$txt\n")
        }
    }
}