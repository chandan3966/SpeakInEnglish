package com.example.speakinenglish.initilizers

import android.content.Context
import androidx.startup.Initializer
import com.example.advertise.AdsManager

class AdsInitializer : Initializer<AdsInitializer> {
    override fun create(context: Context): AdsInitializer {
        AdsManager.initialize(context)
        return this
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}