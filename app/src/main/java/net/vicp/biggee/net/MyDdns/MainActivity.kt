package net.vicp.biggee.net.MyDdns

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.EventListener
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val b = findViewById<View>(R.id.button) as Button
        val d = findViewById<EditText>(R.id.ddns)
        val u = findViewById<EditText>(R.id.username)
        val p = findViewById<EditText>(R.id.password)
        val l = findViewById<TextView>(R.id.logView)
        val s = "http://${u.text}:${p.text}@ddns.oray.com/ph/update?hostname=${d.text}"
        val e = Base64.getEncoder().encodeToString("${u.text}:${p.text}".toByteArray())
        val url = URL(s)
        val param="hostname=${d.text}"
        val a=Credentials.basic(u.text.toString(),p.text.toString())
//        Comparator<String> { obj, anotherString -> obj.compareTo(anotherString) }

        val r=Request.Builder().apply {
            get()
            url("http://ddns.oray.com/ph/update?hostname=${d.text}")
            header("Authorization",a)
        }.build()

        b.setOnClickListener {
            Thread {
                val c=OkHttpClient().newBuilder().apply {
                    readTimeout(5,TimeUnit.SECONDS)
                    cache(Cache(cacheDir,Int.MAX_VALUE.toLong()))
                    }.build()

                val res=c.newCall(r).execute()

                runOnUiThread{
                    l.text="${System.currentTimeMillis()}\n"
                    l.append(res.body()!!.string())
                }
            }.start()
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
