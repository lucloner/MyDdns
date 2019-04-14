package net.vicp.biggee.net.MyDdns

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.vicp.biggee.kotlin.Service
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), ServiceConnection {
    var serviceBinder: Service.Binder? = null

    /**
     * Called when a connection to the Service has been lost.  This typically
     * happens when the process hosting the service has crashed or been killed.
     * This does *not* remove the ServiceConnection itself -- this
     * binding to the service will remain active, and you will receive a call
     * to [.onServiceConnected] when the Service is next running.
     *
     * @param name The concrete component name of the service whose
     * connection has been lost.
     */
    override fun onServiceDisconnected(name: ComponentName?) {
        Service.addLog("service disconnected")
        serviceBinder = null
    }

    /**
     * Called when a connection to the Service has been established, with
     * the [android.os.IBinder] of the communication channel to the
     * Service.
     *
     *
     * **Note:** If the system has started to bind your
     * client app to a service, it's possible that your app will never receive
     * this callback. Your app won't receive a callback if there's an issue with
     * the service, such as the service crashing while being created.
     *
     * @param name The concrete component name of the service that has
     * been connected.
     *
     * @param service The IBinder of the Service's communication channel,
     * which you can now make calls on.
     */
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        serviceBinder = service as Service.Binder
        Service.addLog("server connected")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Service.addLog("app started")

        val l = findViewById<TextView>(R.id.logView)
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            runOnUiThread {
                l.append(
                    "${t.name}\n${e.stackTrace}\n"
                )
            }
        }

        val b = findViewById<View>(R.id.button) as Button
        val badb = findViewById<View>(R.id.adbbutton) as Button
        val bloop = findViewById<View>(R.id.loopbutton) as Button
        val d = findViewById<EditText>(R.id.ddns)
        val u = findViewById<EditText>(R.id.username)
        val p = findViewById<EditText>(R.id.password)
        runOnUiThread {
            d.setText(BuildConfig.myDdns)
            p.setText(BuildConfig.myPass)
        }

        val s = "http://ddns.oray.com/ph/update?hostname=${d.text}"
        val a = Credentials.basic(u.text.toString(), p.text.toString())
        val r = Request.Builder().apply {
            get()
            url(s)
            header("Authorization", a)
        }.build()

        val showLog = Runnable {
            runOnUiThread {
                if (l.lineCount > 30) {
                    l.text = ""
                }
                l.append(Service.log)
                Service.log.clear()
            }
        }

        val waitService = Runnable {
            Service.addLog("check service")
            while (serviceBinder == null) {
                Service.addLog("waiting service")
                Thread.sleep(1000)
            }
            showLog.run()
        }

        Service.runnable = Runnable {
            val c = OkHttpClient().newBuilder().apply {
                readTimeout(5, TimeUnit.SECONDS)
                cache(Cache(cacheDir, Int.MAX_VALUE.toLong()))
            }.build()
            val res = c.newCall(r).execute()
            Service.addLog(res.body()!!.string())
        }

        Thread {
            val bindIntent = Intent(this, Service::class.java)
            bindService(bindIntent, this, BIND_AUTO_CREATE)
            showLog.run()
        }.start()

        b.setOnClickListener {
            Service.addLog("button ddns clicked")
            waitService.run()
            serviceBinder ?: return@setOnClickListener
            serviceBinder!!.ddns()
            Thread.sleep(1000)
            showLog.run()
        }

        badb.setOnClickListener {
            Service.addLog("button adb clicked")
            waitService.run()
            serviceBinder ?: return@setOnClickListener
            serviceBinder!!.adbRemote()
            Thread.sleep(1000)
            showLog.run()
        }

        bloop.setOnClickListener {
            Service.addLog("button loop clicked")
            waitService.run()
            serviceBinder ?: return@setOnClickListener
            serviceBinder!!.schedule.scheduleAtFixedRate({
                Service.addLog("ddns schedule started")
                serviceBinder!!.pool.execute {
                    serviceBinder!!.ddns()
                    Thread.sleep(1000)
                    showLog.run()
                }
            }, 0, 1, TimeUnit.MINUTES)
        }

        Handler().postDelayed({
            try {
                b.performClick()
            } catch (e: Exception) {
                Service.addLog(e.localizedMessage)
            }
            try {
                badb.performClick()
            } catch (e: Exception) {
                Service.addLog(e.localizedMessage)
            }
            try {
                bloop.performClick()
            } catch (e: Exception) {
                Service.addLog(e.localizedMessage)
            }
            showLog.run()
        }, 1000)
    }
}
