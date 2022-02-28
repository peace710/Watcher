package me.peace.watcher.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.pm.IPackageStatsObserver
import android.content.pm.PackageStats
import android.graphics.PixelFormat
import android.graphics.Rect
import android.text.Html
import android.text.TextUtils
import android.text.format.Formatter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import me.peace.watcher.R
import me.peace.watcher.util.Utils
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class PageService: AccessibilityService() {
    private var initial:Boolean = false
    private lateinit var layout:LinearLayoutCompat
    private lateinit var textView:AppCompatTextView
    private lateinit var manager: WindowManager

    private lateinit var timer:Timer
    private lateinit var task:TimerTask
    private var focusView:String = ""
    private var cpuTime = 0L
    private var appCpuTime = 0L
    private var appPackageName = ""


    private val list = listOf<ServiceDelegate>(FocusFrameServiceDelegate())

    companion object{
        private const val FREQ = 1000L
        private const val PATH = "PATH"
        private const val ACTION = "me.peace.page"
        private const val STOP = "stop"

        fun start(context: Context?, path:String){
            val intent = Intent(context,PageService::class.java).apply {
                putExtra(PATH,path)
                action = ACTION
                `package`= context?.packageName
            }
            context?.startService(intent)
        }

        fun stop(context: Context?){
            val intent = Intent(context,PageService::class.java).apply {
                action = ACTION
                `package`= context?.packageName
                putExtra(STOP, STOP)
            }
            context?.startService(intent)
        }
    }

    private fun comparePackage(currentPackage:String){
        if (currentPackage != appPackageName){
            updateCpu()
            appPackageName = currentPackage
        }
    }

    private fun updateCpu(cpu:Long = 0,app:Long = 0){
        cpuTime = cpu
        appCpuTime =  app
    }

    private fun cpu(pid:String):Float{
        val nowCpuTime = Utils.cpuTime()
        val nowAppCpuTime = Utils.appCpuTime(pid)
        var usage = -1f
        if (cpuTime != 0L && appCpuTime != 0L && cpuTime != nowCpuTime){
            usage =  100f * (nowAppCpuTime - appCpuTime) / (nowCpuTime - cpuTime)
        }
        updateCpu(nowCpuTime,nowAppCpuTime)
        return usage
    }

    override fun onCreate() {
        super.onCreate()
        list.forEach { it.onCreate(this) }
        if (!initial){
            init()
            initial = true
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        list.forEach { it.onServiceConnected(this) }
    }

    private fun init(){
        attachUi()
        initTimer()
    }

    private fun initTimer(){
        timer = Timer()
        task = PageTask()
        timer.schedule(task,FREQ,FREQ)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (initial){
            update()
        }
        stopOrNot(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { list.filter { it.isEnable() }.forEach { it.onAccessibilityEvent(this, event) } }

        val child = rootInActiveWindow?.findFocus(FOCUS_INPUT)
        focusView = if (child == null) {
            ""
        }else {
            var rect = Rect()
            child.getBoundsInScreen(rect)
            "${child.viewIdResourceName} - $rect"
        }
        update()
    }

    override fun onInterrupt() {
        list.forEach { it.onInterrupt(this) }
    }

    private fun stopOrNot(intent: Intent?){
        val stop = intent?.getStringExtra(STOP)
        if (stop == STOP){
            detachUi()
            stopSelf()
        }
    }

    private fun update(){
        val page:Array<String>? = Utils.topPage(this)
        val pid = page?.get(0)?:""
        val packageName = page?.get(1)?:""
        val className = page?.get(2)?:""
        val currentMemory = page?.get(3)?:""
        val systemFreeMemory = page?.get(4)?:""
        val version = page?.get(5)?:""
        val ip = Utils.ip()
        comparePackage(packageName)
        val cpu = cpu(pid)
        size(packageName){
            val cacheSize = it?.cacheSize?:0
            val dataSize = it?.dataSize?:0
            val codeSize = it?.codeSize?:0

            textView.post {
                var template = getString(R.string.watcher_info)
                var cpuContent = ""
                if (cpu != -1f){
                    cpuContent = String.format(getString(R.string.watcher_cpu),format(cpu))
                }
                var ipContent = ""
                if (!TextUtils.isEmpty(ip)){
                    ipContent = String.format(getString(R.string.watcher_ip),ip)
                }
                var focusViewContent = ""
                if (!TextUtils.isEmpty(focusView)){
                    focusViewContent = String.format(getString(R.string.watcher_focus_view),focusView)
                }
                val html = String.format(template,pid,version,packageName,className,"$currentMemory/$systemFreeMemory",cpuContent,ipContent,focusViewContent)
                textView.text = Html.fromHtml(html)
            }
        }
    }

    private fun attachUi(){
        manager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams.format = PixelFormat.RGBA_8888
        layoutParams.gravity = Gravity.LEFT or Gravity.TOP
        with(resources) {
            layoutParams.x = getDimensionPixelOffset(R.dimen.offset)
            layoutParams.y = getDimensionPixelOffset(R.dimen.offset)
            layoutParams.width = getDimensionPixelOffset(R.dimen.width)
            layoutParams.height = getDimensionPixelOffset(R.dimen.height)
        }

        val layoutInflater = LayoutInflater.from(applicationContext)
        layout = layoutInflater.inflate(R.layout.layout_page_ui, null) as LinearLayoutCompat
        textView = layout.findViewById(R.id.page_text)
        layout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED)

        manager.addView(layout,layoutParams)
    }

    private fun detachUi(){
        manager.removeView(layout)
    }

    inner class PageTask: TimerTask() {
        override fun run() {
            update()
        }
    }

    private fun size(packageName:String,callback:(PackageStats?) -> Unit){
        val getPackageSizeInfo = Class.forName("android.content.pm.PackageManager").getDeclaredMethod(
            "getPackageSizeInfo", Class.forName("java.lang.String"),
            Class.forName("android.content.pm.IPackageStatsObserver")
        )
        getPackageSizeInfo.invoke(packageManager,packageName,object: IPackageStatsObserver.Stub() {
            override fun onGetStatsCompleted(pStats: PackageStats?, succeeded: Boolean) {
                callback?.invoke(pStats)
            }
        })

    }

    private fun formatSize(size:Long):String{
        return Formatter.formatFileSize(this,size)
    }

    private fun format(num:Float):String{
        val format = DecimalFormat("0.##")
        format.roundingMode = RoundingMode.FLOOR
        return format.format(num)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        list.forEach { it.onUnbind(this, intent) }

        return super.onUnbind(intent)
    }
}