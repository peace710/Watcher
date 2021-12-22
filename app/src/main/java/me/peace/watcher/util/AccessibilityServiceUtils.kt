package me.peace.watcher.util

import android.content.Context
import android.provider.Settings.SettingNotFoundException

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log


object AccessibilityServiceUtils {
    private const val TAG = "AccessibilityService"

    fun goServiceSettings(context: Context) {
        startActivitySafe(context,Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun isAccessibilitySettingsOn(context: Context, serviceName: String?): Boolean {
        var accessibilityEnabled = 0
        val accessibilityFound = false
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        val mStringColonSplitter = SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue: String = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.equals(serviceName, ignoreCase = true)) {
                        return true
                    }
                }
            }
        } else {
            Log.v(TAG, "accessibility disabled");
        }
        return accessibilityFound
    }


    private fun startActivitySafe(context: Context?,intent: Intent){
        val packageManager: PackageManager? = context?.packageManager
        val activities = packageManager?.queryIntentActivities(intent, 0)
        val isIntentSafe = activities?.size!! > 0

        if (isIntentSafe) {
            context?.startActivity(intent)
        }
    }
}