package com.appyhigh.adutils.models

import com.example.advertise.AdsManager
import com.google.android.gms.ads.nativead.NativeAdView

data class PreloadNativeAds(
    val adId: String,
    val adName:String,
    val adSize: String = AdsManager.ADType.DEFAULT_AD,
    var ad: NativeAdView? = null,
    val mediaMaxHeight: Int = 300,
    val loadingTextSize: Int = 24,
    val isAdmanager: Boolean = true,
    val loadTimeOut:Int
)
