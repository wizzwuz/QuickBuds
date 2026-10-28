package com.spizganed.quickbuds.tile

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.spizganed.quickbuds.R
import com.spizganed.quickbuds.bluetooth.BudsService
import com.spizganed.quickbuds.protocol.AncModes
import com.spizganed.quickbuds.ui.MainActivity
import com.spizganed.quickbuds.ui.ThemeRes
import com.spizganed.quickbuds.widget.WidgetStateStore

/**
 * Android routes a long-press on a QS tile through ACTION_QS_TILE_PREFERENCES.
 * ANC opens a compact themed mode picker; Normal/Transparency open the main app.
 */
class AncTileLongPressActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeRes.select(this)
        super.onCreate(savedInstanceState)

        val mode = WidgetStateStore.read(this).ancMode
        val isAnc = mode != AncModes.OFF && mode != AncModes.TRANSPARENCY
        if (!isAnc) {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
            return
        }

        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.decorView.setBackgroundColor(Color.TRANSPARENT)
        window.attributes = window.attributes.apply {
            width = dp(320)
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
            dimAmount = 0.42f
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        }

        val palette = ThemeRes.palette(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(16))
            background = GradientDrawable().apply {
                setColor(palette.card)
                cornerRadius = dp(28).toFloat()
                setStroke(dp(1), palette.outline)
            }
        }
        root.addView(TextView(this).apply {
            text = "ANC mode"
            textSize = 19f
            setTextColor(palette.text)
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            gravity = Gravity.CENTER_VERTICAL
        }, LinearLayout.LayoutParams(-1, dp(34)))
        root.addView(TextView(this).apply {
            text = "Choose noise cancellation strength"
            textSize = 13f
            setTextColor(palette.textSecondary)
            setPadding(0, dp(2), 0, dp(10))
        })

        val modes = AncModes.of(this)
        val choices = listOf(
            "Smart" to AncModes.SMART,
            "High" to "ANC-Deep",
            "Low" to "ANC-Light",
            "Medium" to "ANC-Medium"
        )
        choices.forEach { (label, value) ->
            val supported = modes.supports(value)
            val selected = mode == value
            val row = TextView(this).apply {
                text = label + if (selected) "  ✓" else ""
                textSize = 16f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), 0, dp(14), 0)
                setTextColor(if (supported) palette.text else palette.textSecondary)
                alpha = if (supported) 1f else 0.38f
                background = GradientDrawable().apply {
                    setColor(if (selected) palette.tonal else Color.TRANSPARENT)
                    cornerRadius = dp(16).toFloat()
                    if (selected) setStroke(dp(1), palette.accent)
                }
                isEnabled = supported
                isClickable = supported
                setOnClickListener {
                    val serviceIntent = Intent(this@AncTileLongPressActivity, BudsService::class.java).apply {
                        action = BudsService.ACTION_TILE_SET_ANC
                        putExtra(BudsService.EXTRA_TILE_ANC_MODE, value)
                    }
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent)
                        else startService(serviceIntent)
                    } catch (_: Exception) {
                        // The picker closes cleanly; the tile refreshes from the device state next time.
                    }
                    finish()
                }
            }
            val lp = LinearLayout.LayoutParams(-1, dp(48))
            lp.topMargin = dp(4)
            root.addView(row, lp)
        }
        root.setOnClickListener { /* Keep taps on card padding from dismissing the picker. */ }
        setContentView(root)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
}
