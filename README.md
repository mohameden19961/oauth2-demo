# 🔐 Centralized Auth Server (OAuth2 + JWT)

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Un serveur d'authentification **prêt pour la production**, conçu pour servir d'identité centrale à tous vos projets. Il transforme la connexion Google OAuth2 en jetons JWT sécurisés et persistants en base de données.

---

## ✨ Pourquoi utiliser ce projet ?
- **SSO (Single Sign-On)** : Une seule connexion pour toutes vos applications.
- **Sécurité Hybride** : La légèreté des JWT combinée à la sécurité d'une base de données (permet la révocation instantanée des tokens).
- **Multi-Projets** : Redirection dynamique vers vos différentes applications via `redirect_uri`.
- **Cloud-Ready** : Déploiement optimisé pour Docker et Render.

---

## 🚀 Démarrage Rapide

### 1. Variables d'Environnement
Ne modifiez jamais `application.properties`. Configurez ces variables dans votre environnement :

| Variable | Description |
| :--- | :--- |
| `GOOGLE_CLIENT_ID` | Votre ID client Google Cloud |
| `GOOGLE_CLIENT_SECRET` | Votre secret client Google Cloud |
| `DB_URL` | URL JDBC PostgreSQL (ex: Supabase) |
| `DB_USER` / `DB_PASSWORD` | Identifiants de votre base de données |
| `JWT_SECRET` | Une clé secrète de 32 caractères minimum |

### 2. Lancement Docker
```bash
docker build -t oauth2-auth .
docker run -p 8080:8080 --env-file .env oauth2-auth
```

---

## 🛠️ Documentation de l'API

### Authentification
- **`GET /auth/login`** : Lance le flux Google.
  - *Paramètre :* `?redirect_uri=...` (Optionnel)
- **`POST /api/auth/logout`** : Invalide la session et supprime les cookies.
- **`POST /api/auth/refresh`** : Renouvelle l'Access Token.

### Validation (Pour vos autres Backends)
- **`GET /api/auth/validate`** :
  - *Header :* `Authorization: Bearer <TOKEN>`
  - *Réponse :* `{"valid": true, "email": "...", "role": "USER"}`

---

## 🌐 Déploiement sur Render
Ce projet est optimisé pour un déploiement gratuit sur [Render.com](https://render.com) :
1. Créez un **Web Service**.
2. Sélectionnez le runtime **Docker**.
3. Ajoutez les variables d'environnement listées plus haut.
4. Dans Google Cloud, n'oubliez pas d'ajouter votre URL Render (ex: `https://votre-app.onrender.com/login/oauth2/code/google`) dans les **Authorized Redirect URIs**.

---

## ⚙️ Architecture Technique
- **Backend** : Spring Boot 3 + Spring Security + JJWT.
- **Database** : PostgreSQL (gérée par Spring Data JPA).
- **Cleanup** : Task scheduler intégré pour nettoyer les tokens expirés toutes les heures.
- **Proxy** : Support natif des headers X-Forwarded-For pour le déploiement Cloud.

---
Développé avec ❤️ pour simplifier l'authentification moderne.
