# Phase 2B — Design UX/UI & Parcours Utilisateur

Questions et points à couvrir pour le design et l'expérience utilisateur.

## 2B.1 — Identité & Direction visuelle

- As-tu déjà une identité visuelle ? (logo, couleurs, charte graphique)
- Si non, quel "feeling" veux-tu ? (pro/corporate, fun/ludique, minimaliste, premium, tech...)
- Des produits existants dont tu aimes le design ? (références visuelles)
- Dark mode prévu ?
- Design system existant ou à créer ?

## 2B.2 — Parcours utilisateurs principaux

Pour chaque persona identifié en Phase 1, mappe les parcours critiques :

- **Onboarding** : Comment l'utilisateur découvre et s'inscrit ?
  - Inscription : email/password, social login, magic link, invitation only ?
  - Premier lancement : tutoriel guidé, template de départ, sandbox ?
  - Quelle est la "aha moment" — le moment où l'utilisateur comprend la valeur ?
  - Combien de temps/étapes pour atteindre ce moment ?

- **Core loop** : Quelle est l'action principale que l'utilisateur fait en boucle ?
  - Quels sont les 3-5 écrans/pages les plus utilisés ?
  - Quel est le flux le plus fréquent ? (Exemple : créer → éditer → publier → analyser)
  - Y a-t-il des actions collaboratives ? (partage, commentaires, assignation)

- **Rétention** : Qu'est-ce qui fait revenir l'utilisateur ?
  - Notifications ? (email, push, in-app)
  - Contenu généré par d'autres utilisateurs ?
  - Données cumulées qui prennent de la valeur avec le temps ?
  - Habitude ou intégration dans un workflow existant ?

## 2B.3 — Pages & composants clés

Identifie les pages/vues principales du produit :
- Landing page / page marketing
- Page d'inscription / login
- Dashboard principal
- Pages de contenu/fonctionnalité core
- Pages de settings / profil
- Pages admin (si applicable)

Pour chaque page, note :
- Son objectif principal (1 seul)
- Les actions possibles
- Les données affichées
- Les liens vers d'autres pages

## 2B.4 — Responsive & plateformes

- Mobile-first ou desktop-first ?
- App native nécessaire ou PWA suffisante ?
- Quels breakpoints cibler ?
- Y a-t-il des fonctionnalités spécifiques au mobile ? (caméra, GPS, notifications push...)

## 2B.5 — Accessibilité & inclusion

- Niveau d'accessibilité visé ? (WCAG A, AA, AAA ?)
- Langues supportées ?
- Contraintes d'accessibilité spécifiques au secteur ?

## 2B.6 — Sujets souvent oubliés

Assure-toi d'aborder :
- **États vides** : que voit l'utilisateur quand il n'y a pas encore de données ?
- **États d'erreur** : comment communiquer les erreurs de manière humaine ?
- **États de chargement** : skeleton screens, spinners, optimistic updates ?
- **Emails transactionnels** : confirmation, reset password, notifications — ils font partie de l'UX
- **Gestion du contenu** : qui crée/modère le contenu ? Faut-il un CMS ?
- **Feedback utilisateur** : comment les utilisateurs signalent des bugs ou demandent des features ?

---

## Livrable attendu

Une cartographie UX synthétique :
1. Direction visuelle et références
2. Liste des parcours utilisateurs clés (user flows)
3. Inventaire des pages/vues principales avec objectifs
4. Choix de plateforme (responsive, mobile, etc.)
5. Points d'attention UX identifiés
