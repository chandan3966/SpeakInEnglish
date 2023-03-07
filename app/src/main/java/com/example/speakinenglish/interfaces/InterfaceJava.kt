package com.example.speakinenglish.interfaces

import android.webkit.JavascriptInterface
import com.example.speakinenglish.activity.CallerActivity
import com.example.speakinenglish.fragment.CallingFragment

class InterfaceJava(callingFragment: CallerActivity) {
    var callActivity: CallerActivity? = null

    init {
        this.callActivity = callingFragment
    }

    @JavascriptInterface
    fun onPeerConnected() {
        callActivity?.onPeerConnected()
    }

    @JavascriptInterface
    fun onPeerDisconnected() {
        callActivity?.onPeerDisconnected()
    }
}