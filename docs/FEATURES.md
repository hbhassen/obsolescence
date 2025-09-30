# Fonctionnalités étendues

Ce document décrit les fonctionnalités ajoutées pour répondre aux besoins de support multi-plateforme et de gouvernance Git.

## Prise en charge multi-forge (GitHub & GitLab)

- Un **collecteur composite** (`CompositeProjectScannerService`) orchestre plusieurs implémentations de scanners SCM.
- Deux scanners sont fournis par défaut :
  - `DefaultGithubScannerService` (API GitHub REST v3).
  - `DefaultGitlabScannerService` (API GitLab v4).
- La propriété `dashboard.sources.enabled-providers` permet d'activer l'une ou l'autre forge (`github`, `gitlab`) ou les deux simultanément (valeur séparée par des virgules).
- Un argument de lancement (`--providers` ou `--scm-providers`) peut surcharger dynamiquement cette sélection pour un run donné.

## Configuration Git Flow externalisée

- Les règles Git Flow sont désormais portées par le fichier `src/main/resources/config/gitflow.yml`.
- Les propriétés couvertes incluent :
  - `required-branches`
  - `allowed-branch-patterns`
  - `protected-branches`
  - `allowed-default-branches`
  - `require-protected-default-branch`
  - `max-branch-inactivity-days`
- `DefaultComplianceCheckerService` consomme ces règles pour générer des violations détaillées : branches manquantes, défaut de protection, branche par défaut non conforme, branches inactives.

## Catalogue de structures par stack

- Les règles de structure ont été déplacées dans `src/main/resources/structure/stacks.yml`.
- Le fichier recense les principales stacks rencontrées (Java Spring, Quarkus, Angular, React, Vue, Express, Django, Flask, .NET, Rails, Go, Laravel...).
- Chaque stack précise : chemins obligatoires, fichiers CI attendus, frameworks autorisés/interdits, langages associés.

## Paramétrage GitLab

- Nouvelles propriétés `dashboard.gitlab` (token, group, base-url, include-subgroups, page-size).
- Intégration transparente avec les règles d'analyse existantes (`MetadataAnalyzerService`, détecteur d'obsolescence, etc.).

## Contrôles de bonnes pratiques Git

- Vérification automatique de la protection des branches critiques (main/master/develop).
- Obligation de protection pour la branche par défaut.
- Détection des branches inactives dépassant la limite configurée.

## Fichiers concernés

- `src/main/resources/config/gitflow.yml` — politique Git Flow.
- `src/main/resources/structure/stacks.yml` — référentiel des stacks.
- `src/main/resources/application.yml` — activation des providers SCM et propriétés GitLab.
- `docs/FEATURES.md` — ce document.
