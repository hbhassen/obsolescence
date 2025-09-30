# Dashboard Radar

Dashboard Radar est une application batch Spring Boot qui analyse quotidiennement les dépôts GitHub **et GitLab** d'une organisation afin de détecter les dérives de conformité et les risques d'obsolescence technique.

## Fonctionnalités principales

- **Inventaire Git multi-forge** : récupération des dépôts GitHub et/ou GitLab (branches, merge/pull requests, langages, frameworks, arborescence).
- **Vérification Git Flow** : contrôle de la présence des branches principales (main/master, develop), protection des branches critiques, conformité de la branche par défaut et détection des branches inactives. Les règles sont externalisées dans `config/gitflow.yml`.
- **Analyse de la structure projet** : vérification des chemins obligatoires, détection des fichiers CI/CD et identification des frameworks utilisés (pom.xml, build.gradle, package.json) grâce au catalogue `structure/stacks.yml` couvrant les principales stacks (Java, JS/TS, Python, .NET, Ruby, Go, PHP...).
- **Détection d'obsolescence** : comparaison des versions détectées avec une matrice d'obsolescence configurable (YAML) pour qualifier le niveau de risque.
- **Persistance PostgreSQL** : stockage des résultats dans une base relationnelle (tables `project`, `branch`, `merge_request`, `tech_stack`, `obsolescence`, `file_check`).
- **Batch Spring Boot** : orchestration par Spring Batch (job `dashboardRadarJob`) déclenché au démarrage de l'application (prévu pour un CronJob OpenShift).

## Architecture logique

```
┌─────────────────────────────┐
│ DashboardRadarApplication   │
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│ Job dashboardRadarJob       │
│  • Tasklet AuditTasklet     │
└────────────┬────────────────┘
             │
             ▼
┌──────────────────────────────────────────────┐
│ CompositeProjectScannerService               │
│  • DefaultGithubScannerService               │
│  • DefaultGitlabScannerService               │
└────────────┬─────────────────────────────────┘
             │ snapshots
             ▼
┌──────────────────────────────────────────────┐
│ MetadataAnalyzerService                      │
│ ComplianceCheckerService                     │
│ ObsolescenceDetectorService                  │
└────────────┬─────────────────────────────────┘
             │ rapports
             ▼
┌──────────────────────────────────────────────┐
│ PersistenceService (JPA/PostgreSQL)          │
└──────────────────────────────────────────────┘
```

## Configuration

La configuration se fait via `application.yml` :

```yaml
spring:
  config:
    import:
      - optional:classpath:structure/stacks.yml
      - optional:classpath:config/gitflow.yml

dashboard:
  github:
    token: <token personnel GitHub>
    organization: <organisation cible>
    page-size: 100
  gitlab:
    token: <token personnel GitLab>
    group: <groupe ou namespace GitLab>
    include-subgroups: true
  sources:
    enabled-providers: github,gitlab # valeurs possibles : github / gitlab / github,gitlab
  obsolescence:
    components: # matrice d'obsolescence (version minimale, dates de fin de support)
```

Les propriétés `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` permettent de piloter la connexion base (par défaut H2 en mémoire pour le développement ; dialecte PostgreSQL pour le déploiement).

## Lancement local

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -DGITHUB_TOKEN=<token> -DGITHUB_ORG=<organisation> \
  -DGITLAB_TOKEN=<token> -DGITLAB_GROUP=<groupe> \
  -DSCM_PROVIDERS=github,gitlab
```

Le job Spring Batch se lance automatiquement au démarrage et peut être planifié via un CronJob OpenShift dans l'environnement cible.

## Données persistées

- `project` : métadonnées du dépôt (nom, groupe parent, archivage, date d'activité).
- `branch` : branches collectées, statut main/protégée, date du dernier commit.
- `merge_request` : titres, auteurs, reviewers et statut des PR/MR.
- `tech_stack` : langues et frameworks détectés avec leur version.
- `obsolescence` : synthèse de conformité vis-à-vis de la matrice d'obsolescence.
- `file_check` : présence de Jenkinsfile, Dockerfile et pipeline CI.

## Documentation complémentaire

- [ARCHITECTURE](docs/ARCHITECTURE.md) : détails techniques et modules applicatifs.
- [FEATURES](docs/FEATURES.md) : synthèse des fonctionnalités multi-forge et gouvernance Git.

## Tests

Des tests unitaires couvrent :

- la comparaison de versions sémantiques (`SemanticVersionComparator`),
- la détection d'obsolescence (`DefaultObsolescenceDetectorService`),
- la vérification Git Flow (`DefaultComplianceCheckerService`).

Exécution :

```bash
mvn test
```
