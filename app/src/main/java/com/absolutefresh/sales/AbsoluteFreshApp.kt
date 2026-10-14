package com.absolutefresh.sales

import android.app.Application
import com.absolutefresh.sales.data.di.AppContainer

class AbsoluteFreshApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.initializeSync()
    }
}
