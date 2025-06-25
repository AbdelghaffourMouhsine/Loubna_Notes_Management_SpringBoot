# Guide d'Utilisation - Import et Gestion des Étudiants

## Vue d'ensemble

Ce guide explique comment utiliser les fonctionnalités d'import et de gestion des étudiants pour le rôle **ADMIN_NOTES**.

## Fonctionnalités Implémentées

### 1. Import Avancé des Étudiants

#### Caractéristiques principales :
- **Validation complète** du fichier Excel avant import
- **Détection et résolution des conflits** de données
- **Gestion spéciale des étudiants ajournés**
- **Inscription automatique dans les modules**
- **Historique et journalisation** de toutes les actions

#### Processus d'import en 3 étapes :

##### Étape 1 : Validation du fichier
- Upload du fichier Excel (.xlsx)
- Vérification de la structure (colonnes requises)
- Validation des données (types, contraintes)
- Détection des conflits de données

##### Étape 2 : Résolution des conflits (si nécessaire)
- Interface pour résoudre les conflits de données
- Choix de mise à jour ou conservation des données
- Prévisualisation des modifications

##### Étape 3 : Exécution de l'import
- Confirmation des paramètres
- Import des données avec gestion des erreurs
- Rapport détaillé des opérations effectuées

### 2. Template Excel

#### Téléchargement du template :
- Accès via le bouton "Télécharger le template"
- Fichier Excel avec 3 feuilles :
  - **Template Import Etudiants** : Structure et exemples
  - **Instructions** : Guide détaillé d'utilisation
  - **Niveaux Disponibles** : Liste complète des niveaux

#### Structure du fichier :
```
| ID_ETUDIANT | CNE | NOM | PRENOM | ID_NIVEAU_ACTUEL | TYPE |
|-------------|-----|-----|---------|------------------|------|
| ID Etudiant 1 | CNE001 | ALAMI | Ahmed | 1 | INSCRIPTION |
| ID Etudiant 2 | CNE002 | BENALI | Fatima | 2 | REINSCRIPTION |
```

### 3. Gestion des Étudiants Ajournés

#### Fonctionnalités :
- **Liste automatique** des étudiants ajournés
- **Modules obligatoires** (non validés de l'année précédente)
- **Modules supplémentaires** (du niveau suivant)
- **Interface interactive** pour sélectionner les modules

#### Règles de gestion :
- Inscription automatique aux modules non validés
- Possibilité d'inscription aux modules du niveau suivant
- Suivi des inscriptions supplémentaires

## Guide d'utilisation Pas à Pas

### Accès à l'interface
1. Se connecter avec un compte **ADMIN_NOTES**
2. Aller dans **Gestion des Notes > Import Étudiants**

### Import d'un nouveau fichier

#### Préparation du fichier :
1. Télécharger le template Excel
2. Remplir les données selon la structure :
   - **ID_ETUDIANT** : Identifiant unique
   - **CNE** : Code National Étudiant (obligatoire, unique)
   - **NOM** : Nom de famille (obligatoire)
   - **PRENOM** : Prénom (obligatoire)
   - **ID_NIVEAU_ACTUEL** : ID du niveau (voir feuille "Niveaux Disponibles")
   - **TYPE** : "INSCRIPTION" ou "REINSCRIPTION"

#### Validation des règles métier :
- **INSCRIPTION** : Pour les nouveaux étudiants (ne doivent pas exister)
- **REINSCRIPTION** : Pour les anciens étudiants (doivent exister)
- **Cohérence des niveaux** : Le niveau doit être cohérent avec les résultats précédents

#### Processus d'import :
1. **Sélectionner l'année universitaire**
2. **Choisir le fichier Excel**
3. **Cliquer sur "Valider le fichier"**
4. **Résoudre les conflits** (si détectés)
5. **Confirmer et exécuter l'import**

### Gestion des conflits

#### Types de conflits :
- **Données différentes** : Nom/Prénom différent entre fichier et base
- **Actions possibles** :
  - ✅ **Mettre à jour** : Remplacer par les données du fichier
  - ❌ **Conserver** : Garder les données actuelles

#### Interface de résolution :
- **Table comparative** des données
- **Actions rapides** : Tout sélectionner/désélectionner
- **Prévisualisation** des modifications

### Gestion des étudiants ajournés

#### Accès :
- Via le bouton "Gérer les ajournés" sur la page d'import
- Ou directement : `/admin/notes/import/etudiants-ajournes`

#### Fonctionnalités :
1. **Visualisation automatique** des étudiants ajournés
2. **Modules obligatoires** (automatiquement inscrits)
3. **Sélection de modules supplémentaires** du niveau suivant
4. **Sauvegarde individuelle** par étudiant

## Règles de Validation

### Validation du fichier Excel :
- ✅ Format .xlsx uniquement
- ✅ 6 colonnes obligatoires dans l'ordre exact
- ✅ En-têtes correctes
- ✅ Données cohérentes (types, contraintes)

### Validation métier :

#### Pour les INSCRIPTIONS :
- ✅ CNE unique (n'existe pas en base)
- ✅ Niveau valide (existe en base)
- ✅ Données complètes (CNE, Nom, Prénom)

#### Pour les REINSCRIPTIONS :
- ✅ Étudiant existe en base (recherche par CNE)
- ✅ Niveau cohérent avec les résultats précédents
- ✅ Pas d'inscription en double pour la même année

### Gestion des étudiants ajournés :
- ✅ Inscription automatique aux modules non validés
- ✅ Inscription optionnelle aux modules du niveau suivant
- ✅ Vérification de la disponibilité du niveau suivant

## Messages et Notifications

### Types de messages :
- 🟢 **Succès** : Import réussi avec statistiques
- 🟡 **Avertissements** : Actions effectuées avec notes
- 🔴 **Erreurs** : Problèmes bloquants détaillés

### Journalisation :
- **Historique des modifications** : Sauvegarde des anciennes données
- **Journal d'application** : Enregistrement de toutes les actions
- **Traçabilité complète** : Utilisateur, date, détails

## Exemples Pratiques

### Exemple 1 : Import de nouveaux étudiants
```excel
ID_ETUDIANT | CNE    | NOM    | PRENOM | ID_NIVEAU_ACTUEL | TYPE
ETU001      | CNE001 | ALAMI  | Ahmed  | 1               | INSCRIPTION
ETU002      | CNE002 | BENALI | Fatima | 1               | INSCRIPTION
```

### Exemple 2 : Réinscription avec conflit
```excel
ID_ETUDIANT | CNE    | NOM     | PRENOM   | ID_NIVEAU_ACTUEL | TYPE
ETU003      | CNE003 | CHAKIRI | Mohammed | 2               | REINSCRIPTION
```
Si en base : NOM="CHAKIR", PRENOM="Mohamed"
→ Conflit détecté → Interface de résolution

### Exemple 3 : Étudiant ajourné
- **Niveau actuel** : GI1 (Génie Informatique 1)
- **Modules non validés** : Mathématiques, Physique
- **Niveau suivant disponible** : GI2 (Génie Informatique 2)
- **Action** : Inscription automatique aux modules non validés + possibilité de modules GI2

## Dépannage

### Erreurs communes :

#### "Format de fichier incorrect"
- Vérifier l'extension .xlsx
- Télécharger le template officiel

#### "En-tête incorrect à la colonne X"
- Respecter l'ordre exact des colonnes
- Pas d'espaces supplémentaires dans les en-têtes

#### "Niveau introuvable"
- Vérifier l'ID_NIVEAU_ACTUEL
- Consulter la feuille "Niveaux Disponibles"

#### "CNE déjà existant"
- Pour INSCRIPTION : Utiliser REINSCRIPTION
- Vérifier l'unicité des CNE dans le fichier

#### "Étudiant introuvable"
- Pour REINSCRIPTION : Vérifier que l'étudiant existe
- Contrôler l'orthographe du CNE

### Support :
- **Logs détaillés** : Chaque erreur est contextualisée
- **Numéro de ligne** : Identification précise des problèmes
- **Messages explicites** : Description claire des actions requises

## Architecture Technique

### Services implémentés :
- `ImportEtudiantServiceAdvanced` : Logique d'import avancée
- `InscriptionModuleSupplementaireService` : Gestion modules supplémentaires
- `ExcelTemplateService` : Génération de templates
- `HistoriqueModificationEtudiantService` : Traçabilité
- `JournalApplicationService` : Journalisation

### Contrôleurs :
- `ImportEtudiantAdvancedController` : Endpoints d'import
- Sécurité : `@PreAuthorize("hasRole('ADMIN_NOTES')")`

### Templates Thymeleaf :
- `form-advanced.html` : Interface principale
- `resoudre-conflits.html` : Gestion des conflits
- `confirmer.html` : Confirmation d'import
- `etudiants-ajournes.html` : Gestion des ajournés

Cette implémentation respecte intégralement les spécifications du cahier des charges et offre une interface utilisateur moderne et intuitive.
