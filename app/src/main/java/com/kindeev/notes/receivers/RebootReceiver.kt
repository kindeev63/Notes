package com.kindeev.notes.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.kindeev.notes.services.UpdateService

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, UpdateService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

}