# Plan de tâches - Détecteur d'obsolescence

## 1. Migration Spring Boot 3.5.6
- Mettre à jour le parent `spring-boot-starter-parent` dans le `pom.xml` en version **3.5.6**.
- Vérifier la compatibilité Java (JDK 17+) et ajuster la configuration `application.yml` si nécessaire.
- Lancer `mvn clean verify` pour valider la compilation et les dépendances après migration.

## 2. Structuration du moteur d'obsolescence
- Créer le package `com.example.dashboardradar.obsolescence` et les modèles suivants :
  - `ComponentVersion` pour représenter un composant détecté.
  - `ObsolescenceRule`, `ObsolescenceMatrix` pour le mapping YAML.
  - `ObsolescenceFinding`, `ObsolescenceSeverity`, `ObsolescenceStatus` pour le résultat d'analyse.
- Définir l'interface `ObsolescenceDetectorService` (méthode `evaluate`).
- Implémenter la classe abstraite `AbstractYamlObsolescenceDetectorService` pour factoriser :
  - Chargement YAML via `ResourceLoader`.
  - Comparaison de versions.
  - Calcul des statuts (à jour, obsolète, déprécié, fin de support).

## 3. Implémentations par langage
- **Java** : `JavaObsolescenceDetectorService` charge `obsolescence/java.yml`.
- **Python** : `PythonObsolescenceDetectorService` charge `obsolescence/python.yml`.
- **Node.js / Angular** : `NodeJsObsolescenceDetectorService` charge `obsolescence/nodejs.yml`.
- Fournir un bean `Clock` dans `TimeConfiguration` pour faciliter les tests et le calcul des dates.

## 4. Matrices YAML d'obsolescence
- Ajouter un dossier `src/main/resources/obsolescence` contenant :
  - `java.yml` (Spring, Spring Boot, Hibernate, Jakarta EE, WildFly, EJB, Tomcat, Quarkus, Micronaut, Dropwizard, Struts, JSF, Jersey, Play, Grails...).
  - `python.yml` (Django, Flask, FastAPI, Pyramid, Tornado, Celery, SQLAlchemy, NumPy, Pandas...).
  - `nodejs.yml` (Node.js runtime, Angular core & CLI, RxJS).
- Chaque entrée définit : composant, version minimale, dernière version connue, dates de dépréciation/fin de support, sévérité.

## 5. Configuration applicative
- Mettre à jour `application.yml` pour pointer vers `dashboard.obsolescence.matrix-base-path`.
- Activer `@ConfigurationPropertiesScan` dans `DashboardRadarApplication` pour charger `ObsolescenceMatrixProperties`.

## 6. Prochaines étapes (optionnel)
- Exposer un service métier orchestrant les détecteurs par langage.
- Ajouter des tests unitaires ciblant la comparaison de versions et les scénarios de dates.
- Intégrer les résultats avec Spring Batch (`AuditTasklet`).
