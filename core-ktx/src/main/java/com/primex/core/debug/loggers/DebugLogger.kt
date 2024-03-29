package com.primex.core.debug.loggers

import android.util.Log


private const val debugTag = "DebugLogger"

fun logD(tag: String = debugTag, message: String, isDebug: Boolean = false) {
    if (isDebug) {
        Log.d(tag, message)
    }
}


fun logI(tag: String = debugTag, message: String, isDebug: Boolean = false) {
    if (isDebug) {
        Log.i(tag, message)
    }
}

fun logE(tag: String = debugTag, message: String, isDebug: Boolean = false) {
    if (isDebug) {
        Log.e(tag, message)
    }
}