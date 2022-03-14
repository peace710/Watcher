package me.peace.watcher.service.delegate

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

interface ServiceDelegate {

  companion object {
    private const val TAG = "ServiceDelegate"
  }


  /**
   * 是否启用。
   */
  fun isEnable(): Boolean = true
  /**
   * @see AccessibilityService.onCreate
   */
  fun onCreate(service: AccessibilityService) {
    Log.i(TAG, "onCreate() called with: service = $service")
  }
  /**
   * @see AccessibilityService.onServiceConnected
   */
  fun onServiceConnected(service: AccessibilityService) {
    Log.i(TAG, "onServiceConnected() called with: service = $service")
  }
  /**
   * @see AccessibilityService.onAccessibilityEvent
   */
  fun onAccessibilityEvent(service: AccessibilityService, event: AccessibilityEvent) {
    Log.i(TAG, "onAccessibilityEvent() called with: service = $service, event = $event")
  }
  /**
   * @see AccessibilityService.onInterrupt
   */
  fun onInterrupt(service: AccessibilityService) {
    Log.i(TAG, "onInterrupt() called with: service = $service")
  }
  /**
   * @see AccessibilityService.onUnbind
   */
  fun onUnbind(service: AccessibilityService, intent: Intent?) {
    Log.i(TAG, "onUnbind() called with: service = $service, intent = $intent")
  }


  fun onDestroy(service: AccessibilityService) {
    Log.i(TAG, "onDestroy() called with: service = $service")
  }

  fun onStartCommand(intent: Intent?, flags: Int, startId: Int) {
    Log.d(TAG, "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId")
  }
}