package me.peace.watcher

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import me.peace.watcher.service.PageService
import me.peace.watcher.util.AccessibilityServiceUtils
import me.peace.watcher.util.Utils

class MainActivity : AppCompatActivity() {
    companion object{
        private const val SERVICE = "me.peace.watcher/me.peace.watcher..service.PageService"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.enable_accessibility).setOnClickListener {
            if (!AccessibilityServiceUtils.isAccessibilitySettingsOn(this, SERVICE)){
                AccessibilityServiceUtils.goServiceSettings(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<Button>(R.id.enable_accessibility).requestFocus()
    }
}