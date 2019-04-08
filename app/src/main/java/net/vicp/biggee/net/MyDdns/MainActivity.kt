package net.vicp.biggee.net.MyDdns

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.vicp.biggee.kotlin.Service
import net.vicp.biggee.kotlin.Shell
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), ServiceConnection {
    lateinit var serviceBinder: Service.Binder

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val l = findViewById<TextView>(R.id.logView)
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            runOnUiThread {
                l.append(
                    "${t.name}\n${e.stackTrace}\n"
                )
            }
        }

        val b = findViewById<View>(R.id.button) as Button
        val d = findViewById<EditText>(R.id.ddns)
        runOnUiThread {
            d.setText("biggee.vicp.net")
        }
        val u = findViewById<EditText>(R.id.username)
        val p = findViewById<EditText>(R.id.password)
        runOnUiThread {
            p.setText(BuildConfig.myPass)
        }

        val s = "http://ddns.oray.com/ph/update?hostname=${d.text}"
        val a=Credentials.basic(u.text.toString(),p.text.toString())
        val r=Request.Builder().apply {
            get()
            url(s)
            header("Authorization",a)
        }.build()

        Service.runnable = Runnable {
            val c = OkHttpClient().newBuilder().apply {
                readTimeout(5, TimeUnit.SECONDS)
                cache(Cache(cacheDir, Int.MAX_VALUE.toLong()))
            }.build()
            val res = c.newCall(r).execute()
            runOnUiThread {
                l.append("${SimpleDateFormat.getDateTimeInstance().format(Date())}\n")
                l.append(res.body()!!.string())
            }
        }

        Thread {
            val bindIntent = Intent(this, Service::class.java)
            bindService(bindIntent, this, BIND_AUTO_CREATE)
            startService(bindIntent)
        }.start()

        b.setOnClickListener {
            b.isEnabled = false
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
                if (::serviceBinder.isLateinit) {
                    serviceBinder.ddns()
                }
            }, 0, 1, TimeUnit.MINUTES)
        }

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            if (::serviceBinder.isLateinit) {
                serviceBinder.ddns()
                if (!Service.adbRestarted) {
                    serviceBinder.adbRemote()
                }
            }
            runOnUiThread {
                l.append(Shell.log)
                Shell.log.clear()
            }
        }, 0, 1, TimeUnit.MINUTES)
    }
}
