package com.appfood.backend.di

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.ConsentDao
import com.appfood.backend.database.dao.FaqDao
import com.appfood.backend.database.dao.FcmTokenDao
import com.appfood.backend.database.dao.HydratationDao
import com.appfood.backend.database.dao.JournalEntryDao
import com.appfood.backend.database.dao.NotificationDao
import com.appfood.backend.database.dao.PoidsHistoryDao
import com.appfood.backend.database.dao.PortionDao
import com.appfood.backend.database.dao.QuotaDao
import com.appfood.backend.database.dao.RecetteDao
import com.appfood.backend.database.dao.UserDao
import com.appfood.backend.database.dao.UserPreferencesDao
import com.appfood.backend.database.dao.UserProfileDao
import com.appfood.backend.external.CiqualImporter
import com.appfood.backend.external.FirebaseAdmin
import com.appfood.backend.external.OpenFoodFactsClient
import com.appfood.backend.search.AlimentIndexer
import com.appfood.backend.search.MeilisearchClient
import com.appfood.backend.service.AuthService
import com.appfood.backend.service.ProfileService
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun backendModule(meilisearchUrl: String, meilisearchApiKey: String) = module {
    // HTTP Client
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    // Meilisearch
    single { MeilisearchClient(get(), meilisearchUrl, meilisearchApiKey) }
    single { AlimentIndexer(get(), get()) }

    // Firebase
    single { FirebaseAdmin() }

    // External data sources
    single { CiqualImporter(get(), get()) }
    single { OpenFoodFactsClient(get(), get(), get()) }

    // Services
    single { AuthService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { ProfileService(get(), get(), get(), get(), get(), get(), get(), get()) }

    // DAOs
    single { UserDao() }
    single { UserProfileDao() }
    single { UserPreferencesDao() }
    single { AlimentDao() }
    single { PortionDao() }
    single { RecetteDao() }
    single { JournalEntryDao() }
    single { QuotaDao() }
    single { PoidsHistoryDao() }
    single { HydratationDao() }
    single { NotificationDao() }
    single { FcmTokenDao() }
    single { ConsentDao() }
    single { FaqDao() }
}
