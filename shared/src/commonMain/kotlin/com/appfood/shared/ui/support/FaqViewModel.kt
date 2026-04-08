package com.appfood.shared.ui.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.api.response.FaqResponse
import com.appfood.shared.ui.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the FAQ screen (SUPPORT-02).
 * Loads FAQ items from the API with a static fallback when offline.
 */
class FaqViewModel(
    // TODO: Inject use case when created by SHARED agent
    // private val chargerFaqUseCase: ChargerFaqUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<FaqState>(FaqState.Loading)
    val state: StateFlow<FaqState> = _state.asStateFlow()

    fun init() {
        loadFaq()
    }

    fun loadFaq() {
        _state.value = FaqState.Loading
        viewModelScope.launch {
            // TODO: Replace with actual API call when use case is available
            // val result = chargerFaqUseCase()
            // when (result) {
            //     is AppResult.Success -> {
            //         val grouped = result.data.groupByTheme()
            //         _state.value = FaqState.Success(grouped)
            //     }
            //     is AppResult.Error -> {
            //         // Fallback to static content when offline
            //         _state.value = FaqState.Success(staticFaqContent())
            //     }
            // }

            // Stub: use static content as placeholder
            _state.value = FaqState.Success(staticFaqContent())
        }
    }

    /**
     * Static FAQ content used as fallback when the API is unavailable.
     * This will be replaced by server-side content once the use case is wired.
     */
    private fun staticFaqContent(): List<FaqThemeGroup> {
        return listOf(
            FaqThemeGroup(
                theme = Strings.FAQ_THEME_COMPTE,
                items = listOf(
                    FaqItem(
                        question = "Comment creer mon compte ?",
                        reponse = "Appuyez sur \"S'inscrire\" depuis l'ecran de connexion. Renseignez votre adresse e-mail et un mot de passe d'au moins 8 caracteres.",
                    ),
                    FaqItem(
                        question = "Comment modifier mon profil ?",
                        reponse = "Rendez-vous dans l'onglet Profil, puis appuyez sur \"Modifier le profil\". Vous pouvez changer vos informations corporelles, votre regime alimentaire et votre niveau d'activite.",
                    ),
                    FaqItem(
                        question = "Comment supprimer mon compte ?",
                        reponse = "Allez dans Profil, puis appuyez sur \"Supprimer mon compte\" en bas de page. Cette action est irreversible et toutes vos donnees seront effacees.",
                    ),
                    FaqItem(
                        question = "J'ai oublie mon mot de passe, que faire ?",
                        reponse = "Sur l'ecran de connexion, appuyez sur \"Mot de passe oublie ?\". Un lien de reinitialisation vous sera envoye par e-mail.",
                    ),
                    FaqItem(
                        question = "Mes donnees sont-elles securisees ?",
                        reponse = "Oui, vos donnees sont chiffrees en base de donnees et les communications sont securisees via HTTPS. Consultez notre politique de confidentialite pour plus de details.",
                    ),
                ),
            ),
            FaqThemeGroup(
                theme = Strings.FAQ_THEME_SAISIE,
                items = listOf(
                    FaqItem(
                        question = "Comment ajouter un aliment a mon journal ?",
                        reponse = "Appuyez sur le bouton \"+\" en bas de l'ecran, choisissez le type de repas, puis recherchez un aliment. Selectionnez la portion et validez.",
                    ),
                    FaqItem(
                        question = "Puis-je ajouter un aliment qui n'existe pas dans la base ?",
                        reponse = "Pour le moment, seuls les aliments de la base Ciqual et Open Food Facts sont disponibles. Une fonctionnalite d'ajout manuel est prevue pour une prochaine version.",
                    ),
                    FaqItem(
                        question = "Comment modifier ou supprimer une entree du journal ?",
                        reponse = "Depuis le tableau de bord, appuyez sur l'entree que vous souhaitez modifier. Vous pourrez ajuster la quantite ou supprimer l'entree.",
                    ),
                    FaqItem(
                        question = "Comment ajouter une recette a mon journal ?",
                        reponse = "Lors de l'ajout d'un aliment, basculez en mode \"Recette\" en haut de l'ecran de recherche. Selectionnez la recette et indiquez le nombre de portions.",
                    ),
                    FaqItem(
                        question = "Comment fonctionnent les favoris ?",
                        reponse = "Lors de la recherche d'un aliment, appuyez sur l'etoile a cote d'un aliment pour l'ajouter aux favoris. Vos favoris apparaissent en haut de l'ecran de saisie pour un acces rapide.",
                    ),
                ),
            ),
            FaqThemeGroup(
                theme = Strings.FAQ_THEME_QUOTAS,
                items = listOf(
                    FaqItem(
                        question = "Comment sont calcules mes quotas nutritionnels ?",
                        reponse = "Vos quotas sont calcules automatiquement en fonction de votre profil : age, sexe, poids, taille, niveau d'activite et regime alimentaire. Ils suivent les recommandations officielles (AJR).",
                    ),
                    FaqItem(
                        question = "Puis-je personnaliser mes quotas ?",
                        reponse = "Oui, rendez-vous dans le tableau de bord, puis appuyez sur \"Gestion des quotas\". Vous pouvez modifier chaque quota individuellement ou revenir au calcul automatique.",
                    ),
                    FaqItem(
                        question = "Mes quotas changent-ils si je modifie mon poids ?",
                        reponse = "Si votre poids change significativement, l'application vous proposera de recalculer vos quotas. Vous pouvez accepter ou conserver vos quotas actuels.",
                    ),
                    FaqItem(
                        question = "Que signifient les couleurs des barres de progression ?",
                        reponse = "Vert signifie que vous etes proche de votre objectif. Orange indique un apport insuffisant ou legerement excessif. Rouge signale un ecart important avec votre quota.",
                    ),
                    FaqItem(
                        question = "Quels nutriments sont suivis ?",
                        reponse = "L'application suit les macronutriments (calories, proteines, glucides, lipides, fibres), les vitamines (B12, D, C, etc.) et les mineraux (fer, calcium, zinc, etc.) adaptes a votre regime.",
                    ),
                ),
            ),
            FaqThemeGroup(
                theme = Strings.FAQ_THEME_RECETTES,
                items = listOf(
                    FaqItem(
                        question = "D'ou viennent les recettes ?",
                        reponse = "Les recettes sont creees et validees par notre equipe. Elles sont adaptees aux regimes vegan, vegetarien et flexitarien.",
                    ),
                    FaqItem(
                        question = "Puis-je creer mes propres recettes ?",
                        reponse = "La creation de recettes par les utilisateurs est prevue pour une prochaine version de l'application.",
                    ),
                    FaqItem(
                        question = "Comment filtrer les recettes par regime ?",
                        reponse = "Dans l'onglet Recettes, utilisez les filtres en haut de la page pour selectionner votre regime alimentaire et le type de repas souhaite.",
                    ),
                    FaqItem(
                        question = "Les valeurs nutritionnelles des recettes sont-elles fiables ?",
                        reponse = "Les valeurs nutritionnelles sont calculees automatiquement a partir des ingredients de la base Ciqual. Elles sont indicatives et peuvent varier selon les produits utilises.",
                    ),
                    FaqItem(
                        question = "Pourquoi certaines recettes ne correspondent pas a mon regime ?",
                        reponse = "Les recettes suggerees tiennent compte de votre regime alimentaire. Si une recette ne correspond pas, verifiez vos preferences dans votre profil.",
                    ),
                ),
            ),
            FaqThemeGroup(
                theme = Strings.FAQ_THEME_DONNEES,
                items = listOf(
                    FaqItem(
                        question = "Mes donnees sont-elles synchronisees entre mes appareils ?",
                        reponse = "Oui, vos donnees sont synchronisees automatiquement lorsque vous etes connecte a Internet. En mode hors ligne, les saisies sont enregistrees localement et synchronisees lors de la prochaine connexion.",
                    ),
                    FaqItem(
                        question = "Puis-je exporter mes donnees ?",
                        reponse = "L'export de donnees est prevu pour une prochaine version. Conformement au RGPD, vous pouvez demander vos donnees en contactant le support.",
                    ),
                    FaqItem(
                        question = "D'ou proviennent les donnees nutritionnelles ?",
                        reponse = "Les donnees nutritionnelles proviennent de la base Ciqual de l'ANSES (reference francaise) et d'Open Food Facts (base collaborative). Elles sont regulierement mises a jour.",
                    ),
                    FaqItem(
                        question = "Comment fonctionne le mode hors ligne ?",
                        reponse = "En mode hors ligne, vous pouvez continuer a saisir vos repas, votre poids et votre hydratation. Les donnees seront synchronisees automatiquement lorsque la connexion sera retablie.",
                    ),
                    FaqItem(
                        question = "Que se passe-t-il si je reinstalle l'application ?",
                        reponse = "Vos donnees sont stockees sur nos serveurs. Apres reinstallation, connectez-vous avec votre compte et vos donnees seront synchronisees automatiquement.",
                    ),
                ),
            ),
        )
    }
}

/**
 * State for the FAQ screen.
 */
sealed interface FaqState {
    data object Loading : FaqState
    data class Success(val themes: List<FaqThemeGroup>) : FaqState
    data class Error(val message: String) : FaqState
}

/**
 * A group of FAQ items under a common theme.
 */
data class FaqThemeGroup(
    val theme: String,
    val items: List<FaqItem>,
)

/**
 * A single FAQ question/answer pair.
 */
data class FaqItem(
    val question: String,
    val reponse: String,
)

/**
 * Groups a list of FaqResponse by theme, maintaining order.
 */
fun List<FaqResponse>.groupByTheme(): List<FaqThemeGroup> {
    return groupBy { it.theme }
        .map { (theme, items) ->
            FaqThemeGroup(
                theme = theme,
                items = items.sortedBy { it.ordre }.map { faq ->
                    FaqItem(
                        question = faq.question,
                        reponse = faq.reponse,
                    )
                },
            )
        }
}
