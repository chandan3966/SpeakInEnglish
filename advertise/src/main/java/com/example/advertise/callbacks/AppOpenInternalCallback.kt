package com.example.advertise.callbacks

import com.google.android.gms.ads.appopen.AppOpenAd

interface AppOpenInternalCallback {
    fun onSuccess(ad: AppOpenAd)
    fun onFailed()
}