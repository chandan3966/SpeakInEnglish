package com.example.advertise.callbacks

import com.example.advertise.AppOpenManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

abstract class AppOpenAdCallback : AppOpenAdLoadCallback() {
    open fun onInitSuccess(manager: AppOpenManager) {}
}

abstract class AppOpenAdLoadCallback {
    open fun onAdLoaded(ad: AppOpenAd) {}
    open fun onAdFailedToLoad(loadAdError: LoadAdError? = null) {}
    open fun onAdFailedToShow(adError: AdError) {}
    open fun onAdClosed() {}
}