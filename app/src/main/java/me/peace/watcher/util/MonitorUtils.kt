package me.peace.watcher.util

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.text.format.Formatter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


object MonitorUtils {
    fun topPage(context: Context?): Array<String> {
        val top = top(context)
        val packageName:String=top?.packageName?:""
        val className:String=top?.className?:""
        val pid = pid(context,packageName)
        val currentMemory = appMemory(context,pid)
        val systemFreeMemory = systemFreeMemory(context)
        val version = appVersion(context,packageName)
        return arrayOf(pid.toString(),packageName,className,currentMemory,systemFreeMemory,version)
    }

    private fun top(context: Context?): ComponentName? {
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.getRunningTasks(1)
        if (runningTasks.isNotEmpty()){
            return runningTasks[0]?.topActivity
        }
        return null
    }

    private fun pid(context:Context?, packageName:String):Int{
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses
        var pid = -1
        runningAppProcesses.filter {
            it.processName == packageName
        }.forEach{
           pid = it.pid
        }
        return pid
    }

    private fun appMemory(context:Context?, pid:Int):String{
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processMemoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(pid))
        return Formatter.formatFileSize(context,processMemoryInfo[0].totalPss.toLong() * 1024)
    }

    private fun systemFreeMemory(context: Context?):String{
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return Formatter.formatFileSize(context,memoryInfo.availMem)
    }

    private fun appVersion(context: Context?,packageName:String):String{
        val packageManager = context?.packageManager
        val packageInfo = packageManager?.getPackageInfo(packageName, 0)
        return packageInfo?.versionName?:""
    }

    fun ip(): String {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val element: NetworkInterface = en.nextElement()
                val ip: Enumeration<InetAddress> = element.inetAddresses
                while (ip.hasMoreElements()) {
                    val inetAddress: InetAddress = ip.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress().toString()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return ""
    }

     fun cpuTime(): Long {
        var cpu: Array<String>? = null
        try {
            val reader =
                BufferedReader(InputStreamReader(FileInputStream("/proc/stat")), 1000)
            val load: String = reader.readLine()
            reader.close()
            cpu = load.split(" ").toTypedArray()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return cpu!![2].toLong() + cpu[3].toLong() + cpu[4]
            .toLong() + cpu[6].toLong() + cpu[5].toLong() + cpu[7]
            .toLong() + cpu[8].toLong() + cpu[9].toLong() + cpu[10]
            .toLong()
         return 1
    }

    fun appCpuTime(pid:String): Long {
        var cpu: Array<String>? = null
        try {
            val reader = BufferedReader(
                InputStreamReader(
                    FileInputStream("/proc/$pid/stat")
                ), 1000
            )
            val load: String = reader.readLine()
            reader.close()
            cpu = load.split(" ").toTypedArray()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return cpu!![13].toLong() + cpu[14].toLong() + cpu[15]
            .toLong() + cpu[16].toLong()
        return 1
    }
}