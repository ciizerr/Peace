package com.nami.peace.util

import android.util.Log

object DebugLogger {
    private const val TAG = "REMINDER_DEBUG"

    fun log(message: String) {
        Log.e(TAG, message) // Use Error level so it's easy to see
    }
}
