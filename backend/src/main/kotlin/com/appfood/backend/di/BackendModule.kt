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
import com.appfood.backend.external.RecetteImporter
import com.appfood.backend.search.AlimentIndexer
import com.appfood.backend.search.MeilisearchClient
import com.appfood.backend.security.EncryptionService
import com.appfood.backend.service.AlimentService
import com.appfood.backend.service.AuthService
import com.appfood.backend.service.ConsentService
import com.appfood.backend.service.DashboardService
import com.appfood.backend.service.HydratationService
import com.appfood.backend.service.JournalService
import com.appfood.backend.service.NotificationService
import com.appfood.backend.service.PoidsService
import com.appfood.backend.service.PortionService
import com.appfood.backend.service.ProfileService
import com.appfood.backend.service.QuotaService
import com.appfood.backend.service.RecetteService
import com.appfood.backend.service.RecommandationService
import com.appfood.backend.service.SocialProfileService
import com.appfood.backend.service.SupportService
import com.appfood.backend.service.SyncService
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun backendModule(
    meilisearchUrl: String,
    meilisearchApiKey: String,
    jwtSecret: String = "",
    jwtIssuer: String = "",
    jwtAudience: String = "",
    encryptionKeyBase64: String? = null,
) = module {
    // Security — encryption for sensitive data at rest
    single { EncryptionService(encryptionKeyBase64) }

    // HTTP Client
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10_000
                connectTimeoutMillis = 5_000
                socketTimeoutMillis = 10_000
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
    single { RecetteImporter(get(), get()) }
    single { OpenFoodFactsClient(get(), get(), get()) }

    // Services
    single {
        AuthService(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            jwtSecret = jwtSecret,
            jwtIssuer = jwtIssuer,
            jwtAudience = jwtAudience,
        )
    }
    single { ProfileService(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { SocialProfileService(get()) }
    single { AlimentService(get(), get(), get(), get()) }
    single { JournalService(get(), get(), get()) }
    single { QuotaService(get(), get(), get()) }
    single { PortionService(get()) }
    single { RecommandationService(get(), get(), get(), get(), get()) }
    single { DashboardService(get(), get(), get(), get(), get()) }
    single { RecetteService(get(), get(), get()) }
    single { HydratationService(get(), get()) }
    single { PoidsService(get()) }
    single { NotificationService(get(), get()) }
    single { ConsentService(get()) }
    single { SupportService(get()) }
    single { SyncService(get(), get(), get()) }

    // DAOs
    single { UserDao() }
    single { UserProfileDao(get()) }
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
