package me.peace.watcher.service.delegate.impl

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import me.peace.watcher.R
import me.peace.watcher.service.delegate.ServiceDelegate
import me.peace.watcher.ui.Window
import me.peace.watcher.ui.impl.FloatWindowImpl

class FocusViewServiceDelegate : ServiceDelegate, Window by FloatWindowImpl() {
    companion object{
        private const val ATTACH = "attach"
        private const val FOCUS_FRAME_CHANGED_ACTION = "me.peace.focus.frame.enable.changed"
        private const val TAG = "FocusViewService"
    }

    private lateinit var service: AccessibilityService
    private lateinit var view: View
    private var enableFocusViewFrame:Boolean = true
    private lateinit var receiver: BroadcastReceiver
    private var currentRect = Rect()

    override fun onCreate(service: AccessibilityService) {
        super.onCreate(service)
        initCreate(service)
        updateLocation(service)
        register()
    }

    private fun initCreate(service: AccessibilityService){
        createWindowView(service)
    }

    private fun createWindowView(service: AccessibilityService){
        this.service = service
        view = View(service)
        view.setBackgroundResource(R.drawable.focus_view_frame)
        attach(service)

    }

    private fun attach(service: AccessibilityService){
        if (view != null && view.tag != ATTACH) {
            view.tag = ATTACH
            attachWindow(service, view, createLayoutParams(0, 0, 0, 0))
        }
    }

    private fun detach(){
        if (view != null && view.tag == ATTACH) {
            view.tag = ""
            detachWindow(view)
        }
    }

    override fun onDestroy(service: AccessibilityService) {
        super.onDestroy(service)
        unregister()
        detach()
    }

    private fun updateLocation(service: AccessibilityService){
        if (!::view.isInitialized || !view.isAttachedToWindow ) return
        val rect = Rect()
        service.rootInActiveWindow.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)?.getBoundsInScreen(rect)
        if (rect.isEmpty || currentRect == rect) return
        currentRect = rect
        updateLocation(view,rect.left,rect.top,rect.width(),rect.height())
    }

    override fun onAccessibilityEvent(service: AccessibilityService, event: AccessibilityEvent) {
        super.onAccessibilityEvent(service, event)
        updateLocation(service)
    }

    override fun isEnable(): Boolean = enableFocusViewFrame

    inner class FocusViewEnableChangedReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action=intent?.action
            if (FOCUS_FRAME_CHANGED_ACTION == action) {
                enableFocusViewFrame = !enableFocusViewFrame
                if (enableFocusViewFrame){
                    attach(service)
                }else{
                    detach()
                }
            }
        }
    }

    private fun register(){
        val filter = IntentFilter()
        filter.addAction(FOCUS_FRAME_CHANGED_ACTION)
        receiver = FocusViewEnableChangedReceiver()
        service.registerReceiver(receiver,filter)
    }

    private fun unregister(){
        service.unregisterReceiver(receiver)
    }
}