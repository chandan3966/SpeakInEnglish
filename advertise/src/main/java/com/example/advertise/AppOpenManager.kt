package com.example.advertise

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.advertise.callbacks.AppOpenAdCallback
import com.example.advertise.callbacks.AppOpenInternalCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.*

class AppOpenManager(
    private val myApplication: Application,
    private val appOpenAdUnit: String,
    private val adName: String,
    private val isShownOnlyOnce: Boolean,
    private var backgroundThreshold: Int = 30000,
    private var appOpenAdCallback: AppOpenAdCallback?
) :
    LifecycleObserver,
    Application.ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null
    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null
    private var loadTime: Long = 0
    private var backgroundTime: Long = 0
    private var appCount = 0


    /**
     * Creates and returns ad request.
     */
    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    private val isAdAvailable: Boolean
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    private fun fetchAd() {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable) {
            return
        }

        Log.d("appopen","OnStart:" + System.currentTimeMillis()/1000)
        loadOpenAd(
            4000,
            listOf(appOpenAdUnit),
            object : AppOpenInternalCallback {
                override fun onSuccess(ad: AppOpenAd) {
                    Log.d("appopen", "onSuccess: Else Fallback Shown" + System.currentTimeMillis() / 1000)
                    appOpenAdCallback?.onAdLoaded(ad)
                }

                override fun onFailed() {
                    appOpenAdCallback?.onAdFailedToLoad()
                }
            }
        )


    }

    private fun loadOpenAd(fetchedTimer:Int, primartIds:List<String>,appOpenInternalCallback: AppOpenInternalCallback){
        object : CountDownTimer(fetchedTimer.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (appOpenAd != null) {
                    appOpenInternalCallback.onSuccess(appOpenAd!!)
                    this.cancel()
                }
            }

            override fun onFinish() {
                if (appOpenAd!= null) {
                    appOpenInternalCallback.onSuccess(appOpenAd!!)
                }
                else
                    appOpenInternalCallback.onFailed()
            }
        }.start()
        for (appOpenAdUnit in primartIds){
            loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    super.onAdLoaded(ad)
                    if (appOpenAd == null){
                        appOpenAd = ad
                        loadTime = Date().time
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.d(LOG_TAG, loadAdError.message)
                }

            }
            val request = adRequest
            AppOpenAd.load(
                myApplication, appOpenAdUnit, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback!!
            )
        }

    }

//    fun showIfAdLoaded(activity: Activity): Boolean {
//        currentActivity = activity
//        return showAdIfAvailable()
//    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    /**
     * Shows the ad if one isn't already showing.
     *
     * @return True if the ad is available and will try to show ad, False if the ad is not loaded yet
     */
    private fun showAdIfAvailable(): Boolean {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable) {
            if (currentActivity is AdsManager.BypassAppOpenAd) {
                Log.d(LOG_TAG, "AppOpen Ad Bypassed")
                return false
            }
            Log.d(LOG_TAG, "Will show ad.")
            appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    if (isShownOnlyOnce) {
                        ProcessLifecycleOwner.get().lifecycle.removeObserver(this@AppOpenManager)
                        currentActivity = null
                    } else {
                        fetchAd()
                    }
                    appOpenAdCallback?.onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAdCallback?.onAdFailedToShow(adError)
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }
            currentActivity?.let {
                appOpenAd!!.show(it)
                return true
            }
            return false
        } else {
            Log.d(LOG_TAG, "Ad not loaded yet")
            fetchAd()
            return false
        }
    }

    /**
     * LifecycleObserver methods
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (appCount > 0 && !currentActivity.toString().contains("CallerIdActivity")) {
            val appBackgroundTime = System.currentTimeMillis() - backgroundTime
            if (BuildConfig.DEBUG) {
                backgroundThreshold = 1000
            }
            Log.i(LOG_TAG, "App Background Time: $appBackgroundTime ms")
            if (appBackgroundTime > backgroundThreshold)
                showAdIfAvailable()
        }
        appCount++
    }

    /**
     * LifecycleObserver methods
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        backgroundTime = System.currentTimeMillis()
    }


    private var appOpenAd: AppOpenAd? = null

    companion object {
        private const val LOG_TAG = "AdSdk:AppOpenManager"
        private var isShowingAd = false
        private var splashAppOpenAd: AppOpenAd? = null
        private var reason: String? = null
        var initialized: Boolean = false

//        interface appOpenCallBack {
//            fun adDismissed()
//            fun adError(message: String?)
//            fun adShown()
//            fun adClicked()
//            fun adLoaded(appOpenAd: AppOpenAd)
//            fun adNotLoadedYet(reason: String?)
//        }

//        fun loadSplashAppOpenAd(application: Application, adUnit: String) {
//            val build = AdRequest.Builder().build()
//            val adLoadCallBack = object : AppOpenAdLoadCallback() {
//                override fun onAdLoaded(p0: AppOpenAd) {
//                    super.onAdLoaded(p0)
//                    splashAppOpenAd = p0
//                    reason = null
//                }
//
//                override fun onAdFailedToLoad(p0: LoadAdError) {
//                    super.onAdFailedToLoad(p0)
//                    splashAppOpenAd = null
//                    reason = p0.message
//                }
//            }
//            val applicationContext = application.applicationContext
//            AppOpenAd.load(
//                applicationContext, adUnit, build,
//                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, adLoadCallBack
//            )
//        }

//        fun showAdIfAvailable(activity: Activity, appOpenCallBack: appOpenCallBack) {
//            if (splashAppOpenAd != null) {
//                splashAppOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
//                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
//                        super.onAdFailedToShowFullScreenContent(p0)
//                        appOpenCallBack.adError(p0.message)
//                    }
//
//                    override fun onAdShowedFullScreenContent() {
//                        super.onAdShowedFullScreenContent()
//                        appOpenCallBack.adShown()
//                    }
//
//                    override fun onAdDismissedFullScreenContent() {
//                        super.onAdDismissedFullScreenContent()
//                        appOpenCallBack.adDismissed()
//                    }
//
//                    override fun onAdClicked() {
//                        super.onAdClicked()
//                        appOpenCallBack.adClicked()
//                    }
//                }
//                appOpenCallBack.adLoaded(splashAppOpenAd!!)
////                splashAppOpenAd!!.show(activity)
//            } else {
//                appOpenCallBack.adNotLoadedYet(reason)
//            }
//        }
    }

    /**
     * Constructor
     */
    init {
        this.appCount = 0
/*
        if (!isShownOnlyOnce) {
        }
*/
        initialized = true
        myApplication.registerActivityLifecycleCallbacks(this)
        fetchAd()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
}