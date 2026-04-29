# [NOM DU PROJET] — Document de cadrage & Backlog

> Genere le : [DATE]
> Version : 1.0

---

## 1. Resume executif

### Pitch
[Une phrase qui resume le projet]

### Probleme resolu
[Description du probleme et de la cible]

### Positionnement
[Differenciation, niche visee, concurrence]

### Modele economique
[Comment le projet genere de la valeur/revenus]

### Perimetre MVP
[Ce qui est inclus dans la premiere version]

### Risques identifies
[Liste des risques principaux avec niveau de criticite]

---

## 2. Decisions d'architecture

### Stack technique
| Couche | Choix | Justification |
|--------|-------|---------------|
| Frontend | [techno] | [pourquoi] |
| Backend | [techno] | [pourquoi] |
| Base de donnees | [techno] | [pourquoi] |
| Hebergement | [provider] | [pourquoi] |
| CI/CD | [outil] | [pourquoi] |

### Architecture applicative
[Pattern choisi, schema des composants]

### Entites principales
[Liste des entites et leurs relations]

### Integrations
[APIs et services tiers]

### Points d'attention
[Decisions structurantes, dette technique acceptee, trade-offs]

---

## 3. Parcours utilisateurs

### Personas
[Liste des personas avec description courte]

### Parcours cles
[Pour chaque persona, les 2-3 parcours principaux]

### Pages/Vues principales
[Inventaire avec objectif de chaque page]

---

## 4. Backlog

### Legende
- **Complexite** : S (< 1 session) | M (1-2 sessions) | L (2-4 sessions) | XL (> 4 sessions, a decouper)
- **Priorite** : MVP | V1.1 | V2 | Nice-to-have
- **Critique** : Oui = review obligatoire (algo metier, crypto, sync, donnees sensibles)

---

### THEME : [Nom du theme]

**[CODE-NUM]** — [Titre]

**Objectif** : [Ce que l'utilisateur final voit/fait quand c'est termine]

**Contraintes** :
- [Contrainte technique si applicable]

**Criteres de validation** :
- [ ] [Scenario fonctionnel que le superviseur teste]
- [ ] [Edge case important]

| Complexite | Priorite | Dependances | Critique |
|------------|----------|-------------|----------|
| M | MVP | Aucune | Non |

Notes techniques : [si necessaire]

---

[Repeter pour chaque tache...]

---

## 5. Organisation du travail

### Roles

| Role | Qui | Responsabilites |
|------|-----|-----------------|
| Developpeur principal | Claude Code | Implemente toutes les taches (full-stack, toutes couches). Ecrit les tests. Maintient la coherence du code. |
| Superviseur | [Nom/Role] | Definit les priorites. Valide les resultats par test fonctionnel. Prend les decisions produit. Realise les actions humaines. |

### Workflow d'execution

```
1. Superviseur donne une directive ou valide la prochaine tache du backlog
2. Claude Code analyse, propose un plan si complexe, implemente (tranche verticale)
3. Claude Code verifie (compilation, coherence) et rapporte ce qui a change
4. Superviseur teste sur emulateur/device
5. Si OK → tache Done. Si KO → Claude Code corrige.
```

Pour les taches marquees **Critique = Oui**, une review approfondie du code est faite avant validation (algorithme, securite, coherence).

### Backlog priorise (ordre d'execution)

| # | ID | Titre | Priorite | Statut |
|---|-----|-------|----------|--------|
| 1 | [CODE-01] | [Titre] | MVP | Todo |
| 2 | [CODE-02] | [Titre] | MVP | Todo |
| 3 | [CODE-03] | [Titre] | MVP | Todo |

Le superviseur peut reordonner a tout moment. Pas de sprint formel — flux continu.

### Actions humaines requises

| Priorite | Action | Bloque quelles taches | Statut |
|----------|--------|----------------------|--------|
| Bloquant | [Action que Claude Code ne peut pas faire] | [CODE-XX] | Todo |
| Important | [Action non-bloquante mais necessaire] | [CODE-YY] | Todo |

---

## 6. Prochaines etapes

1. [ ] Valider ce document
2. [ ] Claude Code commence la premiere tache du backlog
3. [ ] Superviseur prepare les actions humaines bloquantes en parallele

---

*Ce document est vivant. Il sera mis a jour au fil de l'avancement du projet.*
