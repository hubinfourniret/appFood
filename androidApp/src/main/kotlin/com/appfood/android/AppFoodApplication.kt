package com.appfood.android

import android.app.Application
import com.appfood.shared.data.local.DatabaseDriverFactory
import com.appfood.shared.di.sharedModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AppFoodApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialisation Firebase (lit automatiquement google-services.json)
        FirebaseApp.initializeApp(this)

        val androidModule = module {
            single { DatabaseDriverFactory(this@AppFoodApplication) }
        }

        startKoin {
            androidContext(this@AppFoodApplication)
            modules(androidModule, sharedModule)
        }
    }
}
