package me.peace.watcher.util

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context


object Utils {
    fun topPage(context: Context?):Array<String>?{
        val top = top(context)
        val packageName:String=top?.packageName?:""
        val className:String=top?.className?:""
        return arrayOf<String>(packageName,className)
    }

    private fun top(context: Context?): ComponentName? {
        kotlin.runCatching {
            val systemService = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningTasks = systemService.getRunningTasks(1)
            if (runningTasks.isNotEmpty()){
                return runningTasks[0]?.topActivity
            }
            return null
        }.onSuccess {
            return it
        }.onFailure {
            it.printStackTrace()
        }
        return null
    }
}