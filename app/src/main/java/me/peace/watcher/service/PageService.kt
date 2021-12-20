package me.peace.watcher.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatTextView
import me.peace.watcher.R
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import me.peace.watcher.shell.Shell
import me.peace.watcher.util.Utils
import java.util.*

class PageService: Service() {
    private var initial:Boolean = false
    private lateinit var layout:LinearLayoutCompat
    private lateinit var textView:AppCompatTextView
    private lateinit var manager: WindowManager

    private lateinit var timer:Timer
    private lateinit var task:TimerTask

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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (!initial){
            init()
            initial = true
        }
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

    private fun stopOrNot(intent: Intent?){
        val stop = intent?.getStringExtra(STOP)
        if (stop == STOP){
            detachUi()
            stopSelf()
        }
    }

    private fun update(){
        val page = Utils.topPage(this)
        textView?.post {
            textView.text = page
        }
    }

    private fun attachUi(){
        manager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams.format = PixelFormat.RGBA_8888
        layoutParams.gravity = Gravity.LEFT or Gravity.TOP
        layoutParams.x = 50
        layoutParams.y = 50
        layoutParams.width = 420
        layoutParams.height = 180

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
}