package me.neversleep.plusplus

import android.content.Context
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.neversleep.plusplus.HookImpl.main

class XMain : IXposedHookLoadPackage, IXposedHookZygoteInit {
    @Throws(Throwable::class)
    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XUtils.xLog("neversleep", "package:" + loadPackageParam.packageName)
        XUtils.xLog("neversleep", "process:" + loadPackageParam.processName)
        if ("android" == loadPackageParam.packageName) {
            XUtils.xLog("neversleep", "start hook system_server...")
            hookAndroid(loadPackageParam)
            XUtils.xLog("neversleep", "end hook system_server...")
        }
        if (BuildConfig.APPLICATION_ID.equals(loadPackageParam.packageName)) {
            hookSelf(loadPackageParam)
        }
    }

    @Throws(Throwable::class)
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        if (startupParam.startsSystemServer) {
            XUtils.xLog("neversleep", "initZygote:" + startupParam.modulePath)
            XUtils.xLog("neversleep", "start hook system_server...")
            main(null)
            XUtils.xLog("neversleep", "end hook system_server...")
        }
    }

    @Throws(Throwable::class)
    private fun hookSelf(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            "me.neversleep.plusplus.MainActivity",
            loadPackageParam.classLoader,
            "getActiveVersion",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(methodHookParam: XC_MethodHook.MethodHookParam) {
                    super.afterHookedMethod(methodHookParam)
                    methodHookParam.setResult(BuildConfig.VERSION_CODE)
                }
            })
    }

    @Throws(Throwable::class)
    private fun hookAndroid(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val xSharedPreferences: XSharedPreferences =
            XSharedPreferences(BuildConfig.APPLICATION_ID, "x_conf")
        xSharedPreferences.makeWorldReadable()
        xSharedPreferences.reload()
        XposedBridge.hookAllMethods(
            XposedHelpers.findClass(
                "com.android.server.am.ActivityManagerService",
                loadPackageParam.classLoader
            ), "systemReady", object : XC_MethodHook() {
                // from class: me.neversleep.plusplus.XMain.2
                @Throws(Throwable::class)
                override fun beforeHookedMethod(methodHookParam: XC_MethodHook.MethodHookParam) {
                    try {
                        XUtils.xLog("neversleep", "Preparing system")
                        XUtils.xLog("neversleep", " Preparing system")
                        getContext(methodHookParam.thisObject)
                    } catch (th: Throwable) {
                        XUtils.xLog("neversleep", Log.getStackTraceString(th))
                    }
                }

                @Throws(Throwable::class)
                override fun afterHookedMethod(methodHookParam: XC_MethodHook.MethodHookParam) {
                    try {
                        XUtils.xLog("neversleep", "System ready")
                        getContext(methodHookParam.thisObject)
                        main(methodHookParam.thisObject.javaClass.classLoader)
                    } catch (th: Throwable) {
                        Log.e("neversleep", Log.getStackTraceString(th))
                        XposedBridge.log(th)
                    }
                }

                @Throws(Throwable::class)
                private fun getContext(obj: Any): Context {
                    var context: Context? = null
                    var cls: Class<*>? = obj.javaClass
                    while (cls != null && context == null) {
                        val declaredFields = cls.declaredFields
                        val length = declaredFields.size
                        var i = 0
                        while (true) {
                            if (i < length) {
                                val field = declaredFields[i]
                                if (field.type == Context::class.java) {
                                    field.isAccessible = true
                                    context = field[obj] as Context
                                    XUtils.xLog(
                                        "neversleep",
                                        "Context found in " + cls + " as " + field.name
                                    )
                                    break
                                }
                                i++
                            }
                        }
                        cls = cls.superclass
                    }
                    if (context != null) {
                        return context
                    }
                    throw Throwable("Context not found")
                }
            })

        val powerGroupClass: Class<*> = XposedHelpers.findClass(
            "com.android.server.power.PowerGroup",
            loadPackageParam.classLoader
        )

        XposedHelpers.findAndHookMethod(
            "com.android.server.power.PowerManagerService",
            loadPackageParam.classLoader,
            "isBeingKeptAwakeLocked",
            powerGroupClass,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    xSharedPreferences.reload()
                    Log.e(
                        "neversleep",
                        "get_disable_sleep: disable_sleep is " + java.lang.String.valueOf(
                            xSharedPreferences.getBoolean("disable_sleep", false)
                        )
                    )

                    if (!xSharedPreferences.getBoolean("disable_sleep", false)) {
                        Log.e("neversleep", "afterHookedMethod: disable_sleep is false")
                        return
                    }
                    param.setResult(true)
                    Log.e("neversleep", "afterHookedMethod: disable_sleep is true")
                }
            })
    }

    companion object {
        const val TAG: String = "neversleep"
    }
}