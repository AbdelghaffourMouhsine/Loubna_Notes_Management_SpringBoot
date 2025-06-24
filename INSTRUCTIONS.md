# Système de Gestion des Utilisateurs

## Configuration de la Base de Données

1. Assurez-vous que MySQL est installé et en cours d'exécution
2. Créez la base de données `notes_db` (ou laissez l'application la créer automatiquement)
3. Utilisateur MySQL : `root` sans mot de passe

## Démarrage de l'Application

1. Exécutez : `mvn spring-boot:run`
2. Accédez à : `http://localhost:8080`

## Connexion par Défaut

- **Login :** admin
- **Mot de passe :** admin123

## Fonctionnalités Disponibles

### ADMIN_USER
- Rechercher et ajouter des personnes
- Créer des comptes utilisateurs avec rôles
- Gérer les comptes (activer/désactiver, verrouiller/déverrouiller)
- Réinitialiser les mots de passe
- Changer les rôles des utilisateurs

### ADMIN_NOTES et ADMIN_SP
- Dashboards avec message d'attente
- Fonctionnalités à implémenter dans les prochaines phases

## Structure des Rôles

- **ADMIN_USER :** Gestion des utilisateurs et comptes
- **ADMIN_NOTES :** Gestion des notes (à venir)
- **ADMIN_SP :** Gestion des structures pédagogiques (à venir)

## Notes Importantes

- Un seul compte ADMIN_USER peut exister dans le système
- Les mots de passe sont automatiquement générés et hachés
- Les logins sont générés automatiquement (nom+prénom)
- Le système gère les doublons de login automatiquement
