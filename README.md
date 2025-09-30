# Dashboard Radar

Dashboard Radar est une application batch Spring Boot qui analyse quotidiennement les dépôts GitHub d'une organisation afin de détecter les dérives de conformité et les risques d'obsolescence technique.

## Fonctionnalités principales

- **Inventaire Git** : récupération des dépôts d'une organisation GitHub (branches, merge requests, langages, frameworks, arborescence).
- **Vérification Git Flow** : contrôle de la présence des branches principales (main/master, develop) et des conventions de nommage (feature/*, hotfix/*, release/*...).
- **Analyse de la structure projet** : vérification des chemins obligatoires, détection des fichiers CI/CD et identification des frameworks utilisés (pom.xml, build.gradle, package.json).
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
│ GithubScannerService (WebClient GitHub API)  │
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
dashboard:
  github:
    token: <token personnel GitHub>
    organization: <organisation cible>
  structure:
    stacks: # règles de structures par stack (Java Spring, Angular, ...)
  obsolescence:
    components: # matrice d'obsolescence (version minimale, dates de fin de support)
```

Les propriétés `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` permettent de piloter la connexion base (par défaut H2 en mémoire pour le développement ; dialecte PostgreSQL pour le déploiement).

## Lancement local

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -DGITHUB_TOKEN=<token> -DGITHUB_ORG=<organisation>
```

Le job Spring Batch se lance automatiquement au démarrage et peut être planifié via un CronJob OpenShift dans l'environnement cible.

## Données persistées

- `project` : métadonnées du dépôt (nom, groupe parent, archivage, date d'activité).
- `branch` : branches collectées, statut main/protégée, date du dernier commit.
- `merge_request` : titres, auteurs, reviewers et statut des PR/MR.
- `tech_stack` : langues et frameworks détectés avec leur version.
- `obsolescence` : synthèse de conformité vis-à-vis de la matrice d'obsolescence.
- `file_check` : présence de Jenkinsfile, Dockerfile et pipeline CI.

## Tests

Des tests unitaires couvrent :

- la comparaison de versions sémantiques (`SemanticVersionComparator`),
- la détection d'obsolescence (`DefaultObsolescenceDetectorService`),
- la vérification Git Flow (`DefaultComplianceCheckerService`).

Exécution :

```bash
mvn test
```
