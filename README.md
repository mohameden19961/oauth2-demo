# 🔐 OAuth2 & JWT Centralized Auth Server

Un serveur d'authentification prêt à l'emploi (Spring Boot 3) qui permet de centraliser l'authentification OAuth2 (Google) et de délivrer des JWT sécurisés pour tous tes autres projets.

## 🚀 Caractéristiques
- **Centralisé** : Un seul serveur pour authentifier tous tes backends (Node.js, Python, Laravel...).
- **Sécurisé** : Utilise OAuth2 standard et génère des Access/Refresh Tokens via JWT (HMAC-SHA).
- **Persistant** : Les tokens sont gérés en base de données (PostgreSQL) avec un nettoyage automatique des tokens expirés.
- **Réutilisable** : Intègre un endpoint `/auth/login` avec redirection configurable et un endpoint `/api/auth/validate`.

---

## 🛠️ Installation et Configuration

### 1. Cloner le projet
```bash
git clone https://github.com/votre-user/oauth2-demo.git
cd oauth2-demo
```

### 2. Configurer les secrets (Sécurité 🛡️)
Le projet utilise des variables d'environnement pour protéger vos clés. **Ne jamais modifier les clés directement dans `application.properties`.**

Créez un script ou configurez votre environnement avec les variables suivantes :

| Variable | Description | Où la trouver ? |
|----------|-------------|-----------------|
| `GOOGLE_CLIENT_ID` | Client ID Google OAuth2 | [Google Cloud Console](https://console.cloud.google.com/) |
| `GOOGLE_CLIENT_SECRET` | Secret Google OAuth2 | [Google Cloud Console](https://console.cloud.google.com/) |
| `DB_URL` | URL JDBC PostgreSQL | [Supabase Database Settings](https://supabase.com/) |
| `DB_USER` | Utilisateur DB | [Supabase Database Settings](https://supabase.com/) |
| `DB_PASSWORD` | Mot de passe DB | [Supabase Database Settings](https://supabase.com/) |
| `JWT_SECRET` | Clé secrète JWT (min 32 chars) | Une chaîne aléatoire que vous choisissez |

### 3. Lancer le projet
```bash
./mvnw spring-boot:run
```

---

## 🛠️ Documentation de l'API (Endpoints)

Voici toutes les requêtes que vous pouvez effectuer sur ce serveur :

### 1. Authentification et Session

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/auth/login` | **Initialiser la connexion**. Paramètre optionnel `?redirect_uri=...` pour rediriger après succès. |
| `GET` | `/api/auth/token` | **Récupérer les tokens**. Utilisé après le succès du login Google. |
| `POST` | `/api/auth/refresh` | **Rafraîchir le token**. Nécessite le `refresh_token` (en cookie ou header `X-Refresh-Token`). |
| `POST` | `/api/auth/logout` | **Déconnexion**. Invalide le token en BDD et supprime le cookie. |

**Exemple Logout :**
```bash
curl -X POST -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/auth/logout
```

### 2. Validation (Pour les autres Backends)

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/auth/validate` | Vérifie si le token est valide, non expiré et non révoqué. |

**Exemple de validation :**
```bash
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/auth/validate
```
*Réponse attendue :* `{"valid": true, "email": "...", "role": "USER"}`

### 3. Utilisateur

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/user/me` | Récupère le profil complet de l'utilisateur connecté. |

```bash
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/user/me
```

---

## 🔍 Comment obtenir les clés ?

### 🌐 Google OAuth2
1. Allez sur [Google Cloud Console](https://console.cloud.google.com/).
2. Créez un projet.
3. Allez dans **API et services > Identifiants**.
4. Créez un **ID de client OAuth 2.0** (Type: Application Web).
5. Ajoutez `http://localhost:8080/login/oauth2/code/google` dans les **URI de redirection autorisés**.

### ⚡ Supabase (PostgreSQL)
1. Créez un projet sur [Supabase](https://supabase.com/).
2. Allez dans **Project Settings > Database**.
3. Copiez l'URL de connexion dans la section **Connection String > JDBC**.

---

## 👨‍💻 Développé avec ❤️
Spring Boot 3.4.5, Spring Security, JJWT, PostgreSQL (Supabase).
