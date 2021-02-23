package com.stark.findyourbuddy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LocationBroadCast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.`package`.equals("android.intent.action.BOOT_COMPLETED")) {

            val intent = Intent(context, LocationService::class.java)
            context!!.startService(intent)
        }
    }
}