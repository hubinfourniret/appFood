# US Status — Statut detaille d'une User Story

Affiche le statut detaille d'une US specifique. L'utilisateur passe l'ID de la US en argument (ex: `/us-status AUTH-01`).

## Instructions

1. Recupere l'ID de la US depuis l'argument : $ARGUMENTS
2. Lis `docs/phase3-backlog-rapport.md` pour trouver la US (criteres d'acceptation, complexite, dependances)
3. Lis `docs/sprint-tracker.md` pour son statut actuel
4. Lis `docs/WAITING-REVIEW.md` si elle est en attente de validation
5. Cherche la branche `feature/{US-ID}-*` dans git pour voir le code produit
6. Affiche :

```
## [US-ID] — [Titre]

### Definition
> En tant que [persona], je veux [action] afin de [benefice].

### Criteres d'acceptation
- [ ] ou [x] selon l'etat

### Infos
- **Complexite** : S/M/L/XL
- **Priorite** : MVP/V1.1/V2
- **Dependances** : [liste]
- **Agents assignes** : [liste]
- **Statut** : Todo / In Progress / Review / Waiting User / Done / Blocked
- **Branche** : feature/{US-ID}-... (si existe)

### Review
[Rapport de review si disponible]

### Blocages
[Actions humaines requises si applicable]
```

7. Si l'ID n'est pas trouve, liste les US dont l'ID est proche.
