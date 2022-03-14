package me.peace.watcher.util

import java.math.RoundingMode
import java.text.DecimalFormat

object Utils {
     fun cpuUsage(pid:String, cpuTime:Long, appCpuTime:Long, function: (Long,Long) -> Unit):Float{
        val nowCpuTime = MonitorUtils.cpuTime()
        val nowAppCpuTime = MonitorUtils.appCpuTime(pid)
        var usage = -1f
        if (cpuTime != 0L && appCpuTime != 0L && cpuTime != nowCpuTime){
            usage =  100f * (nowAppCpuTime - appCpuTime) / (nowCpuTime - cpuTime)
        }
        function.invoke(nowCpuTime,nowAppCpuTime)
        return usage
    }

    fun format(num:Float):String{
        val format = DecimalFormat("0.##")
        format.roundingMode = RoundingMode.FLOOR
        return format.format(num)
    }
}