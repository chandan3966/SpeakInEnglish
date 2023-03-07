package com.example.speakinenglish.initilizers

import android.content.Context
import androidx.startup.Initializer
import com.example.analytics.AnalyticsManager

class AnalyticInitializer : Initializer<AnalyticsManager> {
    override fun create(context: Context): AnalyticsManager {
        AnalyticsManager.initialize(context)
        return AnalyticsManager
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
