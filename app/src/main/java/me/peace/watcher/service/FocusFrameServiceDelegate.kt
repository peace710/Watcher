package me.peace.watcher.service

import android.accessibilityservice.AccessibilityService
import android.animation.RectEvaluator
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import me.peace.watcher.R
import me.peace.watcher.config.Config

/**
 * 高亮焦点控件。
 */
class FocusFrameServiceDelegate : ServiceDelegate {

  private var focusFrameIsEnable = true

  private var receiver: BroadcastReceiver? = null

  private lateinit var manager: WindowManager
  /** 高亮 view 。 */
  private lateinit var view: View
  private lateinit var params: WindowManager.LayoutParams

  /** 动画执行过程中当前 [view] 的位置。 */
  private val animatorFocusRect = Rect()
  /** 当前 [view] 的目标位置。 */
  private val currentFocusRect = Rect()
  /** [view] 变化位置的动画。 */
  private val animator = ValueAnimator.ofObject(RectEvaluator(), Rect()).also {
    it.duration = 100
    it.addUpdateListener { animator ->
      val value = animator.animatedValue as Rect
      animatorFocusRect.set(value)
      params.height = value.height()
      params.width = value.width()
      params.x = value.left
      params.y = value.top
      manager.updateViewLayout(view, params)
    }
  }

  override fun isEnable(): Boolean = focusFrameIsEnable

  override fun onServiceConnected(service: AccessibilityService) {
    super.onServiceConnected(service)
    // 创建高亮 view
    manager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    view = View(service)
    view.setBackgroundResource(R.drawable.focus_frame_bg)
    params = WindowManager.LayoutParams()
    params.gravity = Gravity.TOP or Gravity.START
    params.format = PixelFormat.RGBA_8888
    params.x = 0
    params.y = 0
    params.width = 0
    params.height = 0
    params.type = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
    params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    // 添加将高亮 view
    manager.addView(view, params)
    updateLocation(service)
  }

  /**
   * 更新 [view] 的坐标位置。
   */
  private fun updateLocation(service: AccessibilityService) {
    if (!::view.isInitialized || !view.isAttachedToWindow ) return
    val rect = Rect()
    service.rootInActiveWindow.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)?.getBoundsInScreen(rect)
    if (rect == currentFocusRect) return
    currentFocusRect.set(rect)
    if (focusFrameIsEnable) {
      updateLocation(rect)
    }
  }

  private fun updateLocation(rect: Rect) {
    animator.cancel()
    animator.setObjectValues(animatorFocusRect, rect)
    animator.start()
  }

  override fun onAccessibilityEvent(service: AccessibilityService, event: AccessibilityEvent) {
    super.onAccessibilityEvent(service, event)

    updateLocation(service)
  }

  override fun onUnbind(service: AccessibilityService, intent: Intent?) {
    if (!::manager.isInitialized) return
    animator.cancel()
    manager.removeView(view)
  }


  override fun onCreate(service: AccessibilityService) {
    super.onCreate(service)
    val filter = IntentFilter()
    filter.addAction(Config.FOCUS_FRAME_CHANGED_ACTION)
    receiver = EnableReceiver()
    service.registerReceiver(receiver,filter)
  }


  override fun onDestroy(service: AccessibilityService) {
    super.onDestroy(service)
    service.unregisterReceiver(receiver)
  }

  inner class EnableReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
      val action=intent?.action
      if (Config.FOCUS_FRAME_CHANGED_ACTION == action) {
        focusFrameIsEnable = !focusFrameIsEnable
        if (!focusFrameIsEnable){
          view.setBackgroundResource(R.drawable.no_bg)
        }else{
          updateLocation(currentFocusRect)
          view.setBackgroundResource(R.drawable.focus_frame_bg)
        }
      }
    }
  }
}