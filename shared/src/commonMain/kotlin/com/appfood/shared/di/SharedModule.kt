package com.appfood.shared.di

import com.appfood.shared.data.local.DatabaseDriverFactory
import com.appfood.shared.data.local.LocalAlimentDataSource
import com.appfood.shared.data.local.LocalHydratationDataSource
import com.appfood.shared.data.local.LocalJournalDataSource
import com.appfood.shared.data.local.LocalPoidsDataSource
import com.appfood.shared.data.local.LocalPortionDataSource
import com.appfood.shared.data.local.LocalQuotaDataSource
import com.appfood.shared.data.local.LocalSyncQueueDataSource
import com.appfood.shared.data.local.LocalUserDataSource
import com.appfood.shared.db.AppDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Module Koin partage — enregistre les use cases, repositories et data sources.
 * Rempli progressivement au fil des US.
 *
 * IMPORTANT : DatabaseDriverFactory doit etre enregistre dans le module platform-specific
 * (androidModule ou iosModule) car il depend du contexte natif (Context sur Android).
 */
val sharedModule = module {
    // Base de donnees locale
    single { get<DatabaseDriverFactory>().createDriver() }
    single { AppDatabase(get()) }

    // Use cases (factory)
    // ex: factory { CalculerQuotasUseCase(get(), get()) }

    // Repositories (singleOf)
    // ex: singleOf(::AlimentRepositoryImpl) { bind<AlimentRepository>() }

    // Data sources locales (SQLDelight)
    singleOf(::LocalJournalDataSource)
    singleOf(::LocalAlimentDataSource)
    singleOf(::LocalPoidsDataSource)
    singleOf(::LocalHydratationDataSource)
    singleOf(::LocalQuotaDataSource)
    singleOf(::LocalUserDataSource)
    singleOf(::LocalPortionDataSource)
    singleOf(::LocalSyncQueueDataSource)
}
