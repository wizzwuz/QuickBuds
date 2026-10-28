package com.spizganed.quickbuds.tile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.graphics.drawable.Icon
import com.spizganed.quickbuds.R
import com.spizganed.quickbuds.bluetooth.BudsService
import com.spizganed.quickbuds.protocol.AncModes
import com.spizganed.quickbuds.widget.WidgetStateStore

/**
 * Quick Settings tile for toggling the earbuds between ANC and Off.
 *
 * The command is handled by BudsService so it reuses the app's model-aware protocol,
 * RFCOMM connection, and state updates instead of sending guessed Bluetooth packets.
 */
class AncQuickSettingsTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileFromStoredState()
    }

    override fun onClick() {
        super.onClick()
        val state = WidgetStateStore.read(this)
        val ancIsOn = state.ancMode != AncModes.OFF &&
            state.ancMode != AncModes.TRANSPARENCY

        val target = if (ancIsOn) {
            AncModes.OFF
        } else {
            val modes = AncModes.of(this)
            when {
                modes.supports("ANC-Medium") -> "ANC-Medium"
                modes.levels.isNotEmpty() -> modes.levels.first()
                else -> "ANC-Medium"
            }
        }

        val serviceIntent = Intent(this, BudsService::class.java).apply {
            action = BudsService.ACTION_TILE_SET_ANC
            putExtra(BudsService.EXTRA_TILE_ANC_MODE, target)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            // Immediate feedback; BudsService refreshes this tile after the buds report their mode.
            qsTile?.state = if (target == AncModes.OFF) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            qsTile?.label = "ANC"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                qsTile?.subtitle = if (target == AncModes.OFF) "Off" else "On"
            }
            qsTile?.updateTile()
        } catch (_: Exception) {
            // Do not crash System UI if Android blocks the service start. The next listening
            // pass restores the tile from the last mode the earbuds reported.
            updateTileFromStoredState()
        }
    }

    private fun updateTileFromStoredState() {
        val tile = qsTile ?: return
        val mode = WidgetStateStore.read(this).ancMode
        val active = mode != AncModes.OFF && mode != AncModes.TRANSPARENCY
        tile.label = "ANC"
        tile.icon = Icon.createWithResource(this, R.drawable.ic_anc)
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (active) "On" else "Off"
        }
        tile.updateTile()
    }

    companion object {
        /** Ask System UI to reread the last device-reported mode after an ANC state change. */
        fun requestRefresh(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                runCatching {
                    TileService.requestListeningState(
                        context,
                        ComponentName(context, AncQuickSettingsTileService::class.java)
                    )
                }
            }
        }
    }
}
