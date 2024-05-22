package me.neversleep.plusplus

import android.os.Build
import android.os.IBinder
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object HookImpl {
    const val TAG: String = "neversleep"

    fun main(classLoader: ClassLoader?) {
        try {
            val xSharedPreferences: XSharedPreferences =
                XSharedPreferences(BuildConfig.APPLICATION_ID, "x_conf")
            xSharedPreferences.makeWorldReadable()
            xSharedPreferences.reload()
            XposedBridge.hookAllMethods(
                XposedHelpers.findClass(
                    "com.android.server.policy.PhoneWindowManager",
                    classLoader
                ), "powerPress", object : XC_MethodHook() {
                    // from class: me.neversleep.plusplus.HookImpl.1
                    var mode: Int = 0

                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(methodHookParam: XC_MethodHook.MethodHookParam) {
                        super.beforeHookedMethod(methodHookParam)
                        try {
                            XUtils.xLog("neversleep", "beforeHookedMethod: start")
                            xSharedPreferences.reload()
                            var i = 0
                            if (!xSharedPreferences.getBoolean("power", false)) {
                                Log.e("neversleep", "beforeHookedMethod: power is false")
                                return
                            }
                            XUtils.xLog("neversleep", "beforeHookedMethod: power is true")
                            val cls =
                                Class.forName("android.view.SurfaceControl", false, classLoader)
                            val iBinder = getDisplayBinder(classLoader)
                            if (iBinder != null) {
                                XposedHelpers.callStaticMethod(
                                    cls,
                                    "setDisplayPowerMode",
                                    iBinder,
                                    this.mode
                                )
                                if (this.mode == 0) {
                                    i = 2
                                }
                                this.mode = i
                            }
                            methodHookParam.setResult(null)
                            XUtils.xLog("neversleep", "replace success")
                        } catch (th: Throwable) {
                            XUtils.xLog("neversleep", "beforeHookedMethod: error:", th)
                        }
                    }
                })
            XUtils.xLog("neversleep", "main: Hook success")
        } catch (th: Throwable) {
            th.printStackTrace()
            XUtils.xLog("neversleep", "main: error:" + th.message, th)
        }
    }

    fun getDisplayBinder(classLoader: ClassLoader?): Any? {
        try {
            val clsSurfaceControl: Class<*> =
                XposedHelpers.findClass("android.view.SurfaceControl", classLoader)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) XposedHelpers.callStaticMethod(
                    clsSurfaceControl,
                    "getBuiltInDisplay",
                    0
                ) else XposedHelpers.callStaticMethod(clsSurfaceControl, "getInternalDisplayToken")
            } else {
                val clsDisplayControl: Class<*> = XposedHelpers.findClass(
                    "com.android.server.display.DisplayControl",
                    classLoader
                )
                val ids = XposedHelpers.callStaticMethod(
                    clsDisplayControl,
                    "getPhysicalDisplayIds"
                ) as LongArray
                if (ids == null || ids.size == 0) {
                    return null
                }
                return XposedHelpers.callStaticMethod(
                    clsDisplayControl, "getPhysicalDisplayToken",
                    ids[0]
                )
            }
        } catch (t: Throwable) {
            XUtils.xLog(TAG, "getDisplayBinder", t)
        }
        return null
    }
}