package com.example.advertise

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.LayoutRes
import com.example.advertise.callbacks.AdCallbacks
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
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

    @Synchronized
    fun initialize(context: Context) {
        try {
            sAppContext = context
            if (BuildConfig.DEBUG){
                val testDeviceIds = Arrays.asList(
                    Settings.Secure.getString(
                    sAppContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID))
                val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
                MobileAds.setRequestConfiguration(configuration)
            }
            MobileAds.initialize(context,
                OnInitializationCompleteListener {
                    Log.i(TAG, "SDK Initialized")
                }
            )

        } catch (e: Exception) {
            e.printStackTrace()
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
}