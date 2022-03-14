package me.peace.watcher.ui.impl

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import me.peace.watcher.ui.Window


class FloatWindowImpl : Window {

    companion object{
        private const val TAG = "FloatWindowImpl"
    }

    private var manager:WindowManager? = null

    private fun init(context:Context){
        if (manager == null) {
            manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
    }

    override fun attachWindow(context:Context,view:View,params: WindowManager.LayoutParams) {
        init(context)
        manager?.addView(view,params)
    }

    override fun detachWindow(view:View) {
        manager?.removeView(view)
    }

    override fun createLayoutParams(x: Int, y: Int, w: Int, h: Int):WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        params.type = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.format = PixelFormat.RGBA_8888
        params.gravity = Gravity.RIGHT or Gravity.TOP
        params.x = x
        params.y = y
        params.width = w
        params.height = h
        return params
    }

    override fun updateLocation(view: View,x: Int, y: Int, w: Int, h: Int){
        val params = WindowManager.LayoutParams()
        params.type = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.format = PixelFormat.RGBA_8888
        params.gravity = Gravity.LEFT or Gravity.TOP
        params.x = x
        params.y = y
        params.width = w
        params.height = h
        Log.d(TAG, "updateLocation() called with: view = $view, x = $x, y = $y, w = $w, h = $h")
        manager?.updateViewLayout(view, params)
    }
}