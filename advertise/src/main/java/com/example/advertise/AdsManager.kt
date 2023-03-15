package com.example.advertise

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.appyhigh.adutils.callbacks.NativeAdLoadCallback
import com.appyhigh.adutils.models.PreloadNativeAds
import com.example.advertise.callbacks.AdCallbacks
import com.example.advertise.callbacks.AppOpenAdCallback
import com.example.advertise.interfaces.NativeInternalCallback
import com.example.advertise.models.AdUtilConstants
import com.example.advertise.models.AdUtilConstants.preloadNativeAdList
import com.example.advertise.models.NativeAdItemService
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.ump.ConsentInformation
import java.util.*


object AdsManager {
    private lateinit var sAppContext: Context
    final val TAG = "ads"

    var unifiedNative: NativeAd? = null

    @SuppressLint("StaticFieldLeak")
    var unifiedNativeAdView: NativeAdView? = null

    var videoUnifiedNative: NativeAd? = null

    @SuppressLint("StaticFieldLeak")
    var videoUnifiedNativeAdView: NativeAdView? = null
    private var isInitialized: Boolean = false

    @Synchronized
    fun initialize(context: Context) {
        try {
//            sAppContext = context
//            if (BuildConfig.DEBUG){
//                val testDeviceIds = Arrays.asList(
//                    Settings.Secure.getString(
//                    sAppContext.getContentResolver(),
//                    Settings.Secure.ANDROID_ID))
//                val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//                MobileAds.setRequestConfiguration(configuration)
//            }
//            MobileAds.initialize(context,
//                OnInitializationCompleteListener {
//                    Log.i(TAG, "SDK Initialized")
//                }
//            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var application: Application? = null
    fun isInitialised(): Boolean = isInitialized
    var consentInformation: ConsentInformation? = null

    lateinit var app:Application
    fun initialize(
        app: Application,
        testDevice: String? = null,
        preloadingNativeAdList: HashMap<String, PreloadNativeAds>? = null,
        fetchingCallback: FetchingCallback? = null
    ) {
        this.app = app
        application = app
        sAppContext = application?.applicationContext!!
        val inflater = LayoutInflater.from(app)
//        if (consentInformation == null) {
//            consentInformation = ConsentInformation.getInstance(app)
//        }
//        if (consentInformation?.consentStatus == ConsentStatus.NON_PERSONALIZED) {
//            extras.putString("npa", "1")
//        }
//        val string = AppPrefs.ads.get()
//        if (string != null) {
//            DynamicsAds.adMobNew = JSONObject(string)
//        }
        if (testDevice != null) {
            val build = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(testDevice)).build()
            MobileAds.setRequestConfiguration(build)
        }
        MobileAds.initialize(app) {
            isInitialized = true
            preloadNativeAdList = preloadingNativeAdList
            val context = application?.applicationContext
            if (context != null) {
                if (preloadNativeAdList != null && inflater != null) {
                    preloadAds(inflater, context)
                }
            }
            fetchingCallback?.OnComplete()
        }

    }

    interface FetchingCallback{
        fun OnComplete()
    }

    fun attachAppOpenAdManager(
        appOpenAdUnit: String,
        adName: String,
        appOpenAdCallback: AppOpenAdCallback? = null,
        backgroundThreshold: Int = 30000,
        isShownOnlyOnce: Boolean = false
    ) {
        if (application != null) {
            if (!AppOpenManager.initialized) {
                val appOpenManager =
                    AppOpenManager(
                        application!!,
                        appOpenAdUnit,
                        adName,
                        isShownOnlyOnce,
                        backgroundThreshold,
                        appOpenAdCallback
                    )
                appOpenAdCallback?.onInitSuccess(appOpenManager)
            }
        }
    }

    private var mInterstitialAd: InterstitialAd? = null

    fun requestInterstitial( listener: AdCallbacks,adUnit: String){
        val adRequest: AdRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            sAppContext, adUnit, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            Log.d(TAG, "Ad was clicked.")
                            listener.AdClicked()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed fullscreen content.")
                            listener.AdClosed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Ad failed to show fullscreen content.")
                            listener.AdFailed()
                        }

                        override fun onAdImpression() {
                            Log.d(TAG, "Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad showed fullscreen content.")
                        }
                    }
                    Log.i(TAG, "onAdLoaded")
                    listener.AdLoad()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, loadAdError.toString())
                    mInterstitialAd = null
                    listener.AdFailed()
                }
            })
    }

    fun showInterstitial(activity: Activity){
        if (mInterstitialAd != null){
            mInterstitialAd!!.show(activity)
        }
        else{
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }

    private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
        adView.mediaView = adView.findViewById(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headlines)
        adView.bodyView = adView.findViewById(R.id.ad_body_text)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action_button)
        adView.iconView = adView.findViewById(R.id.ad_icons)
        adView.priceView = adView.findViewById(R.id.ad_price_text)
        adView.starRatingView = adView.findViewById(R.id.ad_stars_bar)
        adView.storeView = adView.findViewById(R.id.ad_store_text)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser_text)

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView?.setMediaContent(nativeAd.mediaContent!!)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.GONE
        } else {
            adView.bodyView?.visibility = View.GONE
            (adView.bodyView as TextView?)?.text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button?)?.text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView?)?.setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }
        if (nativeAd.price == null) {
            adView.priceView?.visibility = View.INVISIBLE
        } else {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as TextView?)?.text = nativeAd.price
        }
        if (nativeAd.store == null) {
            adView.storeView?.visibility = View.INVISIBLE
        } else {
            adView.storeView?.visibility = View.VISIBLE
            (adView.storeView as TextView?)?.text = nativeAd.store
        }
        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar?)?.rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView?.visibility = View.VISIBLE
        }
        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView?)?.text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.mediaContent?.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc?.hasVideoContent() == true) {

            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
            }
        }
    }

    fun loadNativeAdFromService(
        layoutInflater: LayoutInflater,
        context: Context,
        lifecycle: Lifecycle,
        adUnit: String,
        adName: String,
        viewGroup: ViewGroup,
        nativeAdLoadCallback: NativeAdLoadCallback?,
        adType: String = "1",
        mediaMaxHeight: Int = 300,
        loadingTextSize: Int = 24,
        background: Any?,
        textColor1: Int?,
        textColor2: Int?,
        id: Long = viewGroup.id.toLong(),
        populator: ((nativeAd: NativeAd, adView: NativeAdView) -> Unit)? = null,
        preloadAds: Boolean = false,
        autoRefresh: Boolean = false,
        contentURL: String? = null,
        neighbourContentURL: List<String>? = null,
        loadTimeOut:Int
    ) {
        if (adUnit != "STOP") {
            @LayoutRes val layoutId = R.layout.main_ad_template_view
            viewGroup.visibility = View.VISIBLE

            val inflate = layoutInflater.inflate(R.layout.ad_loading_layout, null)
            val id1 = inflate.findViewById<View>(R.id.cardView)
            val tv = inflate.findViewById<TextView>(R.id.tv)
            tv.textSize = loadingTextSize.toFloat()
            if (textColor1 != null) {
                tv.setTextColor(textColor1)
            }
            when (background) {
                is String -> {
                    id1.setBackgroundColor(Color.parseColor(background))
                }
                is Drawable -> {
                    id1.background = background
                }
                is Int -> {
                    id1.setBackgroundColor(background)
                }
            }
            viewGroup.removeAllViews()
            viewGroup.addView(inflate)
            if (adUnit.isBlank()) return
            if (preloadNativeAdList != null) {
                val preloadNativeAds = preloadNativeAdList!![adName]
                val ad = preloadNativeAds?.ad
                if (ad != null) {
                    viewGroup.removeAllViews()
                    viewGroup.addView(ad)
                    preloadNativeAds.ad = null
                    if (preloadAds) {
                        preloadAds(layoutInflater, context)
                    }
                    lifecycle.addObserver(object : LifecycleObserver {
                        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                        fun onDestroy() {
                            AdUtilConstants.nativeAdLifeCycleServiceHashMap.remove(id)
                        }
                    })
                    if (AdUtilConstants.nativeAdLifeCycleServiceHashMap[id] == null ) {
                        AdUtilConstants.nativeAdLifeCycleServiceHashMap[id] = NativeAdItemService(
                            layoutInflater,
                            context,
                            lifecycle,
                            id,
                            adUnit,
                            adName,
                            viewGroup,
                            nativeAdLoadCallback,
                            populator,
                            adType,
                            background,
                            textColor1,
                            textColor2,
                            mediaMaxHeight,
                            loadingTextSize,
                            preloadAds,
                            autoRefresh,
                            contentURL,
                            neighbourContentURL,
                        )
//                        refreshNativeService(adName)
                    }

                } else {
                    if (preloadAds) {
                        preloadAds(layoutInflater, context)
                    }
                    loadNativeAdFromServiceInternal(
                        layoutInflater,
                        context,
                        lifecycle,
                        adName,
                        viewGroup,
                        nativeAdLoadCallback,
                        background = background,
                        textColor1 = textColor1,
                        textColor2 = textColor2,
                        mediaMaxHeight = mediaMaxHeight,
                        loadingTextSize = loadingTextSize,
                        id = id,
                        populator = populator,
                        adType = adType,
                        preloadAds = preloadAds,
                        autoRefresh = preloadAds,
                        contentURL = contentURL,
                        neighbourContentURL = neighbourContentURL,
                        layoutId,
                        loadTimeOut,
                        listOf(adUnit),
                        object :NativeInternalCallback{
                            override fun onSuccess(nativeAd: NativeAd?) {
                                nativeAdLoadCallback?.onAdLoaded()
                                val adView = layoutInflater.inflate(layoutId, null)
                                        as NativeAdView
                                if (background != null) {
                                    when (background) {
                                        is String -> {
                                            adView.setBackgroundColor(Color.parseColor(background))
                                        }
                                        is Drawable -> {
                                            adView.background = background
                                        }
                                        is Int -> {
                                            adView.setBackgroundColor(background)
                                        }
                                    }
                                }
                                if (populator != null) {
                                    populator.invoke(nativeAd!!, adView)
                                } else {
                                    populateUnifiedNativeAdView(
                                        nativeAd!!,
                                        adView,
                                    )
                                }
                                viewGroup.removeAllViews()
                                viewGroup.addView(adView)
//                                refreshNativeService(adName)
                            }

                            override fun onFailure(loadAdError: LoadAdError?) {
                                nativeAdLoadCallback?.onAdFailed(loadAdError)
                            }
                        }
                    )
                    /*The Extra Parameters are just for logging*/
                }
            }
            else {
                if (preloadAds) {
                    preloadAds(layoutInflater, context)
                }

                loadNativeAdFromServiceInternal(
                    layoutInflater,
                    context,
                    lifecycle,
                    adName,
                    viewGroup,
                    nativeAdLoadCallback,
                    background = background,
                    textColor1 = textColor1,
                    textColor2 = textColor2,
                    mediaMaxHeight = mediaMaxHeight,
                    loadingTextSize = loadingTextSize,
                    id = id,
                    populator = populator,
                    adType = adType,
                    preloadAds = preloadAds,
                    autoRefresh = preloadAds,
                    contentURL = contentURL,
                    neighbourContentURL = neighbourContentURL,
                    layoutId,
                    loadTimeOut,
                    listOf(adUnit),
                    object :NativeInternalCallback{
                        override fun onSuccess(nativeAd: NativeAd?) {
                            nativeAdLoadCallback?.onAdLoaded()
                            val adView = layoutInflater.inflate(layoutId, null)
                                    as NativeAdView
                            if (background != null) {
                                when (background) {
                                    is String -> {
                                        adView.setBackgroundColor(Color.parseColor(background))
                                    }
                                    is Drawable -> {
                                        adView.background = background
                                    }
                                    is Int -> {
                                        adView.setBackgroundColor(background)
                                    }
                                }
                            }
                            if (populator != null) {
                                populator.invoke(nativeAd!!, adView)
                            } else {
                                populateUnifiedNativeAdView(
                                    nativeAd!!,
                                    adView,
                                )
                            }
                            viewGroup.removeAllViews()
                            viewGroup.addView(adView)
//                            refreshNativeService(adName)
                        }

                        override fun onFailure(loadAdError: LoadAdError?) {
                            nativeAdLoadCallback?.onAdFailed(loadAdError)
                        }
                    }
                )
            }
        } else {
            viewGroup.visibility = View.GONE
        }
    }

    internal fun loadNativeAdFromServiceInternal(
        layoutInflater: LayoutInflater,
        context: Context,
        lifecycle: Lifecycle,
        adName: String,
        viewGroup: ViewGroup,
        nativeAdLoadCallback: NativeAdLoadCallback?,
        background: Any?,
        textColor1: Int?,
        textColor2: Int?,
        mediaMaxHeight: Int,
        loadingTextSize: Int,
        id: Long,
        populator: ((nativeAd: NativeAd, adView: NativeAdView) -> Unit)?,
        adType: String,
        preloadAds: Boolean,
        autoRefresh: Boolean,
        contentURL: String?,
        neighbourContentURL: List<String>?,
        layoutId: Int,
        fetchedTimer: Int,
        primaryIds: List<String>,
        nativeInternalCallback: NativeInternalCallback
    ) {
        var nativeAd: NativeAd? = null
        object : CountDownTimer(fetchedTimer.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (nativeAd != null) {
                    nativeInternalCallback.onSuccess(nativeAd)
                    this.cancel()
                }
            }

            override fun onFinish() {
                if (nativeAd != null) {
                    nativeInternalCallback.onSuccess(nativeAd)
                }
                else
                    nativeInternalCallback.onFailure(null)
            }
        }.start()
        var loadedUnit = ""
        for (adUnit in primaryIds){

            val adLoader: AdLoader = AdLoader.Builder(context, adUnit)
                .forNativeAd { ad: NativeAd ->
                    if (nativeAd == null) {
                        nativeAd = ad
                        if (loadedUnit.equals(""))
                            loadedUnit = adUnit
                    }

                }
                .withAdListener(object : AdListener() {

                    override fun onAdClicked() {
                        super.onAdClicked()
                        nativeAdLoadCallback?.onAdClicked()
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        if (nativeAd == null)
                            nativeAd = null
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        if (AdUtilConstants.nativeAdLifeCycleServiceHashMap[id] == null && nativeAd != null) {
                            AdUtilConstants.nativeAdLifeCycleServiceHashMap[id] = NativeAdItemService(
                                layoutInflater,
                                context,
                                lifecycle,
                                id,
                                if (!loadedUnit.equals("")) loadedUnit else adUnit,
                                adName,
                                viewGroup,
                                nativeAdLoadCallback,
                                populator,
                                adType,
                                background,
                                textColor1,
                                textColor2,
                                mediaMaxHeight,
                                loadingTextSize,
                                preloadAds,
                                autoRefresh,
                                contentURL,
                                neighbourContentURL
                            )
                        }
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .setRequestCustomMuteThisAd(true)
                        .build()
                ).build()

            loadAd(
                adLoader,
                contentURL,
                neighbourContentURL
            )
        }
    }

    fun requestNativeAd(view: LinearLayout, @LayoutRes layoutId: Int, adUnit: String) {
        try {
            if (unifiedNative != null) {
                unifiedNativeAdView = LayoutInflater.from(view.context)
                    .inflate(layoutId, null) as NativeAdView
                try {
                    populateUnifiedNativeAdView(unifiedNative!!, unifiedNativeAdView!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                view.removeAllViews()
                view.addView(unifiedNativeAdView)
                unifiedNativeAdView?.visibility = View.VISIBLE
            } else {
                try {
                    val adLoader = AdLoader.Builder(
                        view.context,
                        adUnit
                    )
                        .forNativeAd { unifiedNativeAd ->
                            unifiedNative = unifiedNativeAd
                            if (unifiedNative != null) {
                                unifiedNativeAdView = LayoutInflater.from(view.context)
                                    .inflate(
                                        layoutId,
                                        null
                                    ) as NativeAdView
                                try {
                                    populateUnifiedNativeAdView(
                                        unifiedNative!!,
                                        unifiedNativeAdView!!
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                view.removeAllViews()
                                view.addView(unifiedNativeAdView)
                                unifiedNativeAdView!!.visibility = View.VISIBLE
                            }
                        }
                        .withAdListener(object : AdListener() {
                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                Log.d(TAG, "onAdFailedToLoad: "+loadAdError.toString())
                            }
                        })
                        .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
                    adLoader.loadAd(AdRequest.Builder().build())
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
        }
    }

    class ADType {
        companion object {
            val DEFAULT_AD = "6"
            val SMALL = "3"
            val MEDIUM = "4"
            val BIGV1 = "1"
            val BIGV2 = "5"
            val BIGV3 = "2"
        }
    }

    fun preloadAds(layoutInflater: LayoutInflater, context: Context) {
        preloadNativeAdList?.keys?.iterator()?.forEach {
            val preloadNativeAds = preloadNativeAdList!![it]
            if (preloadNativeAds != null && preloadNativeAds.ad == null) {
                preLoadNativeAd(
                    layoutInflater,
                    context,
                    adName = preloadNativeAds.adName,
                    mediaMaxHeight = preloadNativeAds.mediaMaxHeight,
                    adUnit = preloadNativeAds.adId,
                    isAdmanager = preloadNativeAds.isAdmanager,
                    loadTimeOut = preloadNativeAds.loadTimeOut
                )
            }
        }
    }

    fun preloadAds(application: Application, preloadingNativeAdList: HashMap<String, PreloadNativeAds>){
        val context = application?.applicationContext!!
        val inflater = LayoutInflater.from(application)
        preloadNativeAdList = preloadingNativeAdList
        if (preloadNativeAdList != null && inflater != null) {
            preloadAds(inflater, context)
        }
    }

    internal fun preLoadNativeAd(
        layoutInflater: LayoutInflater,
        context: Context,
        adUnit: String,
        adName: String,
        mediaMaxHeight: Int = 300,
        populator: ((nativeAd: NativeAd, adView: NativeAdView) -> Unit)? = null,
        contentURL: String? = null,
        neighbourContentURL: List<String>? = null,
        isAdmanager: Boolean,
        loadTimeOut:Int
    ) {
        Log.d("preLoadNativeAd: ", adUnit+"-"+adName)
        if (context != null){
            if (adUnit != "STOP") {
                val preloadNativeAds = preloadNativeAdList?.get(adName)
                @LayoutRes val layoutId = R.layout.main_ad_template_view
                if (adUnit.isBlank()) return


                Log.d("preload_native", "onStart" + System.currentTimeMillis()/1000)
                preLoadNativeAd(
                    adName,
                    context,
                    contentURL = contentURL,
                    neighbourContentURL = neighbourContentURL,
                    listOf(adUnit),
                    loadTimeOut,
                    object : NativeInternalCallback {
                        override fun onSuccess(nativeAd: NativeAd?) {
                            Log.d("preload_native", "onSuccess: else Fallback Shown" + System.currentTimeMillis()/1000)
                            if (nativeAd != null) {
                                if (nativeAd != null) {
                                    val adView = layoutInflater.inflate(layoutId, null)
                                            as NativeAdView
                                    if (populator != null) {
                                        populator.invoke(nativeAd!!, adView)
                                    } else {
                                        populateUnifiedNativeAdView(
                                            nativeAd!!,
                                            adView,
                                        )
                                    }
                                    if (preloadNativeAds != null) {
                                        preloadNativeAds.ad = adView
                                    }
                                }
                            }
                        }

                        override fun onFailure(loadAdError: LoadAdError?) {

                        }
                    },
                    isAdmanager = isAdmanager
                )
            }
        }
        else {
            Log.d("preLoadNativeAd:contextNull", adUnit+"-"+adName)
        }
    }

    internal fun preLoadNativeAd(
        adName: String,
        context: Context,
        contentURL: String? = null,
        neighbourContentURL: List<String>? = null,
        primaryIds: List<String>,
        fetchedTimer: Int,
        nativeInternalCallback: NativeInternalCallback,
        isAdmanager: Boolean
    ) {
        var nativeAd: NativeAd? = null
        var loadAdError: LoadAdError? = null
        object : CountDownTimer(fetchedTimer.toLong(), 500) {
            override fun onTick(millisUntilFinished: Long) {
                if (nativeAd != null) {
                    nativeInternalCallback.onSuccess(nativeAd)
                    this.cancel()
                }
            }

            override fun onFinish() {
                if (nativeAd != null) {
                    nativeInternalCallback.onSuccess(nativeAd)
                }
                else
                    nativeInternalCallback.onFailure(loadAdError)
            }
        }.start()
        var loadedId = ""
        for (adUnit in primaryIds){
            val adLoader: AdLoader? = AdLoader.Builder(context, adUnit)
                .forNativeAd { ad: NativeAd ->
                    if (nativeAd == null && ad != null) {
                        nativeAd = ad
                        loadedId = adUnit
                    }
                }
                .withAdListener(object : AdListener() {

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        loadAdError = adError
                        Log.d(TAG, "onAdFailedToLoad: "+adName+" : "+adUnit+" : "+adError.message)

                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .setRequestCustomMuteThisAd(true)
                        .build()
                )
                .build()
            loadAd(adLoader, contentURL, neighbourContentURL)
        }

    }

    private fun loadAd(
        adLoader: AdLoader?,
        contentURL: String?,
        neighbourContentURL: List<String>?
    ) {
        val builder = AdRequest.Builder().addNetworkExtrasBundle(
            AdMobAdapter::class.java,
            getConsentEnabledBundle()
        )
        contentURL?.let { builder.setContentUrl(it) }
        neighbourContentURL?.let { builder.setNeighboringContentUrls(it) }
        adLoader?.loadAd(
            builder.build()
        )
    }

    val extras = Bundle()
    fun getConsentEnabledBundle(): Bundle {
        return extras
    }

    fun loadBannerAd(mAdView:AdView){
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                print("loaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
                print("failed")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                print("opened")
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                print("clicked")
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                print("closed")
            }
        }
    }
    interface BypassAppOpenAd
}