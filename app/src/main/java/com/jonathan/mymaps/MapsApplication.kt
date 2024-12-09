package com.jonathan.mymaps

import android.app.Application
import com.jonathan.mymaps.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class MapsApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            workManagerFactory()
            modules(appModules)
        }
    }
}
