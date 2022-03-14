package me.peace.watcher.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import me.peace.watcher.service.delegate.ServiceDelegate
import me.peace.watcher.service.delegate.impl.AppMonitorServiceDelegate
import me.peace.watcher.service.delegate.impl.FocusViewServiceDelegate


class MonitorService: AccessibilityService() {

    private val list = listOf<ServiceDelegate>(AppMonitorServiceDelegate(),FocusViewServiceDelegate())

    override fun onCreate() {
        super.onCreate()
        list.forEach { it.onCreate(this) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        list.forEach { it.onStartCommand(intent,flags,startId) }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        list.forEach { it.onServiceConnected(this) }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { list.filter { it.isEnable() }.forEach { it.onAccessibilityEvent(this,event) } }
    }

    override fun onInterrupt() {
        list.forEach { it.onInterrupt(this) }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        list.forEach { it.onUnbind(this,intent) }
        return super.onUnbind(intent)

    }

    override fun onDestroy() {
        super.onDestroy()
        list.forEach { it.onDestroy(this) }
    }
}