# 🔐 OAuth2 & JWT Centralized Auth Server

Un serveur d'authentification prêt à l'emploi (Spring Boot 3) qui permet de centraliser l'authentification OAuth2 (Google, GitHub) et de délivrer des JWT sécurisés pour tous tes autres projets.

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

## 📖 Guide d'utilisation (Auth-as-a-Service)

### 1. Pour vos applications clientes (Frontend)
Redirigez l'utilisateur vers cet URL pour lancer le login :
`GET /auth/login?redirect_uri=https://mon-app.com/callback`

Après le succès de Google Login, le serveur renverra l'utilisateur vers votre `redirect_uri` avec les tokens :
`https://mon-app.com/callback?access_token=...&refresh_token=...`

### 2. Pour vos autres Backends (Validation)
Vos autres serveurs peuvent vérifier la validité d'un token reçu d'un client :
`GET /api/auth/validate`
Header: `Authorization: Bearer <access_token>`

**Réponse JSON :**
```json
{
  "valid": true,
  "email": "user@example.com",
  "role": "USER"
}
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
Spring Boot 3.4.5, Spring Security, JJWT, PostgreSQL.
