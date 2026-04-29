# Phase 3 — Backlog & Taches

Regles et structure pour la creation du backlog projet.

## Contexte : Claude Code comme developpeur principal

Le backlog est concu pour un workflow ou **Claude Code est le developpeur principal unique** et l'utilisateur est le **superviseur** (coordinateur, decideur, testeur fonctionnel). Les taches sont donc :
- **Verticales** : chaque tache couvre toutes les couches (backend, shared, mobile) d'une feature
- **Orientees resultat** : decrites du point de vue de ce que l'utilisateur final voit/fait
- **Autonomes** : Claude Code peut les implementer de bout en bout sans dispatch intermediaire

## Structure du backlog

Le backlog est organise en 2 niveaux :

```
Theme (grand domaine fonctionnel)
  └── Tache (feature complete et livrable, toutes couches confondues)
```

> **Note** : L'ancien niveau "Epic → User Story" est remplace par des taches plus larges et autonomes. Le decoupage fin en sous-taches est laisse a Claude Code au moment de l'implementation.

## Redaction des taches

### Format obligatoire

```
**[THEME-NUM]** — Titre court

**Objectif** : Description en 1-3 phrases de ce que l'utilisateur final voit ou fait
quand cette tache est terminee. Toujours formuler du point de vue du resultat,
pas de l'implementation.

**Contraintes** :
- Contrainte technique 1 (si applicable)
- Contrainte technique 2

**Criteres de validation** :
- [ ] Ce que le superviseur teste pour valider (scenario fonctionnel)
- [ ] Edge case important a verifier
- [ ] Critere technique critique (si applicable, ex: "fonctionne offline")

**Complexite :** S | M | L | XL
**Priorite :** MVP | V1.1 | V2 | Nice-to-have
**Dependances :** [liste des tache IDs pre-requis, ou "Aucune"]
**Critique :** Oui | Non (si Oui = review algo/crypto/sync + validation superviseur obligatoire)
**Notes techniques :** [details d'implementation si pertinents — Claude Code s'en sert comme guide]
```

### Ce qui change par rapport au format User Story classique

| Aspect | Ancien (US) | Nouveau (Tache) |
|--------|-------------|-----------------|
| Granularite | 1 US = 1 couche (backend OU mobile OU shared) | 1 tache = feature complete (toutes couches) |
| Persona | "En tant que [persona], je veux..." | "Objectif : l'utilisateur peut..." |
| Assignation | Agent specifique (BACKEND, MOBILE, SHARED) | Claude Code (full-stack) |
| Criteres | Criteres d'acceptation techniques | Criteres de validation fonctionnels (ce que le superviseur teste) |
| Dispatch | Phase 4 repartit entre agents | Pas de dispatch — Claude Code fait tout |

### Guide des complexites

- **S (Small)** : < 1 session. Bugfix, ajustement UI, ajout de champ. Claude Code le fait en une passe.
- **M (Medium)** : 1-2 sessions. Feature simple mais complete (ecran + endpoint + logique). Ex : page de profil avec edition.
- **L (Large)** : 2-4 sessions. Feature complexe avec plusieurs composants. Ex : systeme de journal alimentaire avec recherche + portions + favoris.
- **XL (Extra Large)** : > 4 sessions. Probablement a decouper. Si tu ecris une tache XL, demande-toi si elle peut etre splitee en 2-3 taches L ou M.

### Guide des priorites

- **MVP** : Sans ca, le produit n'a pas de sens. Le strict minimum pour valider l'hypothese core.
- **V1.1** : Important mais pas bloquant pour le lancement. Ameliore significativement l'experience.
- **V2** : Features d'expansion, optimisations, nouvelles audiences.
- **Nice-to-have** : Serait cool mais peut vivre sans indefiniment.

### Bonnes pratiques

1. **Une tache = un resultat testable par le superviseur.** Si le superviseur ne peut pas le tester sur emulateur/device, c'est trop technique ou trop vague.
2. **Independance maximale.** Minimise les dependances entre taches. Si A depend de B, note-le explicitement.
3. **L'objectif est concret.** Pas "ameliorer le dashboard" mais "le dashboard affiche les calories du jour, les macros et les repas saisis".
4. **Les criteres de validation sont fonctionnels.** Pas "le DTO a un champ X" mais "l'utilisateur voit X sur l'ecran".
5. **Les notes techniques sont un guide, pas une spec.** Claude Code a le contexte du codebase et peut adapter. Ne pas sur-specifier l'implementation.
6. **Marquer les taches critiques.** Toute tache touchant a un algo metier, du chiffrement, de la sync ou des donnees de sante doit etre marquee Critique = Oui.

### Themes techniques a ne pas oublier

Ces themes sont souvent absents des backlogs et creent de la dette technique :

- **SETUP** : Initialisation projet, structure, config build, DI
- **AUTH** : Inscription, connexion, reset password, gestion de session
- **INFRA** : Deploiement, monitoring, logging, alerting, backups
- **DATA** : Imports de donnees, indexation, qualite des donnees
- **UX** : Etats vides, etats de chargement, gestion erreurs, navigation
- **LEGAL** : RGPD, CGU, politique de confidentialite, consentement
- **QUALITE** : Tests unitaires, tests integration, tests E2E

> **Note** : Contrairement a un backlog multi-equipe, ces themes n'ont pas besoin d'etre des epics separees avec des sous-stories par couche. Chaque tache inclut naturellement le backend, le shared et le mobile necessaires.

---

## Exemple de structure

```
## THEME : Suivi nutritionnel quotidien

### JOURNAL-01 — Saisie d'un aliment au journal

**Objectif** : L'utilisateur recherche un aliment, choisit une portion,
voit le resume nutritionnel, valide, et l'entree apparait dans son journal du jour.

**Contraintes** :
- Recherche via Meilisearch (pas de requete SQL directe)
- Offline-first : si pas de connexion, l'entree est mise en file d'attente de sync
- L'aliment doit exister dans PostgreSQL (coherence avec l'index Meilisearch)

**Criteres de validation** :
- [ ] Recherche "banane" → resultats pertinents en < 1s
- [ ] Selection d'une portion → resume nutritionnel affiche (calories, proteines, glucides, lipides)
- [ ] Validation → retour au dashboard, l'entree apparait dans le repas du moment
- [ ] En mode avion → l'entree est sauvegardee localement et synchro au retour en ligne

**Complexite :** L
**Priorite :** MVP
**Dependances :** SETUP-01, DATA-01
**Critique :** Non
**Notes techniques :** Utiliser AddEntryScreen existant. Endpoint POST /api/v1/journal/entries.
Portions via PortionApi. SyncQueue SQLDelight pour offline.
```

---

## Gestion du backlog en continu

Le backlog n'est pas fige. Il evolue au fil du projet :

- **Ajout** : Le superviseur identifie un nouveau besoin ou un bug → nouvelle tache
- **Repriorisation** : Le superviseur change l'ordre des priorites a tout moment
- **Decoupe** : Une tache trop large (XL) est splitee en 2-3 taches plus petites
- **Suppression** : Une tache devenue irrelevante est retiree
- **Conversion** : Un bug remonte en prod peut devenir une tache prioritaire

Le superviseur dit "fais X en premier" ou "continue dans l'ordre" — pas besoin de sprint planning formel.

---

## Checklist finale du backlog

Avant de presenter le backlog a l'utilisateur, verifie :

- [ ] Toutes les fonctionnalites discutees en Phase 1 et 2 sont couvertes
- [ ] Les taches MVP sont clairement identifiees et suffisantes pour un lancement
- [ ] Chaque tache est une **tranche verticale** (pas decoupee par couche technique)
- [ ] Les dependances forment un graphe coherent (pas de cycle)
- [ ] Les themes techniques/transverses sont presents (setup, auth, infra, data, UX, legal, qualite)
- [ ] Chaque tache a des criteres de validation testables par le superviseur
- [ ] Les taches critiques (algo, crypto, sync, donnees sensibles) sont marquees
- [ ] Les estimations de complexite sont coherentes entre elles
- [ ] Le backlog MVP est realiste
- [ ] Les taches humaines (cles API, comptes, contenu legal) sont identifiees separement
