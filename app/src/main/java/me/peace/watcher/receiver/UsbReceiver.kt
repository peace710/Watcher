package me.peace.watcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import me.peace.watcher.config.Config
import me.peace.watcher.service.PageService


class UsbReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action=intent?.action
        if (Intent.ACTION_MEDIA_MOUNTED == action) {
            if (Config.ENABLE_USB_CHECK) {
                val path = intent.data?.path ?: ""
                PageService.start(context, path)
            }
        } else if (Intent.ACTION_MEDIA_UNMOUNTED == action) {
            if (Config.ENABLE_USB_CHECK) {
                PageService.stop(context)
            }
        }
    }
}