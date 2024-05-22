package me.neversleep.plusplus

import android.util.Log
import de.robv.android.xposed.XposedBridge

object XUtils {
    const val TAG: String = "Utils"

    @JvmOverloads
    fun xLog(str: String, str2: String, th: Throwable? = null) {
        XposedBridge.log("me.neversleep.plusplus::$str::$str2")
        if (th != null) {
            XposedBridge.log(th)
        }
        if (th != null) {
            Log.e(str, str2, th)
        } else {
            Log.e(str, str2)
        }
    }
}