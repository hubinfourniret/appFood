package com.appfood.shared.di

import com.appfood.shared.data.impl.HydratationRepositoryImpl
import com.appfood.shared.data.impl.PoidsRepositoryImpl
import com.appfood.shared.data.impl.QuotaRepositoryImpl
import com.appfood.shared.data.impl.RecetteRepositoryImpl
import com.appfood.shared.data.impl.RecommandationRepositoryImpl
import com.appfood.shared.data.impl.UserRepositoryImpl
import com.appfood.shared.data.local.DatabaseDriverFactory
import com.appfood.shared.data.local.LocalAlimentDataSource
import com.appfood.shared.data.local.LocalHydratationDataSource
import com.appfood.shared.data.local.LocalJournalDataSource
import com.appfood.shared.data.local.LocalPoidsDataSource
import com.appfood.shared.data.local.LocalPortionDataSource
import com.appfood.shared.data.local.LocalQuotaDataSource
import com.appfood.shared.data.local.LocalSyncQueueDataSource
import com.appfood.shared.data.local.LocalUserDataSource
import com.appfood.shared.data.remote.AuthApi
import com.appfood.shared.data.remote.HydratationApi
import com.appfood.shared.data.remote.PoidsApi
import com.appfood.shared.data.remote.QuotaApi
import com.appfood.shared.data.remote.RecetteApi
import com.appfood.shared.data.remote.RecommandationApi
import com.appfood.shared.data.remote.SyncApi
import com.appfood.shared.data.remote.UserApi
import com.appfood.shared.data.repository.HydratationRepository
import com.appfood.shared.data.repository.PoidsRepository
import com.appfood.shared.data.repository.QuotaRepository
import com.appfood.shared.data.repository.RecetteRepository
import com.appfood.shared.data.repository.RecommandationRepository
import com.appfood.shared.data.repository.UserRepository
import com.appfood.shared.db.AppDatabase
import com.appfood.shared.domain.hydratation.AjouterEauUseCase
import com.appfood.shared.domain.hydratation.GetHydratationJourUseCase
import com.appfood.shared.domain.hydratation.UpdateObjectifHydratationUseCase
import com.appfood.shared.domain.poids.DetecterChangementPoidsUseCase
import com.appfood.shared.domain.poids.EnregistrerPoidsUseCase
import com.appfood.shared.domain.poids.GetHistoriquePoidsUseCase
import com.appfood.shared.domain.quota.CalculerQuotasUseCase
import com.appfood.shared.domain.quota.RecalculerQuotasUseCase
import com.appfood.shared.domain.recette.RechercherRecettesUseCase
import com.appfood.shared.domain.recommandation.RecommandationAlimentUseCase
import com.appfood.shared.sync.ConnectivityMonitor
import com.appfood.shared.sync.SyncManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Module Koin partage — enregistre les use cases, repositories et data sources.
 * Rempli progressivement au fil des US.
 *
 * IMPORTANT : DatabaseDriverFactory doit etre enregistre dans le module platform-specific
 * (androidModule ou iosModule) car il depend du contexte natif (Context sur Android).
 *
 * IMPORTANT : ApiClient doit etre enregistre dans le module platform-specific
 * car il depend de la configuration Ktor HttpClient (engine, base URL).
 */
val sharedModule = module {
    // Base de donnees locale
    single { get<DatabaseDriverFactory>().createDriver() }
    single { AppDatabase(get()) }

    // Remote API clients
    // ApiClient must be registered in the platform-specific module
    single { AuthApi(get()) }
    single { UserApi(get()) }
    single { QuotaApi(get()) }
    single { RecommandationApi(get()) }
    single { SyncApi(get()) }
    single { HydratationApi(get()) }
    single { PoidsApi(get()) }
    single { RecetteApi(get()) }

    // Sync infrastructure
    // ConnectivityMonitor is registered in the platform-specific module (expect/actual)
    single { SyncManager(get(), get(), get()) }

    // Use cases (factory — new instance per injection)
    factory { CalculerQuotasUseCase() }
    factory { RecalculerQuotasUseCase(get(), get(), get()) }
    factory { RecommandationAlimentUseCase() }
    factory { AjouterEauUseCase(get()) }
    factory { GetHydratationJourUseCase(get()) }
    factory { UpdateObjectifHydratationUseCase(get()) }
    factory { EnregistrerPoidsUseCase(get()) }
    factory { GetHistoriquePoidsUseCase(get()) }
    factory { DetecterChangementPoidsUseCase(get()) }
    factory { RechercherRecettesUseCase(get()) }

    // Repositories
    single<UserRepository> { UserRepositoryImpl(get(), get(), get()) }
    single<QuotaRepository> { QuotaRepositoryImpl(get(), get()) }
    single<RecommandationRepository> { RecommandationRepositoryImpl(get()) }
    single<HydratationRepository> { HydratationRepositoryImpl(get(), get(), get()) }
    single<PoidsRepository> { PoidsRepositoryImpl(get(), get(), get()) }
    single<RecetteRepository> { RecetteRepositoryImpl(get()) }

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
