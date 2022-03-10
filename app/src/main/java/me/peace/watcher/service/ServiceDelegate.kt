package me.peace.watcher.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

interface ServiceDelegate {
  /**
   * 是否启用。
   */
  fun isEnable(): Boolean
  /**
   * @see AccessibilityService.onCreate
   */
  fun onCreate(service: AccessibilityService) {
    Log.i("aaa", "onCreate() called with: service = $service")
  }
  /**
   * @see AccessibilityService.onServiceConnected
   */
  fun onServiceConnected(service: AccessibilityService) {
    Log.i("aaa", "onServiceConnected() called with: service = $service")
  }
  /**
   * @see AccessibilityService.onAccessibilityEvent
   */
  fun onAccessibilityEvent(service: AccessibilityService, event: AccessibilityEvent) {
    Log.i("aaa", "onAccessibilityEvent() called with: service = $service, event = $event")
  }
  /**
   * @see AccessibilityService.onInterrupt
   */
  fun onInterrupt(service: AccessibilityService) {
    Log.i("aaa", "onInterrupt() called with: service = $service")
  }
  /**
   * @see AccessibilityService.onUnbind
   */
  fun onUnbind(service: AccessibilityService, intent: Intent?) {
    Log.i("aaa", "onUnbind() called with: service = $service, intent = $intent")
  }


  fun onDestroy(service: AccessibilityService) {
    Log.i("aaa", "onDestroy() called with: service = $service")
  }
}