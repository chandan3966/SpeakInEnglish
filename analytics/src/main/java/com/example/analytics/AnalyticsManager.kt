package com.example.analytics

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import java.io.File

object AnalyticsManager {
    private val binaryPlaces = arrayOf(
        "/data/bin/", "/system/bin/", "/system/xbin/", "/sbin/",
        "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/",
        "/data/local/"
    )
    private lateinit var sAppContext: Context
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
//    private var cleverTapDefaultInstance: CleverTapAPI? = null

    private fun canSend(): Boolean {
        return sAppContext != null && mFirebaseAnalytics != null
    }

    private fun canPush(): Boolean {
        return sAppContext != null != null
    }

    @Synchronized
    fun initialize(context: Context) {
        try {
            sAppContext = context
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)

            try {
            } catch (e: Exception) {
                e.printStackTrace()
            }
            /*ApxorSDK.initialize(R.string.apxor_id, context)*/
            setProperty(
                "DeviceType",
                getDeviceType(context)
            )
            setProperty(
                "Rooted",
                java.lang.Boolean.toString(isRooted())
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setProperty(
        propertyName: String,
        propertyValue: String
    ) {
        if (canSend()) {
            mFirebaseAnalytics.setUserProperty(propertyName, propertyValue)
        }
    }

    fun logEvent(eventName: String) {
        try {
            if (canSend()) {
                mFirebaseAnalytics.logEvent(eventName, Bundle())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logEvent(eventName: String, params: Bundle) {
        try {
            if (canSend()) {
                mFirebaseAnalytics.logEvent(eventName, params)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun bundleToMap(extras: Bundle): HashMap<String?, Any?>? {
        val map =
            HashMap<String?, Any?>()
        try {
            val ks = extras.keySet()
            for (key in ks) {
                map[key] = extras.getString(key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    fun setCurrentScreen(
        activity: Activity,
        screenName: String?
    ) {
        if (canSend()) {
            if (null != screenName) {
                mFirebaseAnalytics.setCurrentScreen(activity, screenName, screenName)
                /*ApxorSDK.trackScreen(screenName)*/
            }
        }
    }

    private fun getDeviceType(c: Context): String {
        try {
            val uiModeManager = c.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            return when (uiModeManager.currentModeType) {
                Configuration.UI_MODE_TYPE_TELEVISION -> "TELEVISION"
                Configuration.UI_MODE_TYPE_WATCH -> "WATCH"
                Configuration.UI_MODE_TYPE_NORMAL -> if (isTablet(
                        c
                    )
                ) "TABLET" else "PHONE"
                Configuration.UI_MODE_TYPE_UNDEFINED -> "UNKOWN"
                else -> ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "UNKNOWN"
        }
    }

    private fun isRooted(): Boolean {
        for (p in binaryPlaces) {
            val su = File(p + "su")
            if (su.exists()) {
                return true
            }
        }
        return false
    }

    private fun isTablet(context: Context): Boolean {
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }


    fun MaptoBundle(hashMap: HashMap<String?, Any?>?): Bundle {
        val bundle = Bundle()
        try {
            for ((key, value) in hashMap?.entries!!) {
                bundle.putString(key, value.toString())
            }
            return bundle
        } catch (e: Exception) {
            e.printStackTrace()
            return Bundle()
        }
        return Bundle()
    }
}