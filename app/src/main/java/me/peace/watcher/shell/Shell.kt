package me.peace.watcher.shell

import java.io.BufferedReader
import java.io.InputStreamReader


object Shell {
    private fun exec(cmd:String, callback:(String) -> Unit){
        Thread {
            val process = Runtime.getRuntime().exec(cmd)
            if (callback != null) {
                process.waitFor()
                val br = BufferedReader(InputStreamReader(process.inputStream))

                var str: String? = ""
                var log = StringBuilder()
                while (br.readLine().also { str = it } != null) {
                    log.append(str).append("\n")
                }
                callback.invoke(log.toString())
            }
        }.start()
    }
}