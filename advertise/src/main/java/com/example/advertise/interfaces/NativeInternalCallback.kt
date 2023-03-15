package com.example.advertise.interfaces

import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd

interface NativeInternalCallback {
    fun onSuccess(nativeAd: NativeAd?)
    fun onFailure(loadAdError: LoadAdError?)
}