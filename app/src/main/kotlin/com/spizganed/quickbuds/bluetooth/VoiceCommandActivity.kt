package com.spizganed.quickbuds.bluetooth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.spizganed.quickbuds.ui.Gesture
import com.spizganed.quickbuds.ui.GestureAction
import com.spizganed.quickbuds.ui.GestureConfigStore
import com.spizganed.quickbuds.ui.GestureSide

class VoiceCommandActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var targetPkg: String? = null
        val gestures = listOf(
            Gesture.DOUBLE_TAP,
            Gesture.TRIPLE_TAP,
            Gesture.TAP_HOLD,
            Gesture.SINGLE_TAP,
            Gesture.LONG_PRESS
        )
        val sides = listOf(GestureSide.LEFT, GestureSide.RIGHT)
        for (g in gestures) {
            for (s in sides) {
                if (GestureConfigStore.load(this, s, g).contains(GestureAction.CUSTOM_APP)) {
                    val pkg = GestureConfigStore.getCustomApp(this, s, g)
                    if (!pkg.isNullOrEmpty()) {
                        targetPkg = pkg
                        break
                    }
                }
            }
            if (targetPkg != null) break
        }

        if (!targetPkg.isNullOrEmpty()) {
            val launchIntent = packageManager.getLaunchIntentForPackage(targetPkg)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            if (launchIntent != null) {
                try {
                    startActivity(launchIntent)
                } catch (_: Exception) {}
            }
        }
        finish()
    }
}
