package com.appfood.backend

import com.appfood.backend.database.tables.AlimentsTable
import com.appfood.backend.database.tables.IngredientsTable
import com.appfood.backend.database.tables.RecettesTable
import com.appfood.backend.database.tables.Role
import com.appfood.backend.database.tables.SourceAliment
import com.appfood.backend.database.tables.SourceRecette
import com.appfood.backend.database.tables.UsersTable
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RecetteRoutesTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val adminUserId = "admin-user-001"
    private val adminEmail = "admin@example.com"

    private fun seedTestUser(
        userId: String = TEST_USER_ID,
        email: String = TEST_USER_EMAIL,
        role: Role = Role.USER,
    ) {
        val now = Clock.System.now()
        transaction {
            UsersTable.insert {
                it[UsersTable.id] = userId
                it[UsersTable.email] = email
                it[UsersTable.nom] = "Test"
                it[UsersTable.prenom] = "User"
                it[UsersTable.role] = role
                it[UsersTable.createdAt] = now
                it[UsersTable.updatedAt] = now
            }
        }
    }

    private fun seedTestAliment(alimentId: String = "aliment-001") {
        transaction {
            AlimentsTable.insert {
                it[AlimentsTable.id] = alimentId
                it[AlimentsTable.nom] = "Tofu ferme"
                it[AlimentsTable.marque] = null
                it[AlimentsTable.sourceAliment] = SourceAliment.CIQUAL
                it[AlimentsTable.sourceId] = "ciqual-001"
                it[AlimentsTable.codeBarres] = null
                it[AlimentsTable.categorie] = "Proteines vegetales"
                it[AlimentsTable.regimesCompatibles] = "VEGAN,VEGETARIEN"
                it[AlimentsTable.calories] = 144.0
                it[AlimentsTable.proteines] = 15.0
                it[AlimentsTable.glucides] = 2.0
                it[AlimentsTable.lipides] = 8.5
                it[AlimentsTable.fibres] = 1.2
                it[AlimentsTable.sel] = 0.05
                it[AlimentsTable.sucres] = 0.5
                it[AlimentsTable.fer] = 2.7
                it[AlimentsTable.calcium] = 350.0
                it[AlimentsTable.zinc] = 1.5
                it[AlimentsTable.magnesium] = 60.0
                it[AlimentsTable.vitamineB12] = 0.0
                it[AlimentsTable.vitamineD] = 0.0
                it[AlimentsTable.vitamineC] = 0.0
                it[AlimentsTable.omega3] = 0.5
                it[AlimentsTable.omega6] = 3.0
            }
        }
    }

    private fun seedTestRecette(recetteId: String = "recette-001") {
        val now = Clock.System.now()
        transaction {
            RecettesTable.insert {
                it[RecettesTable.id] = recetteId
                it[RecettesTable.nom] = "Tofu grille aux legumes"
                it[RecettesTable.description] = "Un plat simple et nutritif"
                it[RecettesTable.tempsPreparationMin] = 15
                it[RecettesTable.tempsCuissonMin] = 20
                it[RecettesTable.nbPortions] = 2
                it[RecettesTable.regimesCompatibles] = "VEGAN,VEGETARIEN"
                it[RecettesTable.sourceRecette] = SourceRecette.MANUELLE
                it[RecettesTable.typeRepas] = "DEJEUNER,DINER"
                it[RecettesTable.etapes] = "Couper le tofu|||Griller|||Servir"
                it[RecettesTable.calories] = 288.0
                it[RecettesTable.proteines] = 30.0
                it[RecettesTable.glucides] = 4.0
                it[RecettesTable.lipides] = 17.0
                it[RecettesTable.fibres] = 2.4
                it[RecettesTable.sel] = 0.1
                it[RecettesTable.sucres] = 1.0
                it[RecettesTable.fer] = 5.4
                it[RecettesTable.calcium] = 700.0
                it[RecettesTable.zinc] = 3.0
                it[RecettesTable.magnesium] = 120.0
                it[RecettesTable.vitamineB12] = 0.0
                it[RecettesTable.vitamineD] = 0.0
                it[RecettesTable.vitamineC] = 0.0
                it[RecettesTable.omega3] = 1.0
                it[RecettesTable.omega6] = 6.0
                it[RecettesTable.imageUrl] = null
                it[RecettesTable.publie] = true
                it[RecettesTable.createdAt] = now
                it[RecettesTable.updatedAt] = now
            }
            IngredientsTable.insert {
                it[IngredientsTable.id] = "ingredient-001"
                it[IngredientsTable.recetteId] = recetteId
                it[IngredientsTable.alimentId] = "aliment-001"
                it[IngredientsTable.alimentNom] = "Tofu ferme"
                it[IngredientsTable.quantiteGrammes] = 200.0
            }
        }
    }

    // --- Unauthorized access ---

    @Test
    fun `should return unauthorized when accessing recettes without token`() =
        withTestApp {
            val client = createJsonClient()
            val response = client.get("/api/v1/recettes")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    // --- GET /api/v1/recettes ---

    @Test
    fun `should list recettes successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            seedTestAliment()
            seedTestRecette()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/recettes") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertEquals(1, data.size)
            assertEquals(1, body["total"]!!.jsonPrimitive.int)
            val recette = data[0].jsonObject
            assertEquals("Tofu grille aux legumes", recette["nom"]!!.jsonPrimitive.content)
            assertNotNull(recette["nutrimentsParPortion"])
        }

    @Test
    fun `should return empty list when no recettes exist`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/recettes") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertEquals(0, data.size)
        }

    @Test
    fun `should filter recettes by query`() =
        withTestApp {
            // Given
            seedTestUser()
            seedTestAliment()
            seedTestRecette()
            val client = createJsonClient()
            val token = createTestToken()

            // When — search for "tofu"
            val response =
                client.get("/api/v1/recettes?q=tofu") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertEquals(1, data.size)

            // When — search for something that doesn't exist
            val response2 =
                client.get("/api/v1/recettes?q=pizza") {
                    bearerAuth(token)
                }
            val body2 = json.parseToJsonElement(response2.bodyAsText()).jsonObject
            assertEquals(0, body2["data"]!!.jsonArray.size)
        }

    // --- GET /api/v1/recettes/{id} ---

    @Test
    fun `should get recette detail by id`() =
        withTestApp {
            // Given
            seedTestUser()
            seedTestAliment()
            seedTestRecette()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/recettes/recette-001") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("recette-001", body["id"]!!.jsonPrimitive.content)
            assertEquals("Tofu grille aux legumes", body["nom"]!!.jsonPrimitive.content)
            assertNotNull(body["ingredients"])
            assertNotNull(body["etapes"])
            assertNotNull(body["nutrimentsTotaux"])
            assertNotNull(body["nutrimentsParPortion"])
            assertNotNull(body["createdAt"])
            assertNotNull(body["updatedAt"])

            val ingredients = body["ingredients"]!!.jsonArray
            assertEquals(1, ingredients.size)
            val etapes = body["etapes"]!!.jsonArray
            assertEquals(3, etapes.size)
        }

    @Test
    fun `should return not found for non-existent recette`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/recettes/non-existent-id") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    // --- POST /api/v1/recettes (requires ADMIN) ---

    @Test
    fun `should create recette as admin`() =
        withTestApp {
            // Given
            seedTestUser(userId = adminUserId, email = adminEmail, role = Role.ADMIN)
            seedTestAliment()
            val client = createJsonClient()
            val token = createTestToken(userId = adminUserId)

            // When
            val response =
                client.post("/api/v1/recettes") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{
                            "nom": "Smoothie vert",
                            "description": "Un smoothie nutritif",
                            "tempsPreparationMin": 5,
                            "tempsCuissonMin": 0,
                            "nbPortions": 1,
                            "regimesCompatibles": ["VEGAN"],
                            "typeRepas": ["PETIT_DEJEUNER","COLLATION"],
                            "ingredients": [{"alimentId": "aliment-001", "quantiteGrammes": 100.0}],
                            "etapes": ["Mixer tous les ingredients"],
                            "publie": true
                        }""",
                    )
                }

            // Then
            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("Smoothie vert", body["nom"]!!.jsonPrimitive.content)
            assertNotNull(body["id"])
            assertNotNull(body["ingredients"])
            assertNotNull(body["nutrimentsTotaux"])
        }

    @Test
    fun `should return forbidden when non-admin creates recette`() =
        withTestApp {
            // Given — regular user (admin check in RecetteService)
            seedTestUser()
            seedTestAliment()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/recettes") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{
                            "nom": "Test",
                            "description": "Test",
                            "tempsPreparationMin": 5,
                            "tempsCuissonMin": 0,
                            "nbPortions": 1,
                            "regimesCompatibles": ["VEGAN"],
                            "typeRepas": ["DEJEUNER"],
                            "ingredients": [{"alimentId": "aliment-001", "quantiteGrammes": 100.0}],
                            "etapes": ["Etape 1"],
                            "publie": false
                        }""",
                    )
                }

            // Then — forbidden because user role is USER, not ADMIN
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `should return validation error when creating recette with empty name`() =
        withTestApp {
            // Given
            seedTestUser(userId = adminUserId, email = adminEmail, role = Role.ADMIN)
            seedTestAliment()
            val client = createJsonClient()
            val token = createTestToken(userId = adminUserId)

            // When
            val response =
                client.post("/api/v1/recettes") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{
                            "nom": "",
                            "description": "Test",
                            "tempsPreparationMin": 5,
                            "tempsCuissonMin": 0,
                            "nbPortions": 1,
                            "regimesCompatibles": ["VEGAN"],
                            "typeRepas": ["DEJEUNER"],
                            "ingredients": [{"alimentId": "aliment-001", "quantiteGrammes": 100.0}],
                            "etapes": ["Etape 1"],
                            "publie": false
                        }""",
                    )
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `should return validation error when creating recette with no ingredients`() =
        withTestApp {
            // Given
            seedTestUser(userId = adminUserId, email = adminEmail, role = Role.ADMIN)
            val client = createJsonClient()
            val token = createTestToken(userId = adminUserId)

            // When
            val response =
                client.post("/api/v1/recettes") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{
                            "nom": "Test",
                            "description": "Test",
                            "tempsPreparationMin": 5,
                            "tempsCuissonMin": 0,
                            "nbPortions": 1,
                            "regimesCompatibles": ["VEGAN"],
                            "typeRepas": ["DEJEUNER"],
                            "ingredients": [],
                            "etapes": ["Etape 1"],
                            "publie": false
                        }""",
                    )
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
}
