package net.vicp.biggee.kotlin

import java.io.DataOutputStream

object Shell {

    /**
     * 执行shell指令
     * @param strings 指令集
     * @return 指令集是否执行成功
     */
    fun exeCmdByRoot(vararg strings: String): Boolean {
        try {
            val su = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(su.outputStream)

            for (s in strings) {
                outputStream.writeBytes(s + "\n")
                outputStream.flush()
            }
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            su.waitFor()
            outputStream.close()
            return true
        } catch (e: Exception) {
            return false
        }

    }
}