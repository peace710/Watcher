package me.peace.watcher.service.delegate.impl

import android.accessibilityservice.AccessibilityService
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import kotlinx.coroutines.*
import me.peace.watcher.R
import me.peace.watcher.service.delegate.ServiceDelegate
import me.peace.watcher.ui.Window
import me.peace.watcher.ui.impl.FloatWindowImpl
import me.peace.watcher.util.AccessibilityServiceUtils.focusViewInfo
import me.peace.watcher.util.MonitorUtils
import me.peace.watcher.util.Utils.cpuUsage
import me.peace.watcher.util.Utils.format

class AppMonitorServiceDelegate : ServiceDelegate,Window by FloatWindowImpl(),CoroutineScope by MainScope(){

    companion object {
        private const val TAG = "AppMonitorService"
    }

    private lateinit var layout: LinearLayoutCompat
    private lateinit var textView: AppCompatTextView
    private var focusView:String = ""
    private var cpuTime = 0L
    private var appCpuTime = 0L
    private var appPackageName = ""
    private var job:Job = Job()


    override fun onCreate(service: AccessibilityService) {
        super.onCreate(service)
        initCreate(service)
        job.cancel()
        job = job(service)
    }

    private fun job(service: AccessibilityService):Job = launch {
        while (true) {
            Log.d(TAG, "job() called")
            updateMonitorInfo(service)
            delay(1000)
        }
    }



    private fun initCreate(service: AccessibilityService){
        createWindowView(service)
    }

    private fun createWindowView(service: AccessibilityService){
        val params = with(service.resources) {
            val x = getDimensionPixelOffset(R.dimen.offset)
            val y = getDimensionPixelOffset(R.dimen.offset)
            val w = getDimensionPixelOffset(R.dimen.width)
            val h = getDimensionPixelOffset(R.dimen.height)
           createLayoutParams(x, y, w, h)
        }


        val layoutInflater = LayoutInflater.from(service.applicationContext)
        layout = layoutInflater.inflate(R.layout.layout_page_ui, null) as LinearLayoutCompat
        textView = layout.findViewById(R.id.page_text)
        layout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED)
        attachWindow(service.applicationContext,layout,params)
    }

    override fun onDestroy(service: AccessibilityService) {
        super.onDestroy(service)
        job.cancel()
        detachWindow(layout)
    }

    override fun onAccessibilityEvent(service: AccessibilityService, event: AccessibilityEvent) {
        super.onAccessibilityEvent(service, event)
        focusView = focusViewInfo(service,event)
        updateMonitorInfo(service)
    }

    private fun updateMonitorInfo(service: AccessibilityService){
        val page:Array<String>? = MonitorUtils.topPage(service)
        val pid = page?.get(0)?:""
        val packageName = page?.get(1)?:""
        val className = page?.get(2)?:""
        val currentMemory = page?.get(3)?:""
        val systemFreeMemory = page?.get(4)?:""
        val version = page?.get(5)?:""
        val ip = MonitorUtils.ip()
        resetCpuUsage(packageName)
        val cpu = cpuUsage(pid,cpuTime,appCpuTime){
                nowCpuTime,nowAppCpuTime -> run{
                    updateCpu(nowCpuTime,nowAppCpuTime)
                }
        }

        with(service) {
            var template = getString(R.string.monitor_info)
            var cpuContent = ""
            if (cpu != -1f) {
                cpuContent = String.format(getString(R.string.watcher_cpu), format(cpu))
            }
            var ipContent = ""
            if (!TextUtils.isEmpty(ip)) {
                ipContent = String.format(getString(R.string.watcher_ip), ip)
            }
            var focusViewContent = ""
            if (!TextUtils.isEmpty(focusView)) {
                focusViewContent = String.format(getString(R.string.monitor_focus_view), focusView)
            }
            val html = String.format(
                template,
                pid,
                version,
                packageName,
                className,
                "$currentMemory/$systemFreeMemory",
                cpuContent,
                ipContent,
                focusViewContent
            )
            Log.d(TAG, "updateMonitorInfo $html")
            textView.post {
                textView.text = Html.fromHtml(html)
            }

        }
    }

    private fun resetCpuUsage(currentPackage:String){
        if (currentPackage != appPackageName){
            updateCpu()
            appPackageName = currentPackage
        }
    }

    private fun updateCpu(cpu:Long = 0,app:Long = 0){
        cpuTime = cpu
        appCpuTime = app
    }
}