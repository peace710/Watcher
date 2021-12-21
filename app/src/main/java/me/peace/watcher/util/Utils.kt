package me.peace.watcher.util

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.text.format.Formatter


object Utils {
    fun topPage(context: Context?):Array<String>?{
        val top = top(context)
        val packageName:String=top?.packageName?:""
        val className:String=top?.className?:""
        val currentMemory = appMemory(context,packageName)
        val systemFreeMemory = systemFreeMemory(context)
        return arrayOf<String>(packageName,className,currentMemory,systemFreeMemory)
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

    private fun appMemory(context:Context?, packageName:String):String{
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses
        var memory = ""
        runningAppProcesses.filter {
            it.processName == packageName
        }.forEach{
            val processMemoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(it.pid))
            memory = Formatter.formatFileSize(context,processMemoryInfo[0].dalvikPrivateDirty.toLong() * 1024)
        }
        return memory
    }

    private fun systemFreeMemory(context: Context?):String{
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return Formatter.formatFileSize(context,memoryInfo.availMem)
    }
}