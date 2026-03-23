# Phase 3 — Backlog & User Stories

Règles et structure pour la création du backlog projet.

## Structure du backlog

Le backlog est organisé en 3 niveaux :

```
Thème (grand domaine fonctionnel)
  └── Epic (fonctionnalité majeure)
       └── User Story (tâche unitaire livrable)
```

## Rédaction des User Stories

### Format obligatoire

```
**[EPIC-ID-STORY-NUM]** — Titre court

En tant que [persona],
je veux [action concrète],
afin de [bénéfice/valeur].

**Critères d'acceptation :**
- [ ] Critère 1 (vérifiable et testable)
- [ ] Critère 2
- [ ] Critère N

**Complexité :** S | M | L | XL
**Priorité :** MVP | V1.1 | V2 | Nice-to-have
**Dépendances :** [liste des story IDs pré-requis, ou "Aucune"]
**Agent assigné :** [à remplir en Phase 4]
**Notes techniques :** [détails d'implémentation si pertinents]
```

### Guide des complexités

- **S (Small)** : < 1 jour. Tâche isolée, pas d'ambiguïté. Ex : ajouter un champ à un formulaire.
- **M (Medium)** : 1-3 jours. Fonctionnalité simple mais complète. Ex : page de profil utilisateur avec édition.
- **L (Large)** : 3-5 jours. Fonctionnalité complexe avec plusieurs composants. Ex : système de notifications multi-canal.
- **XL (Extra Large)** : > 5 jours. Probablement à redécouper. Si tu écris une story XL, demande-toi si elle peut être splitée. Ex : moteur de recherche full-text avec filtres avancés.

### Guide des priorités

- **MVP** : Sans ça, le produit n'a pas de sens. Le strict minimum pour valider l'hypothèse core.
- **V1.1** : Important mais pas bloquant pour le lancement. Améliore significativement l'expérience.
- **V2** : Features d'expansion, optimisations, nouvelles audiences.
- **Nice-to-have** : Serait cool mais peut vivre sans indéfiniment.

### Bonnes pratiques

1. **Une story = un livrable testable.** Si tu ne peux pas le tester, c'est trop vague.
2. **Indépendance maximale.** Minimise les dépendances entre stories. Si A dépend de B, note-le explicitement.
3. **Le persona est concret.** Pas "l'utilisateur" mais "le restaurateur", "l'admin", "le visiteur non inscrit".
4. **Les critères d'acceptation sont la définition de "fini".** Pas de zone grise.
5. **N'oublie pas les stories techniques.** Setup du projet, CI/CD, migrations, monitoring — ce sont aussi des stories.
6. **N'oublie pas les stories transverses.** Auth, gestion d'erreurs, emails, responsive, accessibilité.

### Epics techniques à ne pas oublier

Ces epics sont souvent absentes des backlogs et créent de la dette technique :

- **SETUP** : Initialisation projet, structure, linting, CI/CD, env de dev
- **AUTH** : Inscription, connexion, reset password, gestion de session, rôles
- **INFRA** : Déploiement, monitoring, logging, alerting, backups
- **DATA** : Migrations, seeds, imports/exports, RGPD (suppression données)
- **UX-TRANSVERSE** : Design system, responsive, états vides/erreur/loading, accessibilité
- **QUALITE** : Tests unitaires, tests E2E, documentation API, documentation développeur

---

## Exemple de structure

```
## THEME : Gestion des utilisateurs

### EPIC : AUTH — Authentification
- AUTH-01 : Inscription par email
- AUTH-02 : Connexion / Déconnexion
- AUTH-03 : Reset de mot de passe
- AUTH-04 : Connexion OAuth (Google)
- AUTH-05 : Gestion des rôles (admin/user)

### EPIC : PROFIL — Profil utilisateur
- PROFIL-01 : Page de profil en lecture
- PROFIL-02 : Édition du profil
- PROFIL-03 : Upload d'avatar
- PROFIL-04 : Suppression de compte (RGPD)
```

---

## Checklist finale du backlog

Avant de présenter le backlog à l'utilisateur, vérifie :

- [ ] Toutes les fonctionnalités discutées en Phase 1 et 2 sont couvertes
- [ ] Les stories MVP sont clairement identifiées et suffisantes pour un lancement
- [ ] Les dépendances forment un graphe cohérent (pas de cycle)
- [ ] Les stories techniques/transverses sont présentes
- [ ] Chaque story a ses critères d'acceptation
- [ ] Les estimations de complexité sont cohérentes entre elles
- [ ] Le backlog MVP est réaliste par rapport aux ressources identifiées
