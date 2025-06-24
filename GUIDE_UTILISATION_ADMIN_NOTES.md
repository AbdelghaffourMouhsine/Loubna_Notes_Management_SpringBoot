# 🎓 Guide d'Utilisation - ADMIN_NOTES

## 🚀 Démarrage Rapide

### Accès à l'Application
1. **Connexion** : Utilisez vos identifiants ADMIN_NOTES
2. **Dashboard** : `/admin/notes/dashboard` - Vue d'ensemble et accès rapides
3. **Navigation** : Menu latéral pour accéder aux différentes sections

---

## 📋 Fonctionnalités Principales

### 1. 👥 **Gestion des Étudiants**

#### ➕ **Créer un Nouvel Étudiant**
- **Accès** : Dashboard → "Nouvel Étudiant" OU `/admin/notes/etudiants/new`
- **Champs requis** : CNE, Nom, Prénom
- **Validation** : Le CNE doit être unique dans le système

#### ✏️ **Modifier un Étudiant**
- **Accès** : Liste étudiants → Icône "Modifier" OU Détail étudiant → "Modifier"
- **Fonctionnalités** :
  - Modification CNE, nom, prénom
  - Vérification automatique d'unicité du CNE
  - Sauvegarde dans l'historique des modifications

#### 🗑️ **Supprimer un Étudiant**
- **Accès** : Liste OU détail étudiant → Bouton "Supprimer"
- **Sécurité** : Confirmation obligatoire + contrôles (étudiant avec notes = suppression interdite)

### 2. 🔍 **Recherche et Consultation**

#### 🔎 **Recherche Avancée**
- **Accès** : `/admin/notes/etudiants` → "Recherche Avancée"
- **Critères disponibles** :
  - **CNE** : Recherche partielle
  - **Nom/Prénom** : Recherche partielle insensible à la casse
  - **Niveau** : Sélection dans liste déroulante
  - **Année universitaire** : Format AAAA/AAAA (ex: 2024/2025)

#### 📊 **Consultation par Classe**
- **Accès** : `/admin/notes/etudiants/classe/{niveauId}?anneeUniversitaire=2024/2025`
- **Informations** : Effectif, statistiques, liste complète
- **Actions** : Sélection multiple, export, impression

#### 📄 **Détail d'un Étudiant**
- **Accès** : Liste → Icône "Voir" OU clic sur nom
- **Contenu** :
  - Informations personnelles complètes
  - Historique des modifications récentes
  - Actions rapides (modifier, historique, changer niveau)

### 3. 📈 **Historique et Suivi**

#### 📚 **Historique Complet des Modifications**
- **Accès** : Détail étudiant → "Voir l'historique"
- **Informations** :
  - Toutes les modifications avec ancien/nouveau
  - Utilisateur et date/heure précise
  - Statistiques des modifications

#### 🔄 **Modification de Niveau**
- **Accès** : Détail étudiant → "Modifier le niveau"
- **Processus** :
  1. Sélectionner nouveau niveau
  2. Saisir année universitaire
  3. Validation et création nouvelle inscription

### 4. 📥 **Export et Rapports**

#### 📊 **Export CSV**
- **Export global** : Résultats de recherche → "Exporter"
- **Export classe** : Vue classe → "Exporter la classe"
- **Export sélection** : Cocher étudiants → "Exporter sélectionnés"

#### 📋 **Format d'Export**
```
CNE | Nom | Prénom | Niveau | Filière | Année | Type Inscription | Date | Notes | Moyenne | Statut
```

---

## 🎯 Cas d'Usage Fréquents

### ✅ **Rechercher un Étudiant par CNE**
1. Aller à `/admin/notes/etudiants`
2. Développer "Recherche Avancée"
3. Saisir le CNE
4. Cliquer "Rechercher"

### ✅ **Modifier les Informations d'un Étudiant**
1. Rechercher l'étudiant
2. Cliquer sur l'icône "Modifier" (crayon)
3. Modifier les champs nécessaires
4. Cliquer "Modifier" → Sauvegarde automatique dans l'historique

### ✅ **Voir Tous les Étudiants d'une Classe**
1. Aller à la recherche avancée
2. Sélectionner le niveau (ex: GI1)
3. Saisir l'année universitaire (ex: 2024/2025)
4. Cliquer "Rechercher"

### ✅ **Exporter une Liste d'Étudiants**
1. Effectuer une recherche (ou voir tous)
2. Optionnel : Sélectionner des étudiants spécifiques
3. Cliquer "Exporter" ou "Exporter sélectionnés"
4. Le fichier CSV se télécharge automatiquement

### ✅ **Consulter l'Historique des Modifications**
1. Aller au détail d'un étudiant
2. Cliquer "Voir l'historique"
3. Consulter toutes les modifications avec dates et utilisateurs

---

## ⚠️ Points Importants

### 🛡️ **Sécurité et Contrôles**
- **CNE unique** : Le système vérifie automatiquement l'unicité
- **Suppression protégée** : Impossible si l'étudiant a des notes
- **Historique complet** : Toutes les modifications sont tracées
- **Utilisateur identifié** : Chaque action est liée à l'utilisateur connecté

### ⚡ **Performances**
- **Pagination** : 10 étudiants par page par défaut
- **Recherche optimisée** : Index sur CNE, nom, prénom
- **Export limité** : Respecter les limites raisonnables pour l'export

### 🎨 **Interface**
- **Responsive** : Fonctionne sur mobile, tablette, desktop
- **Intuitive** : Icônes explicites, couleurs cohérentes
- **Feedback** : Messages de succès/erreur pour chaque action

---

## 🆘 Dépannage

### ❌ **Problèmes Fréquents**

#### "CNE déjà existant"
- **Cause** : Un autre étudiant a déjà ce CNE
- **Solution** : Vérifier le CNE ou rechercher l'étudiant existant

#### "Impossible de supprimer l'étudiant"
- **Cause** : L'étudiant a des notes enregistrées
- **Solution** : Contacter l'administrateur pour supprimer les notes d'abord

#### "Étudiant non trouvé"
- **Cause** : L'étudiant a été supprimé ou l'ID est incorrect
- **Solution** : Refaire une recherche depuis la liste

#### Export CSV vide
- **Cause** : Aucun étudiant ne correspond aux critères
- **Solution** : Élargir les critères de recherche

### 📞 **Support**
- **Logs** : Toutes les actions sont enregistrées dans le journal
- **Historique** : Possibilité de consulter qui a fait quoi et quand
- **Sauvegarde** : Les modifications sont sauvegardées avant chaque changement

---

## 🔗 **Raccourcis Utiles**

| Action | URL Directe |
|--------|-------------|
| Dashboard | `/admin/notes/dashboard` |
| Liste étudiants | `/admin/notes/etudiants` |
| Nouvel étudiant | `/admin/notes/etudiants/new` |
| Recherche | `/admin/notes/etudiants/search` |

---

**💡 Conseil** : Utilisez le dashboard comme point de départ, il contient tous les raccourcis vers les fonctionnalités principales !

---

*Développé conformément aux spécifications du projet de gestion des notes - Module ADMIN_NOTES*
