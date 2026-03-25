# appFood — Modeles de donnees

> Source de verite pour tous les modeles.
> 3 representations : Kotlin (shared), PostgreSQL (Exposed), SQLDelight (local).
> Les agents doivent suivre exactement ces definitions.

---

## 1. Enumerations partagees

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Enums.kt

enum class RegimeAlimentaire {
    VEGAN,
    VEGETARIEN,
    FLEXITARIEN,
    OMNIVORE
}

enum class NiveauActivite {
    SEDENTAIRE,       // Coefficient 1.2
    LEGER,            // Coefficient 1.375
    MODERE,           // Coefficient 1.55
    ACTIF,            // Coefficient 1.725
    TRES_ACTIF        // Coefficient 1.9
}

enum class Sexe {
    HOMME,
    FEMME
}

enum class MealType {
    PETIT_DEJEUNER,
    DEJEUNER,
    DINER,
    COLLATION
}

enum class NutrimentType {
    CALORIES,         // kcal
    PROTEINES,        // g
    GLUCIDES,         // g
    LIPIDES,          // g
    FIBRES,           // g
    SEL,              // g
    SUCRES,           // g
    FER,              // mg
    CALCIUM,          // mg
    ZINC,             // mg
    MAGNESIUM,        // mg
    VITAMINE_B12,     // µg
    VITAMINE_D,       // µg
    VITAMINE_C,       // mg
    OMEGA_3,          // g
    OMEGA_6           // g
}

enum class SourceAliment {
    CIQUAL,
    OPEN_FOOD_FACTS,
    MANUEL
}

enum class SourceRecette {
    MANUELLE,
    IMPORT,
    COMMUNAUTAIRE
}

enum class ConsentType {
    ANALYTICS,
    PUBLICITE,
    AMELIORATION_SERVICE
}

enum class NotificationType {
    RAPPEL,
    RECOMMANDATION,
    BILAN,
    HYDRATATION
}

// V1.1 — Objectif de poids (POIDS-03)
enum class ObjectifPoids {
    PRISE_DE_MASSE,    // +300-500 kcal/jour
    MAINTIEN,          // quotas standards
    PERTE_DE_POIDS     // -300-500 kcal/jour
}

enum class Role {
    USER,
    ADMIN
}

enum class SyncStatus {
    SYNCED,
    PENDING,
    CONFLICT
}
```

---

## 2. Modeles Kotlin (shared/model/)

### User

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/User.kt

@Serializable
data class User(
    val id: String,                // UUID, genere par Firebase Auth
    val email: String,
    val nom: String?,
    val prenom: String?,
    val role: Role = Role.USER,    // ADMIN pour les gestionnaires de contenu (recettes)
    val createdAt: Instant,
    val updatedAt: Instant
)

@Serializable
data class UserProfile(
    val userId: String,
    val sexe: Sexe,
    val age: Int,                  // en annees
    val poidsKg: Double,           // poids actuel en kg
    val tailleCm: Int,             // taille en cm
    val regimeAlimentaire: RegimeAlimentaire,
    val niveauActivite: NiveauActivite,
    val onboardingComplete: Boolean,
    val objectifPoids: ObjectifPoids?,  // V1.1 — null = pas d'objectif defini (equivalent MAINTIEN)
    val updatedAt: Instant
)

@Serializable
data class UserPreferences(
    val userId: String,
    val alimentsExclus: List<String>,      // liste d'aliment IDs
    val allergies: List<String>,           // ex: "gluten", "soja", "arachides"
    val alimentsFavoris: List<String>,     // liste d'aliment IDs
    val updatedAt: Instant
)
```

### Aliment

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Aliment.kt

@Serializable
data class Aliment(
    val id: String,                // UUID
    val nom: String,
    val marque: String?,           // null pour les aliments generiques (Ciqual)
    val source: SourceAliment,
    val sourceId: String?,         // ID dans la base source (code Ciqual, barcode OFF)
    val codeBarres: String?,
    val categorie: String,
    val regimesCompatibles: List<RegimeAlimentaire>,
    val nutrimentsPour100g: NutrimentValues,
    val portionsStandard: List<PortionStandard>
)

@Serializable
data class NutrimentValues(
    val calories: Double,          // kcal
    val proteines: Double,         // g
    val glucides: Double,          // g
    val lipides: Double,           // g
    val fibres: Double,            // g
    val sel: Double,               // g
    val sucres: Double,            // g
    val fer: Double,               // mg
    val calcium: Double,           // mg
    val zinc: Double,              // mg
    val magnesium: Double,         // mg
    val vitamineB12: Double,       // µg
    val vitamineD: Double,         // µg
    val vitamineC: Double,         // mg
    val omega3: Double,            // g
    val omega6: Double             // g
)
```

### Portion

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Portion.kt

@Serializable
data class PortionStandard(
    val id: String,                // UUID
    val alimentId: String?,        // null si portion generique
    val nom: String,               // ex: "1 pomme", "1 cuillere a soupe", "1 bol"
    val quantiteGrammes: Double,   // equivalence en grammes
    val estGenerique: Boolean,     // true = s'applique a tout (cuillere, bol...)
    val estPersonnalise: Boolean,  // true = cree par l'utilisateur
    val userId: String?            // null si portion systeme, userId si personnalisee
)
```

### Recette

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Recette.kt

@Serializable
data class Recette(
    val id: String,                // UUID
    val nom: String,
    val description: String,
    val tempsPreparationMin: Int,  // en minutes
    val tempsCuissonMin: Int,      // en minutes
    val nbPortions: Int,
    val regimesCompatibles: List<RegimeAlimentaire>,
    val source: SourceRecette,
    val typeRepas: List<MealType>, // petit-dej, plat, dessert...
    val ingredients: List<IngredientRecette>,
    val etapes: List<String>,      // etapes de preparation ordonnees
    val nutrimentsTotaux: NutrimentValues,   // calcule automatiquement, persiste en base
    val imageUrl: String?,
    val publie: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Serializable
data class IngredientRecette(
    val alimentId: String,
    val alimentNom: String,        // denormalise pour affichage rapide
    val quantiteGrammes: Double
)
```

### Journal

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Journal.kt

@Serializable
data class JournalEntry(
    val id: String,                // UUID
    val userId: String,
    val date: LocalDate,           // kotlinx.datetime
    val mealType: MealType,
    val alimentId: String?,        // reference aliment OU recette, pas les deux
    val recetteId: String?,
    val nom: String,               // denormalise pour affichage rapide
    val quantiteGrammes: Double,   // quantite consommee
    val nbPortions: Double?,       // si recette, nombre de portions consommees
    val nutrimentsCalcules: NutrimentValues, // calcule = nutriments * quantite / 100
    @Transient
    val syncStatus: SyncStatus = SyncStatus.SYNCED,    // CHAMP LOCAL UNIQUEMENT — @Transient = exclu de la serialisation JSON. Present en SQLDelight, absent de la table Exposed. Default SYNCED pour l'instanciation depuis le serveur.
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Quota

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Quota.kt

@Serializable
data class QuotaJournalier(
    val userId: String,
    val nutriment: NutrimentType,
    val valeurCible: Double,       // valeur actuelle (calculee ou personnalisee)
    val estPersonnalise: Boolean,
    val valeurCalculee: Double,    // valeur d'origine calculee (pour "revenir au calcul auto")
    val unite: String,             // "kcal", "g", "mg", "µg"
    val updatedAt: Instant
)

// Utilise pour l'affichage dashboard
@Serializable
data class QuotaStatus(
    val nutriment: NutrimentType,
    val valeurCible: Double,
    val valeurConsommee: Double,
    val pourcentage: Double,       // consommee / cible * 100
    val unite: String
)
```

### Poids

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Poids.kt

@Serializable
// Note : pas de updatedAt — les pesees sont IMMUTABLES apres creation.
// Pour corriger un poids, supprimer et recreer l'entree.
data class HistoriquePoids(
    val id: String,                // UUID
    val userId: String,
    val date: LocalDate,
    val poidsKg: Double,
    val estReference: Boolean,     // true = utilise pour le calcul des quotas
    val syncStatus: SyncStatus = SyncStatus.SYNCED,    // CHAMP LOCAL UNIQUEMENT — absent de la table Exposed. Default SYNCED pour la deserialisation depuis le serveur.
    val createdAt: Instant
)
```

### Hydratation

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Hydratation.kt

@Serializable
data class HydratationJournaliere(
    val id: String,                // UUID
    val userId: String,
    val date: LocalDate,
    val quantiteMl: Int,           // cumul du jour
    val objectifMl: Int,           // calcule ou personnalise
    val estObjectifPersonnalise: Boolean,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,    // CHAMP LOCAL UNIQUEMENT — absent de la table Exposed. Default SYNCED pour la deserialisation depuis le serveur.
    val entrees: List<HydratationEntry>,
    val updatedAt: Instant             // necessaire pour le ConflictResolver (last-write-wins)
)

@Serializable
data class HydratationEntry(
    val id: String,                // UUID
    val heure: Instant,
    val quantiteMl: Int            // ex: 250 (un verre), 500 (une bouteille)
)
```

### Notification

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Notification.kt

@Serializable
data class AppNotification(
    val id: String,                // UUID
    val userId: String,
    val type: NotificationType,
    val titre: String,
    val contenu: String,
    val dateEnvoi: Instant,
    val lue: Boolean
)
```

### Consentement

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Consentement.kt

@Serializable
data class Consentement(
    val id: String,                // UUID
    val userId: String,
    val type: ConsentType,
    val accepte: Boolean,
    val dateConsentement: Instant,
    val versionPolitique: String   // ex: "1.0", "1.1"
)
```

### Recommandation

```kotlin
// shared/src/commonMain/kotlin/com/appfood/shared/model/Recommandation.kt

@Serializable
data class RecommandationAliment(
    val aliment: Aliment,
    val nutrimentsCibles: List<NutrimentType>,  // nutriments que cet aliment aide a combler
    val quantiteSuggereGrammes: Double,         // quantite pour combler le manque
    val pourcentageCouverture: Map<NutrimentType, Double> // % du manque couvert par nutriment
)

@Serializable
data class RecommandationRecette(
    val recette: Recette,
    val nutrimentsCibles: List<NutrimentType>,
    val pourcentageCouvertureGlobal: Double,    // % global de comblement des manques
    val pourcentageCouverture: Map<NutrimentType, Double>
)
```

---

## 3. Schema PostgreSQL (Exposed — backend)

```kotlin
// backend/src/main/kotlin/com/appfood/backend/database/tables/

// --- UsersTable.kt ---
object UsersTable : Table("users") {
    val id = varchar("id", 36)             // UUID Firebase
    val email = varchar("email", 255).uniqueIndex()
    val nom = varchar("nom", 100).nullable()
    val prenom = varchar("prenom", 100).nullable()
    val role = enumerationByName<Role>("role", 10).default(Role.USER)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object UserProfilesTable : Table("user_profiles") {
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val sexe = enumerationByName<Sexe>("sexe", 10)
    val age = integer("age")
    val poidsKg = double("poids_kg")              // CHIFFRE — donnee sensible
    val tailleCm = integer("taille_cm")           // CHIFFRE — donnee sensible
    val regimeAlimentaire = enumerationByName<RegimeAlimentaire>("regime_alimentaire", 20)
    val niveauActivite = enumerationByName<NiveauActivite>("niveau_activite", 20)
    val onboardingComplete = bool("onboarding_complete").default(false)
    val objectifPoids = enumerationByName<ObjectifPoids>("objectif_poids", 20).nullable() // V1.1
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(userId)
}

object UserPreferencesTable : Table("user_preferences") {
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val alimentsExclus = text("aliments_exclus")   // JSON array of IDs
    val allergies = text("allergies")               // JSON array of strings
    val alimentsFavoris = text("aliments_favoris")  // JSON array of IDs
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(userId)
}

// --- AlimentsTable.kt ---
object AlimentsTable : Table("aliments") {
    val id = varchar("id", 36)
    val nom = varchar("nom", 255)
    val marque = varchar("marque", 255).nullable()
    val source = enumerationByName<SourceAliment>("source", 20)
    val sourceId = varchar("source_id", 100).nullable()
    val codeBarres = varchar("code_barres", 50).nullable().index()
    val categorie = varchar("categorie", 100)
    val regimesCompatibles = text("regimes_compatibles") // JSON array
    // Nutriments pour 100g
    val calories = double("calories")
    val proteines = double("proteines")
    val glucides = double("glucides")
    val lipides = double("lipides")
    val fibres = double("fibres")
    val sel = double("sel")
    val sucres = double("sucres")
    val fer = double("fer")
    val calcium = double("calcium")
    val zinc = double("zinc")
    val magnesium = double("magnesium")
    val vitamineB12 = double("vitamine_b12")
    val vitamineD = double("vitamine_d")
    val vitamineC = double("vitamine_c")
    val omega3 = double("omega_3")
    val omega6 = double("omega_6")
    override val primaryKey = PrimaryKey(id)
}

// --- PortionsTable.kt ---
object PortionsTable : Table("portions") {
    val id = varchar("id", 36)
    val alimentId = varchar("aliment_id", 36).references(AlimentsTable.id).nullable()
    val nom = varchar("nom", 100)
    val quantiteGrammes = double("quantite_grammes")
    val estGenerique = bool("est_generique").default(false)
    val estPersonnalise = bool("est_personnalise").default(false)
    val userId = varchar("user_id", 36).references(UsersTable.id).nullable()
    override val primaryKey = PrimaryKey(id)
}

// --- RecettesTable.kt ---
object RecettesTable : Table("recettes") {
    val id = varchar("id", 36)
    val nom = varchar("nom", 255)
    val description = text("description")
    val tempsPreparationMin = integer("temps_preparation_min")
    val tempsCuissonMin = integer("temps_cuisson_min")
    val nbPortions = integer("nb_portions")
    val regimesCompatibles = text("regimes_compatibles") // JSON array
    val source = enumerationByName<SourceRecette>("source", 20)
    val typeRepas = text("type_repas")                   // JSON array
    val etapes = text("etapes")                          // JSON array
    // Nutriments totaux (calcules)
    val calories = double("calories")
    val proteines = double("proteines")
    val glucides = double("glucides")
    val lipides = double("lipides")
    val fibres = double("fibres")
    val sel = double("sel")
    val sucres = double("sucres")
    val fer = double("fer")
    val calcium = double("calcium")
    val zinc = double("zinc")
    val magnesium = double("magnesium")
    val vitamineB12 = double("vitamine_b12")
    val vitamineD = double("vitamine_d")
    val vitamineC = double("vitamine_c")
    val omega3 = double("omega_3")
    val omega6 = double("omega_6")
    val imageUrl = varchar("image_url", 500).nullable()
    val publie = bool("publie").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object IngredientsTable : Table("ingredients") {
    val id = varchar("id", 36)
    val recetteId = varchar("recette_id", 36).references(RecettesTable.id, onDelete = ReferenceOption.CASCADE)
    val alimentId = varchar("aliment_id", 36).references(AlimentsTable.id)
    val alimentNom = varchar("aliment_nom", 255)
    val quantiteGrammes = double("quantite_grammes")
    override val primaryKey = PrimaryKey(id)
}

// --- JournalEntriesTable.kt ---
object JournalEntriesTable : Table("journal_entries") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val date = date("date").index()
    val mealType = enumerationByName<MealType>("meal_type", 20)
    val alimentId = varchar("aliment_id", 36).references(AlimentsTable.id).nullable()
    val recetteId = varchar("recette_id", 36).references(RecettesTable.id).nullable()
    val nom = varchar("nom", 255)
    val quantiteGrammes = double("quantite_grammes")
    val nbPortions = double("nb_portions").nullable()
    // Nutriments calcules (snapshot au moment de la saisie)
    val calories = double("calories")
    val proteines = double("proteines")
    val glucides = double("glucides")
    val lipides = double("lipides")
    val fibres = double("fibres")
    val sel = double("sel")
    val sucres = double("sucres")
    val fer = double("fer")
    val calcium = double("calcium")
    val zinc = double("zinc")
    val magnesium = double("magnesium")
    val vitamineB12 = double("vitamine_b12")
    val vitamineD = double("vitamine_d")
    val vitamineC = double("vitamine_c")
    val omega3 = double("omega_3")
    val omega6 = double("omega_6")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

// --- QuotasTable.kt ---
object QuotasTable : Table("quotas") {
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val nutriment = enumerationByName<NutrimentType>("nutriment", 20)
    val valeurCible = double("valeur_cible")
    val estPersonnalise = bool("est_personnalise").default(false)
    val valeurCalculee = double("valeur_calculee")
    val unite = varchar("unite", 10)
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(userId, nutriment) // Cle naturelle composite, pas d'id surrogate — aligne avec le modele Kotlin et SQLDelight
}

// --- PoidsHistoryTable.kt ---
object PoidsHistoryTable : Table("poids_history") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val date = date("date")
    val poidsKg = double("poids_kg")               // CHIFFRE — donnee sensible
    val estReference = bool("est_reference").default(false)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

// --- HydratationTable.kt ---
object HydratationTable : Table("hydratation") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val date = date("date")
    val quantiteMl = integer("quantite_ml")
    val objectifMl = integer("objectif_ml")
    val estObjectifPersonnalise = bool("est_objectif_personnalise").default(false)
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("idx_user_date_hydra", userId, date)
    }
}

object HydratationEntriesTable : Table("hydratation_entries") {
    val id = varchar("id", 36)
    val hydratationId = varchar("hydratation_id", 36).references(HydratationTable.id, onDelete = ReferenceOption.CASCADE)
    val heure = timestamp("heure")
    val quantiteMl = integer("quantite_ml")
    override val primaryKey = PrimaryKey(id)
}

// --- FcmTokensTable.kt ---
object FcmTokensTable : Table("fcm_tokens") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val token = text("token")
    val platform = varchar("platform", 10)  // "ANDROID" | "IOS"
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("idx_user_token", userId, token) // evite les doublons exact
        // NOTE : actuellement 1 token par (userId, token). Un utilisateur peut avoir
        // plusieurs devices de la meme plateforme. Si le multi-device pose probleme
        // (trop de notifications), c'est une decision produit a prendre — voir TODO-HUMAIN.md
    }
}

// --- NotificationsTable.kt ---
object NotificationsTable : Table("notifications") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val type = enumerationByName<NotificationType>("type", 20)
    val titre = varchar("titre", 255)
    val contenu = text("contenu")
    val dateEnvoi = timestamp("date_envoi")
    val lue = bool("lue").default(false)
    override val primaryKey = PrimaryKey(id)
}

// --- ConsentsTable.kt ---
object ConsentsTable : Table("consents") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val type = enumerationByName<ConsentType>("type", 30)
    val accepte = bool("accepte")
    val dateConsentement = timestamp("date_consentement")
    val versionPolitique = varchar("version_politique", 10)
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("idx_user_consent_type", userId, type)
    }
}

// --- FaqTable.kt ---
object FaqTable : Table("faq") {
    val id = varchar("id", 36)
    val theme = varchar("theme", 100)              // ex: "compte", "saisie", "quotas"
    val question = text("question")
    val reponse = text("reponse")
    val ordre = integer("ordre")
    val actif = bool("actif").default(true)
    override val primaryKey = PrimaryKey(id)
}
```

---

## 4. Schema SQLDelight (local — shared)

```sql
-- shared/src/commonMain/sqldelight/com/appfood/shared/AppDatabase.sq

-- Cache du profil utilisateur (separe en 3 tables pour meilleure separation des concerns)
CREATE TABLE local_user (
    id TEXT NOT NULL PRIMARY KEY,
    email TEXT NOT NULL,
    nom TEXT,
    prenom TEXT,
    role TEXT NOT NULL DEFAULT 'USER',
    onboarding_complete INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL DEFAULT 0,
    updated_at INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE local_user_profile (
    user_id TEXT NOT NULL PRIMARY KEY,
    sexe TEXT NOT NULL,
    age INTEGER NOT NULL,
    poids_kg REAL NOT NULL,
    taille_cm INTEGER NOT NULL,
    regime_alimentaire TEXT NOT NULL,
    niveau_activite TEXT NOT NULL,
    objectif_poids TEXT,           -- nullable, V1.1 (ObjectifPoids enum as string)
    updated_at INTEGER NOT NULL DEFAULT 0
);

-- Cache des aliments (favoris + recents + recherches)
CREATE TABLE local_aliment (
    id TEXT NOT NULL PRIMARY KEY,
    nom TEXT NOT NULL,
    marque TEXT,
    source TEXT NOT NULL,
    source_id TEXT,                 -- ID dans la base source (code Ciqual, barcode OFF)
    code_barres TEXT,               -- pour le scan offline (V1.1) + recherche par barcode
    categorie TEXT NOT NULL,
    regimes_compatibles TEXT NOT NULL,
    calories REAL NOT NULL,
    proteines REAL NOT NULL,
    glucides REAL NOT NULL,
    lipides REAL NOT NULL,
    fibres REAL NOT NULL,
    sel REAL NOT NULL,
    sucres REAL NOT NULL,
    fer REAL NOT NULL,
    calcium REAL NOT NULL,
    zinc REAL NOT NULL,
    magnesium REAL NOT NULL,
    vitamine_b12 REAL NOT NULL,
    vitamine_d REAL NOT NULL,
    vitamine_c REAL NOT NULL,
    omega_3 REAL NOT NULL,
    omega_6 REAL NOT NULL,
    est_favori INTEGER NOT NULL DEFAULT 0,
    derniere_utilisation INTEGER,
    cached_at INTEGER NOT NULL
);

-- Journal alimentaire (offline-first)
CREATE TABLE local_journal_entry (
    id TEXT NOT NULL PRIMARY KEY,
    user_id TEXT NOT NULL,
    date TEXT NOT NULL,
    meal_type TEXT NOT NULL,
    aliment_id TEXT,
    recette_id TEXT,
    nom TEXT NOT NULL,
    quantite_grammes REAL NOT NULL,
    nb_portions REAL,
    calories REAL NOT NULL,
    proteines REAL NOT NULL,
    glucides REAL NOT NULL,
    lipides REAL NOT NULL,
    fibres REAL NOT NULL,
    sel REAL NOT NULL,
    sucres REAL NOT NULL,
    fer REAL NOT NULL,
    calcium REAL NOT NULL,
    zinc REAL NOT NULL,
    magnesium REAL NOT NULL,
    vitamine_b12 REAL NOT NULL,
    vitamine_d REAL NOT NULL,
    vitamine_c REAL NOT NULL,
    omega_3 REAL NOT NULL,
    omega_6 REAL NOT NULL,
    sync_status TEXT NOT NULL DEFAULT 'PENDING',
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

-- Quotas de l'utilisateur
CREATE TABLE local_quota (
    user_id TEXT NOT NULL,
    nutriment TEXT NOT NULL,
    valeur_cible REAL NOT NULL,
    est_personnalise INTEGER NOT NULL DEFAULT 0,
    valeur_calculee REAL NOT NULL,
    unite TEXT NOT NULL,
    updated_at INTEGER NOT NULL,
    PRIMARY KEY (user_id, nutriment)
);

-- Cache des recettes consultees
CREATE TABLE local_recette (
    id TEXT NOT NULL PRIMARY KEY,
    nom TEXT NOT NULL,
    description TEXT NOT NULL,
    temps_preparation_min INTEGER NOT NULL,
    temps_cuisson_min INTEGER NOT NULL,
    nb_portions INTEGER NOT NULL,
    regimes_compatibles TEXT NOT NULL,
    type_repas TEXT NOT NULL,
    ingredients_json TEXT NOT NULL,  -- JSON array de {alimentId, alimentNom, quantiteGrammes}. Denormalise, PAS de table local_ingredients. Deserialise en List<IngredientRecette> au chargement.
    etapes_json TEXT NOT NULL,      -- JSON array de String (etapes ordonnees). Deserialise en List<String>.
    calories REAL NOT NULL,
    proteines REAL NOT NULL,
    glucides REAL NOT NULL,
    lipides REAL NOT NULL,
    fibres REAL NOT NULL,
    sel REAL NOT NULL,
    sucres REAL NOT NULL,
    fer REAL NOT NULL,
    calcium REAL NOT NULL,
    zinc REAL NOT NULL,
    magnesium REAL NOT NULL,
    vitamine_b12 REAL NOT NULL,
    vitamine_d REAL NOT NULL,
    vitamine_c REAL NOT NULL,
    omega_3 REAL NOT NULL,
    omega_6 REAL NOT NULL,
    image_url TEXT,
    created_at INTEGER NOT NULL,   -- timestamp original de la recette (copie du serveur)
    updated_at INTEGER NOT NULL,   -- timestamp original de la recette (copie du serveur)
    cached_at INTEGER NOT NULL     -- quand le cache local a ete ecrit
);

-- Portions standard (cache local)
CREATE TABLE local_portion (
    id TEXT NOT NULL PRIMARY KEY,
    aliment_id TEXT,
    nom TEXT NOT NULL,
    quantite_grammes REAL NOT NULL,
    est_generique INTEGER NOT NULL DEFAULT 0,
    est_personnalise INTEGER NOT NULL DEFAULT 0,
    user_id TEXT
);

-- Historique poids
CREATE TABLE local_poids (
    id TEXT NOT NULL PRIMARY KEY,
    user_id TEXT NOT NULL,
    date TEXT NOT NULL,
    poids_kg REAL NOT NULL,
    est_reference INTEGER NOT NULL DEFAULT 0,
    sync_status TEXT NOT NULL DEFAULT 'PENDING',
    created_at INTEGER NOT NULL
);

-- Hydratation journaliere
CREATE TABLE local_hydratation (
    id TEXT NOT NULL PRIMARY KEY,
    user_id TEXT NOT NULL,
    date TEXT NOT NULL,
    quantite_ml INTEGER NOT NULL,
    objectif_ml INTEGER NOT NULL,
    est_objectif_personnalise INTEGER NOT NULL DEFAULT 0,
    sync_status TEXT NOT NULL DEFAULT 'PENDING',
    updated_at INTEGER NOT NULL
);

CREATE TABLE local_hydratation_entry (
    id TEXT NOT NULL PRIMARY KEY,
    hydratation_id TEXT NOT NULL,
    heure INTEGER NOT NULL,
    quantite_ml INTEGER NOT NULL,
    FOREIGN KEY (hydratation_id) REFERENCES local_hydratation(id) ON DELETE CASCADE
);

-- Preferences utilisateur
CREATE TABLE local_preferences (
    user_id TEXT NOT NULL PRIMARY KEY,
    aliments_exclus TEXT NOT NULL DEFAULT '[]',
    allergies TEXT NOT NULL DEFAULT '[]',
    aliments_favoris TEXT NOT NULL DEFAULT '[]',
    updated_at INTEGER NOT NULL
);

-- Index pour les requetes frequentes
CREATE INDEX idx_local_journal_user_date ON local_journal_entry(user_id, date);
CREATE INDEX idx_local_journal_date ON local_journal_entry(date);
CREATE INDEX idx_local_poids_user ON local_poids(user_id);
CREATE INDEX idx_local_hydratation_user_date ON local_hydratation(user_id, date);
CREATE INDEX idx_local_aliment_favori ON local_aliment(est_favori);

-- File de sync (entrees en attente de synchronisation)
CREATE TABLE sync_queue (
    id TEXT NOT NULL PRIMARY KEY,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    action TEXT NOT NULL,
    payload_json TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT
);
```

---

## 5. Mapping entre les 3 representations

| Entite | Kotlin (shared/model) | PostgreSQL (Exposed) | SQLDelight (local) |
|--------|-----------------------|----------------------|--------------------|
| User | `User` + `UserProfile` + `UserPreferences` | `UsersTable` + `UserProfilesTable` + `UserPreferencesTable` | `local_user` + `local_preferences` |
| Aliment | `Aliment` + `NutrimentValues` | `AlimentsTable` | `local_aliment` |
| Portion | `PortionStandard` | `PortionsTable` | `local_portion` |
| Recette | `Recette` + `IngredientRecette` | `RecettesTable` + `IngredientsTable` | `local_recette` |
| Journal | `JournalEntry` | `JournalEntriesTable` | `local_journal_entry` |
| Quota | `QuotaJournalier` | `QuotasTable` | `local_quota` |
| Poids | `HistoriquePoids` | `PoidsHistoryTable` | `local_poids` |
| Hydratation | `HydratationJournaliere` + `HydratationEntry` | `HydratationTable` + `HydratationEntriesTable` | `local_hydratation` + `local_hydratation_entry` |
| Notification | `AppNotification` | `NotificationsTable` | (pas de cache local) |
| Consentement | `Consentement` | `ConsentsTable` | (pas de cache local) |
| Recommandation | `RecommandationAliment` + `RecommandationRecette` | (calcule a la volee) | (pas de cache local) |
| FAQ | (pas de modele metier) | `FaqTable` | (pas de cache local) |
| Sync queue | `SyncStatus` | (pas de table serveur) | `sync_queue` |

### Regles de coherence

1. Les **modeles Kotlin** (`shared/model/`) sont la source de verite pour les noms de champs et les types
2. Les **tables Exposed** doivent mapper 1:1 avec les modeles Kotlin (memes noms de champs en snake_case)
3. Les **tables SQLDelight** sont un sous-ensemble — elles ne stockent que ce qui est necessaire en offline
4. Les champs JSON (arrays, objets imbriques) sont stockes en `TEXT` en base et deserialises en Kotlin
5. Les `Instant` sont stockes en `timestamp` (Exposed) ou `INTEGER` epoch millis (SQLDelight)
6. Les `LocalDate` sont stockes en `date` (Exposed) ou `TEXT` ISO-8601 `YYYY-MM-DD` (SQLDelight)
7. Les champs `NutrimentValues` sont **denormalises** en 16 colonnes individuelles dans les tables Exposed et SQLDelight. Le mapping vers/depuis l'objet `NutrimentValues` se fait via une extension function `ResultRow.toNutrimentValues()` dans le DAO et `toNutrimentValues()` dans le LocalDataSource. Chaque agent doit creer ce mapping une seule fois et le reutiliser.
8. Convention **snake_case ↔ camelCase** : les colonnes PostgreSQL et SQLDelight utilisent `snake_case` (`vitamine_b12`), les proprietes Kotlin utilisent `camelCase` (`vitamineB12`). La conversion est faite dans les fonctions de mapping.

### Modeles offline-first vs serveur-only

| Modele | Offline-first | syncStatus | Cache local | Explication |
|--------|---------------|------------|-------------|-------------|
| `JournalEntry` | **Oui** | Oui | `local_journal_entry` | Saisie quotidienne, doit fonctionner sans connexion |
| `HistoriquePoids` | **Oui** | Oui | `local_poids` | Saisie ponctuelle, doit fonctionner sans connexion |
| `HydratationJournaliere` | **Oui** | Oui | `local_hydratation` | Saisie quotidienne, doit fonctionner sans connexion |
| `Aliment` | Non (cache) | Non | `local_aliment` | Cache des aliments consultes/favoris, pas de creation offline |
| `Recette` | Non (cache) | Non | `local_recette` | Cache des recettes consultees, pas de creation offline |
| `QuotaJournalier` | Non (cache) | Non | `local_quota` | Cache local pour affichage dashboard offline |
| `PortionStandard` | Non (cache) | Non | `local_portion` | Cache local des portions |
| `User/UserProfile` | Non (cache) | Non | `local_user` | Cache local du profil |
| `Consentement` | Non | Non | Non | Serveur-only, pas de cache |
| `AppNotification` | Non | Non | Non | Serveur-only, pas de cache |
| `Recommandation` | Non | Non | Non | Calcule a la volee, pas de cache |

**Regle** : Seuls les modeles avec `syncStatus` supportent la creation offline. Les autres sont des caches en lecture seule, mis a jour via `/sync/pull`.

### Champs speciaux — attention agents

| Champ | Comportement | Kotlin | Exposed | SQLDelight |
|-------|-------------|--------|---------|------------|
| `JournalEntry.syncStatus` | Local uniquement — gere la sync offline | Oui | **NON** | Oui (`sync_status`) |
| `HistoriquePoids.syncStatus` | Idem — aligne sur JournalEntry | Oui | **NON** | Oui (`sync_status`) |
| `HydratationJournaliere.syncStatus` | Idem — aligne sur JournalEntry | Oui | **NON** | Oui (`sync_status`) |
| `Recette.nutrimentsParPortion` | RETIRE DU MODELE DOMAIN — present uniquement dans les DTOs API (`RecetteSummaryResponse`, `RecetteDetailResponse`). Calcule cote serveur (= nutrimentsTotaux / nbPortions) au moment de la serialisation de la response. | **NON** | **NON** | **NON** |
| `Aliment.portionsStandard` | Charge via requete separee sur `local_portion WHERE aliment_id = ?` + portions generiques `WHERE est_generique = 1`. Non stocke dans `local_aliment`. Cote Exposed : join sur `PortionsTable`. | Oui | Join | Join |
| `UserProfile.objectifPoids` | V1.1 — nullable, null = MAINTIEN | Oui | Oui (nullable) | Oui (nullable) |

---

*Ce document est la reference pour tous les modeles de donnees. Tout ecart doit etre justifie et valide par le PROJECT-MASTER.*
