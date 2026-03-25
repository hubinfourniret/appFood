package com.appfood.android

import android.app.Application
import com.appfood.shared.data.local.DatabaseDriverFactory
import com.appfood.shared.di.sharedModule
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AppFoodApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialisation Firebase (lit automatiquement google-services.json)
        Firebase.initialize(this)

        val androidModule = module {
            single { DatabaseDriverFactory(this@AppFoodApplication) }
        }

        startKoin {
            androidContext(this@AppFoodApplication)
            modules(androidModule, sharedModule)
        }
    }
}
