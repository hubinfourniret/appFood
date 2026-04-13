package com.appfood.shared.ui.journal

import com.appfood.shared.api.request.AddJournalEntryRequest
import com.appfood.shared.api.request.UpdateJournalEntryRequest
import com.appfood.shared.api.response.AlimentResponse
import com.appfood.shared.api.response.DailySummaryResponse
import com.appfood.shared.api.response.JournalEntryResponse
import com.appfood.shared.api.response.JournalListResponse
import com.appfood.shared.api.response.NutrimentValuesResponse
import com.appfood.shared.api.response.PortionListResponse
import com.appfood.shared.api.response.PortionResponse
import com.appfood.shared.api.response.SearchAlimentResponse
import com.appfood.shared.api.request.CreateRecetteRequest
import com.appfood.shared.data.repository.AlimentRepository
import com.appfood.shared.data.repository.JournalRepository
import com.appfood.shared.data.repository.RecetteRepository
import com.appfood.shared.model.Aliment
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.Recette
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.SourceAliment
import com.appfood.shared.sync.SyncEnqueuer
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ====================== FAKE DATA ======================

private val FAKE_NUTRIMENT_VALUES_RESPONSE = NutrimentValuesResponse(
    calories = 144.0,
    proteines = 15.0,
    glucides = 2.0,
    lipides = 8.5,
    fibres = 1.0,
    sel = 0.01,
    sucres = 0.5,
    fer = 5.4,
    calcium = 350.0,
    zinc = 1.0,
    magnesium = 30.0,
    vitamineB12 = 0.0,
    vitamineD = 0.0,
    vitamineC = 0.0,
    omega3 = 0.4,
    omega6 = 4.5,
)

private val FAKE_JOURNAL_ENTRY_RESPONSE = JournalEntryResponse(
    id = "entry-001",
    date = "2026-04-10",
    mealType = "DEJEUNER",
    alimentId = "test-aliment-001",
    recetteId = null,
    nom = "Tofu ferme nature",
    quantiteGrammes = 150.0,
    nbPortions = null,
    nutrimentsCalcules = FAKE_NUTRIMENT_VALUES_RESPONSE,
    createdAt = "2026-04-10T12:00:00Z",
    updatedAt = "2026-04-10T12:00:00Z",
)

private val FAKE_ALIMENT = Aliment(
    id = "test-aliment-001",
    nom = "Tofu ferme nature",
    marque = null,
    source = SourceAliment.CIQUAL,
    sourceId = "31045",
    codeBarres = null,
    categorie = "Legumineuses et produits derives",
    regimesCompatibles = listOf(RegimeAlimentaire.VEGAN, RegimeAlimentaire.VEGETARIEN),
    nutrimentsPour100g = NutrimentValues(
        calories = 144.0,
        proteines = 15.0,
        glucides = 2.0,
        lipides = 8.5,
        fibres = 1.0,
        sel = 0.01,
        sucres = 0.5,
        fer = 5.4,
        calcium = 350.0,
        zinc = 1.0,
        magnesium = 30.0,
        vitamineB12 = 0.0,
        vitamineD = 0.0,
        vitamineC = 0.0,
        omega3 = 0.4,
        omega6 = 4.5,
    ),
    portionsStandard = emptyList(),
)

private val FAKE_ALIMENT_RESPONSE = AlimentResponse(
    id = "test-aliment-001",
    nom = "Tofu ferme nature",
    marque = null,
    source = "CIQUAL",
    sourceId = "31045",
    codeBarres = null,
    categorie = "Legumineuses et produits derives",
    regimesCompatibles = listOf("VEGAN", "VEGETARIEN"),
    nutrimentsPour100g = FAKE_NUTRIMENT_VALUES_RESPONSE,
    portionsStandard = emptyList(),
)

// ====================== FAKES ======================

class FakeJournalRepository : JournalRepository {
    var addEntryResult: AppResult<JournalEntryResponse> = AppResult.Success(FAKE_JOURNAL_ENTRY_RESPONSE)
    var addEntryCalled = false
    var lastAddEntryRequest: AddJournalEntryRequest? = null

    override suspend fun getEntries(date: String?, mealType: String?): AppResult<JournalListResponse> {
        return AppResult.Success(JournalListResponse(data = emptyList(), total = 0))
    }

    override suspend fun addEntry(request: AddJournalEntryRequest): AppResult<JournalEntryResponse> {
        addEntryCalled = true
        lastAddEntryRequest = request
        return addEntryResult
    }

    override suspend fun updateEntry(id: String, request: UpdateJournalEntryRequest): AppResult<JournalEntryResponse> {
        return AppResult.Success(FAKE_JOURNAL_ENTRY_RESPONSE)
    }

    override suspend fun deleteEntry(id: String): AppResult<Unit> {
        return AppResult.Success(Unit)
    }

    override suspend fun getDailySummary(date: String?): AppResult<DailySummaryResponse> {
        return AppResult.Success(
            DailySummaryResponse(
                date = "2026-04-10",
                totalNutriments = FAKE_NUTRIMENT_VALUES_RESPONSE,
                parRepas = emptyMap(),
                nbEntrees = 0,
            )
        )
    }

    override suspend fun getRecents(limit: Int?): AppResult<JournalListResponse> {
        return AppResult.Success(JournalListResponse(data = emptyList(), total = 0))
    }

    override suspend fun getFavoris(): AppResult<JournalListResponse> {
        return AppResult.Success(JournalListResponse(data = emptyList(), total = 0))
    }

    override suspend fun addFavori(alimentId: String): AppResult<Unit> {
        return AppResult.Success(Unit)
    }

    override suspend fun removeFavori(alimentId: String): AppResult<Unit> {
        return AppResult.Success(Unit)
    }
}

class FakeAlimentRepository : AlimentRepository {
    override suspend fun search(query: String, regime: String?, page: Int?, size: Int?): AppResult<SearchAlimentResponse> {
        return AppResult.Success(SearchAlimentResponse(data = listOf(FAKE_ALIMENT_RESPONSE), total = 1, query = query))
    }

    override suspend fun getById(id: String): AppResult<AlimentResponse> {
        return AppResult.Success(FAKE_ALIMENT_RESPONSE)
    }

    override suspend fun getByBarcode(code: String): AppResult<AlimentResponse> {
        return AppResult.Success(FAKE_ALIMENT_RESPONSE)
    }

    override suspend fun getPortions(alimentId: String?): AppResult<PortionListResponse> {
        return AppResult.Success(PortionListResponse(data = emptyList(), total = 0))
    }
}

class FakeRecetteRepository : RecetteRepository {
    override suspend fun listRecettes(
        regime: String?,
        typeRepas: String?,
        sort: String?,
        query: String?,
        page: Int,
        limit: Int,
    ): AppResult<List<Recette>> {
        return AppResult.Success(emptyList())
    }

    override suspend fun getRecette(id: String): AppResult<Recette> {
        return AppResult.Error(code = "NOT_FOUND", message = "Not found")
    }

    override suspend fun createRecette(request: CreateRecetteRequest): AppResult<Recette> {
        return AppResult.Error(code = "NOT_IMPLEMENTED", message = "Not implemented")
    }
}

class FakeSyncEnqueuer : SyncEnqueuer {
    var enqueueCalled = false
    var lastEntityType: String? = null
    var lastAction: String? = null
    var enqueueCount = 0

    override fun enqueue(entityType: String, entityId: String, action: String, payloadJson: String) {
        enqueueCalled = true
        lastEntityType = entityType
        lastAction = action
        enqueueCount++
    }
}

// ====================== TESTS ======================

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var journalRepository: FakeJournalRepository
    private lateinit var alimentRepository: FakeAlimentRepository
    private lateinit var recetteRepository: FakeRecetteRepository
    private lateinit var syncEnqueuer: FakeSyncEnqueuer
    private lateinit var viewModel: JournalViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        journalRepository = FakeJournalRepository()
        alimentRepository = FakeAlimentRepository()
        recetteRepository = FakeRecetteRepository()
        syncEnqueuer = FakeSyncEnqueuer()
        viewModel = JournalViewModel(
            journalRepository = journalRepository,
            alimentRepository = alimentRepository,
            recetteRepository = recetteRepository,
            syncManager = syncEnqueuer,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- Test 1 : Flow complet ajout aliment reussi ----------

    @Test
    fun `ajout aliment reussi - flow complet SelectMeal vers Saved`() = runTest {
        // 1. init
        viewModel.init()
        advanceUntilIdle()

        // 2. Etat initial
        assertEquals(AddEntryState.SelectMeal, viewModel.addEntryState.value)

        // 3. Selection du type de repas
        viewModel.onMealTypeSelected(MealType.DEJEUNER)
        assertEquals(AddEntryState.SearchFood, viewModel.addEntryState.value)

        // 4. Selection d'un aliment
        viewModel.onAlimentSelected(FAKE_ALIMENT)
        assertEquals(AddEntryState.SelectPortion, viewModel.addEntryState.value)
        assertEquals(FAKE_ALIMENT, viewModel.selectedAliment.value)

        // 5. Modification de la quantite
        viewModel.onQuantityChanged(150.0)
        assertEquals(150.0, viewModel.quantityGrams.value)

        // 6. Validation
        viewModel.onValidateEntry()

        // L'etat passe a Saving immediatement
        assertEquals(AddEntryState.Saving, viewModel.addEntryState.value)

        // Attendre la coroutine de journalRepository.addEntry
        advanceUntilIdle()

        // Etat final : Saved (succes API)
        assertEquals(AddEntryState.Saved, viewModel.addEntryState.value)
        assertTrue(journalRepository.addEntryCalled, "journalRepository.addEntry aurait du etre appele")

        // En cas de succes, enqueue ne doit PAS etre appele (pas besoin de sync offline)
        assertTrue(!syncEnqueuer.enqueueCalled, "syncManager.enqueue ne devrait pas etre appele en cas de succes API")

        // Verifier le contenu de la requete
        val request = journalRepository.lastAddEntryRequest!!
        assertEquals("DEJEUNER", request.mealType)
        assertEquals("test-aliment-001", request.alimentId)
        assertEquals(150.0, request.quantiteGrammes)
        assertEquals("Tofu ferme nature", request.nom)
    }

    // ---------- Test 2 : Flow ajout aliment avec erreur reseau ----------

    @Test
    fun `ajout aliment avec erreur reseau - bascule en SavedOffline`() = runTest {
        // Configurer le repository pour retourner une erreur
        journalRepository.addEntryResult = AppResult.Error(
            code = "NETWORK_ERROR",
            message = "Connection refused",
        )

        viewModel.init()
        advanceUntilIdle()

        // Parcourir le flow
        viewModel.onMealTypeSelected(MealType.DEJEUNER)
        viewModel.onAlimentSelected(FAKE_ALIMENT)
        viewModel.onQuantityChanged(150.0)
        viewModel.onValidateEntry()

        advanceUntilIdle()

        // Etat final : SavedOffline (erreur reseau, fallback offline)
        assertEquals(
            AddEntryState.SavedOffline,
            viewModel.addEntryState.value,
            "L'etat devrait etre SavedOffline apres une erreur reseau"
        )
        assertTrue(journalRepository.addEntryCalled)

        // Enqueue doit etre appele en cas d'erreur (offline fallback)
        assertTrue(syncEnqueuer.enqueueCalled, "syncManager.enqueue aurait du etre appele en cas d'erreur reseau")
        assertEquals("journal", syncEnqueuer.lastEntityType)
        assertEquals("CREATE", syncEnqueuer.lastAction)
    }

    // ---------- Test 3 : Reset apres validation ----------

    @Test
    fun `resetAddEntryFlow remet tout a zero apres validation`() = runTest {
        viewModel.init()
        advanceUntilIdle()

        // Completer le flow
        viewModel.onMealTypeSelected(MealType.DEJEUNER)
        viewModel.onAlimentSelected(FAKE_ALIMENT)
        viewModel.onQuantityChanged(150.0)
        viewModel.onValidateEntry()
        advanceUntilIdle()
        assertEquals(AddEntryState.Saved, viewModel.addEntryState.value)

        // Reset
        viewModel.resetAddEntryFlow()

        // Verifications
        assertEquals(AddEntryState.SelectMeal, viewModel.addEntryState.value)
        assertNull(viewModel.selectedMealType.value, "selectedMealType devrait etre null apres reset")
        assertNull(viewModel.selectedAliment.value, "selectedAliment devrait etre null apres reset")
        assertEquals(100.0, viewModel.quantityGrams.value, "quantityGrams devrait revenir a la valeur par defaut (100g)")
        assertNull(viewModel.selectedPortion.value, "selectedPortion devrait etre null apres reset")
        assertEquals("", viewModel.searchQuery.value, "searchQuery devrait etre vide apres reset")
    }

    // ---------- Test 4 : Validation bloquee si pas de meal type ----------

    @Test
    fun `onValidateEntry sans meal type ne declenche pas de sauvegarde`() = runTest {
        viewModel.init()
        advanceUntilIdle()

        // Ne PAS appeler onMealTypeSelected
        // Selectionner un aliment directement (forcer via onAlimentSelected)
        // Note: en realite l'UI empecherait ca, mais on teste la guard du ViewModel

        viewModel.onValidateEntry()
        advanceUntilIdle()

        // L'etat ne doit PAS etre Saving ou Saved
        assertNotEquals(AddEntryState.Saving, viewModel.addEntryState.value)
        assertNotEquals(AddEntryState.Saved, viewModel.addEntryState.value)
        assertNotEquals(AddEntryState.SavedOffline, viewModel.addEntryState.value)

        // Le repository ne doit pas avoir ete appele
        assertTrue(!journalRepository.addEntryCalled, "addEntry ne devrait pas etre appele sans meal type")
        assertTrue(!syncEnqueuer.enqueueCalled, "enqueue ne devrait pas etre appele sans meal type")
    }

    // ---------- Test 5 : Validation bloquee si pas d'aliment ----------

    @Test
    fun `onValidateEntry avec meal type mais sans aliment ne declenche pas de sauvegarde`() = runTest {
        viewModel.init()
        advanceUntilIdle()

        viewModel.onMealTypeSelected(MealType.DEJEUNER)
        assertEquals(AddEntryState.SearchFood, viewModel.addEntryState.value)

        // Ne PAS appeler onAlimentSelected
        viewModel.onValidateEntry()
        advanceUntilIdle()

        // L'etat ne doit PAS etre Saving ou Saved
        assertNotEquals(AddEntryState.Saving, viewModel.addEntryState.value)
        assertNotEquals(AddEntryState.Saved, viewModel.addEntryState.value)
        assertNotEquals(AddEntryState.SavedOffline, viewModel.addEntryState.value)

        // Le repository ne doit pas avoir ete appele
        assertTrue(!journalRepository.addEntryCalled, "addEntry ne devrait pas etre appele sans aliment")
        assertTrue(!syncEnqueuer.enqueueCalled, "enqueue ne devrait pas etre appele sans aliment")
    }
}
