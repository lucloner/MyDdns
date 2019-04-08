package net.vicp.biggee.kotlin

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object Shell {
    val log = StringBuilder()

    /**
     * 执行shell指令
     * @param strings 指令集
     * @return 指令集是否执行成功
     */
    fun exeCmdByRoot(vararg strings: String): Boolean {
        try {
            val su = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(su.outputStream)
            val mReader = BufferedReader(InputStreamReader(su.inputStream))

            for (s in strings) {
                outputStream.writeBytes(s + "\n")
                outputStream.flush()
            }
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            su.waitFor()
            outputStream.close()

            val mRespBuff = StringBuffer("${System.currentTimeMillis()}:\n")
            val buff = CharArray(1024)
            var ch = mReader.read(buff)
            while (ch != -1) {
                mRespBuff.append(buff, 0, ch)
                ch = mReader.read(buff)
            }
            mReader.close()
            log.append(mRespBuff)
            Log.v("Shell", mRespBuff.toString())
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            log.append(e.toString())
            return false
        }

    }
}