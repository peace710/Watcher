package me.peace.watcher.ui

import android.content.Context
import android.view.WindowManager
import android.view.View


interface Window {

    fun attachWindow(context: Context,view:View,params:WindowManager.LayoutParams)

    fun detachWindow(view:View)

    fun createLayoutParams(x:Int,y:Int,w:Int,h:Int): WindowManager.LayoutParams

    fun updateLocation(view: View,x: Int, y: Int, w: Int, h: Int)
}