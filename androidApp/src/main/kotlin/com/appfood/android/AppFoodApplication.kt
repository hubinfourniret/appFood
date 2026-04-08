package com.appfood.android

import android.app.Application
import com.appfood.shared.data.local.DatabaseDriverFactory
import com.appfood.shared.di.sharedModule
import com.appfood.shared.sync.ConnectivityMonitor
import com.google.firebase.FirebaseApp
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
            single {
                HttpClient(OkHttp) {
                    install(ContentNegotiation) {
                        json(Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            encodeDefaults = true
                        })
                    }
                }
            }
            single { ConnectivityMonitor() }
        }

        startKoin {
            androidContext(this@AppFoodApplication)
            modules(androidModule, sharedModule)
        }
    }
}
