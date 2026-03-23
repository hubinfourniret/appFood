# Sprint Status — Etat d'avancement

Affiche l'etat d'avancement du sprint en cours.

## Instructions

1. Lis `docs/sprint-tracker.md`
2. Lis `docs/WAITING-REVIEW.md` (US en attente de validation)
3. Lis `docs/TODO-HUMAIN.md` (actions humaines en attente)
4. Affiche un resume structure :

```
## Sprint {N} — Etat d'avancement

### Progression
- Total US : X
- Terminees : X (X%)
- En cours : X
- En attente de review : X
- Bloquees : X
- Todo : X

### US en attente de ta validation
[Liste des US dans WAITING-REVIEW.md]

### Actions humaines requises
[Liste des actions bloquantes dans TODO-HUMAIN.md]

### Prochaines US a lancer
[US dont les dependances sont satisfaites]
```

5. Si aucun fichier de tracking n'existe, indique que le sprint n'a pas encore demarre et propose de lancer `/project-master`.
