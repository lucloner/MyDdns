package net.vicp.biggee.net.MyDdns

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val b = findViewById<View>(R.id.button) as Button
        val d = findViewById<EditText>(R.id.ddns)
        val u = findViewById<EditText>(R.id.username)
        val p = findViewById<EditText>(R.id.password)
        runOnUiThread {
            p.setText(BuildConfig.myPass)
        }
        val l = findViewById<TextView>(R.id.logView)
        val s = "http://ddns.oray.com/ph/update?hostname=${d.text}"
        val a=Credentials.basic(u.text.toString(),p.text.toString())
        val r=Request.Builder().apply {
            get()
            url(s)
            header("Authorization",a)
        }.build()

        b.setOnClickListener {
            b.isEnabled = false
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
                val c=OkHttpClient().newBuilder().apply {
                    readTimeout(5,TimeUnit.SECONDS)
                    cache(Cache(cacheDir,Int.MAX_VALUE.toLong()))
                }.build()
                val res=c.newCall(r).execute()
                runOnUiThread{
                    l.text = "${SimpleDateFormat.getDateTimeInstance().format(Date())}\n"
                    l.append(res.body()!!.string())
                }
            }, 0, 5, TimeUnit.MINUTES)
        }

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            runOnUiThread{
                l.append(
                    "${t.name}\n${e.stackTrace}\n"
                )
            }
        }
    }
}
