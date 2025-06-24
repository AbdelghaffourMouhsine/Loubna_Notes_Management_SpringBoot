# Application de Gestion des Notes

## Description
Application web de gestion des notes des étudiants d'une école d'ingénieurs développée avec Spring Boot.

## Architecture du Projet

### Structure des packages
```
com.example.gestion_de_notes/
├── bootstrap/          # Initialisation des données
├── config/            # Configuration Spring Security
├── controller/        # Contrôleurs MVC
├── dto/              # Data Transfer Objects
├── entity/           # Entités JPA
├── repository/       # Repositories Spring Data JPA
└── service/          # Services métier
```

### Entités principales

#### 1. Gestion des structures pédagogiques
- **Filiere** : Représente une filière (GI, GC, etc.)
- **Niveau** : Représente un niveau d'étude (GI1, GI2, etc.)
- **Module** : Représente un module d'enseignement
- **Element** : Représente un élément/matière d'un module
- **EnseignantModuleAnnee** : Association enseignant-module pour une année
- **ParametreValidation** : Paramètres de validation par filière/niveau

#### 2. Gestion des étudiants
- **Etudiant** : Représente un étudiant
- **Inscription** : Inscription d'un étudiant dans un niveau
- **InscriptionModule** : Inscription d'un étudiant dans un module
- **HistoriqueModificationEtudiant** : Historique des modifications

#### 3. Gestion des notes
- **Note** : Note d'un étudiant pour un élément
- **Session** : Type de session (Normale/Rattrapage)
- **FormuleCalcul** : Formules de calcul pour Excel

#### 4. Gestion des utilisateurs et logs
- **Personne** : Données personnelles
- **CompteUtilisateur** : Compte d'accès à l'application
- **Role** : Rôles (ADMIN_USER, ADMIN_NOTES, ADMIN_SP)
- **JournalApplication** : Journal des actions
- **HistoriqueConnexion** : Historique des connexions

## Fonctionnalités implémentées

### 1. Gestion des structures pédagogiques
- ✅ Entités créées
- ✅ Repositories créés
- ✅ Services de base créés
- ✅ Contrôleur pour les filières
- ⏳ CRUD complet pour modules/éléments
- ⏳ Import de structure pédagogique depuis Excel

### 2. Gestion des étudiants
- ✅ Entités créées
- ✅ Repositories créés
- ✅ Services de base créés
- ✅ Contrôleur CRUD pour étudiants
- ✅ Service d'import d'étudiants depuis Excel
- ⏳ Gestion des étudiants ajournés
- ⏳ Interface de recherche avancée

### 3. Gestion des comptes et sécurité
- ✅ Authentification Spring Security
- ✅ Gestion des rôles
- ✅ Contrôleur admin utilisateurs
- ✅ Historique et logs
- ⏳ Interface de gestion des permissions

## Technologies utilisées
- **Spring Boot 3.5.3**
- **Spring Data JPA** - Accès aux données
- **Spring Security** - Sécurité et authentification
- **Thymeleaf** - Templates web
- **MySQL** - Base de données
- **Apache POI** - Import/Export Excel
- **Lombok** - Réduction du code boilerplate
- **Jakarta Validation** - Validation des données

## Configuration

### Base de données
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/notes_db
spring.datasource.username=root
spring.datasource.password=
```

### Compte administrateur par défaut
- **Login** : admin
- **Mot de passe** : admin123
- **Rôle** : ADMIN_USER

## Structure de la base de données

### Tables principales
1. `personne` - Données personnelles
2. `compte_utilisateur` - Comptes d'accès
3. `filiere` - Filières d'étude
4. `niveau` - Niveaux d'étude (avec données pré-initialisées)
5. `module` - Modules d'enseignement
6. `element` - Éléments/matières
7. `etudiant` - Étudiants
8. `inscription` - Inscriptions des étudiants
9. `inscription_module` - Inscriptions aux modules
10. `note` - Notes des étudiants
11. `parametre_validation` - Paramètres de validation
12. `enseignant_module_annee` - Affectations enseignants
13. `historique_modification_etudiant` - Historique modifications
14. `journal_application` - Journal des actions
15. `historique_connexion` - Historique connexions
16. `formule_calcul` - Formules de calcul

## Prochaines étapes de développement

### Priorité haute
1. Finaliser les contrôleurs pour modules et éléments
2. Implémenter la génération de fichiers Excel pour collecte de notes
3. Créer les interfaces de délibération
4. Développer l'import/export de structures pédagogiques

### Priorité moyenne
1. Créer les vues Thymeleaf
2. Implémenter la collecte et validation des notes
3. Développer les rapports et statistiques
4. Améliorer l'interface utilisateur

### Priorité basse
1. Tests unitaires et d'intégration
2. Documentation utilisateur
3. Optimisations de performance
4. Fonctionnalités avancées

## Installation et démarrage

1. Cloner le projet
2. Configurer MySQL et créer la base `notes_db`
3. Lancer l'application : `mvn spring-boot:run`
4. Accéder à http://localhost:8080
5. Se connecter avec admin/admin123

## Contact
Développé pour le Master Génie Logiciel pour le Cloud Computing
