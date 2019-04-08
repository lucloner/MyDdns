package net.vicp.biggee.kotlin

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.util.concurrent.ArrayBlockingQueue
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

    class Binder : android.os.Binder(), ThreadFactory {
        val threadlist = ArrayList<Thread>()
        val pool = ThreadPoolExecutor(
            1,
            1,
            1,
            TimeUnit.DAYS,
            ArrayBlockingQueue<Runnable>(2),
            ThreadPoolExecutor.AbortPolicy()
        )

        fun adbRemote() {
            pool.execute(adbRunnable)
        }

        fun ddns() {
            if (::runnable.isLateinit) {
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
            threadlist.iterator().forEach {
                if (!it.isAlive) {
                    threadlist.remove(it)
                }
            }
            r ?: return Thread()
            val t = Thread(r)
            threadlist.add(t)
            return t
        }
    }

    companion object {
        lateinit var runnable: Runnable
        val adbRunnable = Runnable {
            Shell.exeCmdByRoot("setprop service.adb.tcp.port 5555")
            Shell.exeCmdByRoot("stop adbd")
            Shell.exeCmdByRoot("start adbd")
            adbRestarted = true
        }
        var adbRestarted = false
    }
}