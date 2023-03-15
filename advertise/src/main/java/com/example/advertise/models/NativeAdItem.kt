package com.appyhigh.adutils.models

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.appyhigh.adutils.callbacks.NativeAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

data class NativeAdItem(
    val application: Context,
    val id: Long,
    val lifecycle: Lifecycle,
    val adUnit: String,
    val adName:String,
    val viewGroup: ViewGroup,
    val nativeAdLoadCallback: NativeAdLoadCallback?,
    val layoutId: Int,
    val populator: ((nativeAd: NativeAd, adView: NativeAdView) -> Unit)? = null,
    var viewId: String = "1",
    var background: Any? = null,
    var textColor1: Int? = null,
    var textColor2: Int? = null,
    var mediaMaxHeight: Int = 300,
    var textSize: Int = 48,
    var buttonColor: Int = Color.parseColor("#000000"),
    var contentURL: String? = null,
    var neighbourContentURL: List<String>? = null,
    var showLoadingMessage: Boolean,
    var isAdManager:Boolean


)
