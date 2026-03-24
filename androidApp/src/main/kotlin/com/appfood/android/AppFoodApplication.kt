package com.appfood.android

import android.app.Application
import com.appfood.shared.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppFoodApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AppFoodApplication)
            modules(sharedModule)
        }
    }
}
