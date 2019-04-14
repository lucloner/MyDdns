package net.vicp.biggee.kotlin

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object Shell {

    /**
     * 执行shell指令
     * @param strings 指令集
     * @return 指令集是否执行成功
     */
    fun exeCmdByRoot(vararg strings: String, isRoot: Boolean = true): Boolean {
        try {
            val su = if (isRoot) Runtime.getRuntime().exec("su") else Runtime.getRuntime().exec("sh")
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
            Service.addLog("shell return:$mRespBuff")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Service.addLog("shell exception:$e")
            return false
        }
    }
}