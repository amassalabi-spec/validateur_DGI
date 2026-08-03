# 🚀 Guide d'Intégration Frontend-Backend

## Architecture Global

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  Frontend React (Vite)                                         │
│  ├── Port: 8443 (dev) / 5173 (default Vite)                   │
│  ├── Services HTTP: auth, invoice, analytics                  │
│  ├── State Management: React hooks + localStorage JWT          │
│  └── Components: Dashboard, Workspace, Registre, PdfStudio... │
│                                                                 │
│                           ↕ HTTP / CORS                        │
│                                                                 │
│  Backend Spring Boot 3.4.8 (Java 17)                          │
│  ├── Port: 8086                                                │
│  ├── Security: JWT Stateless + Multi-tenant (companyId)       │
│  ├── Database: MySQL (port 3305)                              │
│  ├── REST Endpoints:                                           │
│  │   ├── POST   /api/v1/auth/register                         │
│  │   ├── POST   /api/v1/auth/login                            │
│  │   ├── POST   /api/v1/invoices/upload                       │
│  │   ├── GET    /api/v1/invoices                              │
│  │   ├── GET    /api/v1/invoices/{id}/pdf                    │
│  │   ├── GET    /api/v1/analytics/company                    │
│  │   └── GET    /api/v1/analytics/admin                      │
│  └── Services:                                                 │
│      ├── Parsing: PoiExtractionService (DOCX/XLSX/PDF)       │
│      ├── Audit Engine: DgiAuditEngine (5 règles DGI)          │
│      ├── PDF Generation: PdfGeneratorService (Thymeleaf)      │
│      └── Auth: JwtTokenProvider, TenantContext (multi-tenant) │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Prérequis

### Système
- **Java 17+** (JDK installé)
- **Node.js 18+** & **npm** ou **pnpm**
- **MySQL 5.7+** (Service en cours d'exécution)
- **Git** (pour cloner si nécessaire)

### Configuration Base de Données

```bash
# Créer la base de données MySQL (Spring Boot le fera automatiquement avec ddl-auto=update)
# Mais vous pouvez la créer manuellement :
mysql -u root -p
> CREATE DATABASE validation;
> USE validation;
```

Vérifiez que les identifiants correspondent à `application.properties` :
```properties
spring.datasource.url=jdbc:mysql://localhost:3305/validation?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=DataBase@2026!
```

---

## Démarrage Rapide

### 1️⃣ Démarrer le Backend (Spring Boot)

```bash
cd C:\Users\amass\IdeaProjects\validateur_DGI

# Compiler et lancer l'application
./mvnw.cmd spring-boot:run

# Ou avec Maven classique
./mvnw.cmd clean package -DskipTests
java -jar target/validateur_DGI-0.0.1-SNAPSHOT.jar
```

**Attendu:**
```
...
[INFO] Started ValidateurDgiApplication in 12.345 seconds (JVM running for 15.678)
```

Le backend sera accessible à: **http://localhost:8086**

### 2️⃣ Démarrer le Frontend (React + Vite)

Dans un **nouveau terminal**:

```bash
cd "C:\Users\amass\IdeaProjects\validateur_DGI\frontend\SaaS B2B Fiscal Compliance App"

# Installer les dépendances (une seule fois)
npm install
# ou
pnpm install

# Démarrer le serveur de développement
npm run dev
# ou
pnpm dev
```

**Attendu:**
```
VITE v5.0.0  ready in 234 ms

➜  Local:   http://localhost:8443/
```

Le frontend sera accessible à: **http://localhost:8443** (ou un autre port disponible)

---

## Flux Utilisateur Complet

### 📝 Enregistrement (Sign-up)

1. **Frontend** → Ouvre page de login (non implémentée dans UI, endpoint existe)
2. **Utilisateur** → Entre `email` et `password`
3. **Frontend** → POST `/api/v1/auth/register`
4. **Backend** → Crée Company + AppUser, retourne JWT + UserInfo
5. **Frontend** → Stocke JWT dans localStorage, redirige vers Dashboard

### 🔐 Connexion (Login)

1. **Frontend** → POST `/api/v1/auth/login` avec `{ email, password }`
2. **Backend** → Valide credentials, retourne JWT + UserInfo
3. **Frontend** → Stocke JWT + User, configure Authorization header

### 📄 Upload et Audit de Facture

1. **Frontend (Workspace)** → Drag-drop fichier (`.docx`, `.xlsx`, ou `.pdf`)
2. **Frontend** → POST `/api/v1/invoices/upload` (multipart/form-data)
3. **Backend**:
   - Extrait texte avec `PoiExtractionService`
   - Parse avec `FallbackParsingService` (ou Spring AI si configuré)
   - Audit avec `DgiAuditEngine` (5 règles DGI)
   - Sauve Invoice + rapport audit en base
   - Retourne `InvoiceUploadResponse` avec anomalies
4. **Frontend (Workspace)** → Affiche anomalies en temps réel, permet corrections
5. **Utilisateur** → Soumet corrections (optionnel)
6. **Backend** → PUT `/api/v1/invoices/{id}/correct` → Ré-audit + PDF généré

### 📊 Consultation Registre et PDF

1. **Frontend (Registre)** → GET `/api/v1/invoices?page=0&size=20`
2. **Backend** → Retourne factures paginées filtrées par `companyId` (du JWT)
3. **Utilisateur** → Clique sur facture → GET `/api/v1/invoices/{id}/pdf`
4. **Backend** → Génère PDF via `PdfGeneratorService` (Thymeleaf + Flying Saucer)
5. **Frontend** → Télécharge PDF

### 📈 Dashboard BI

1. **Frontend (Dashboard)** → GET `/api/v1/analytics/company`
2. **Backend** → Retourne KPI:
   - TVA collectée par taux (ventilation)
   - CA HT/TTC
   - Taux conformité DGI %
   - Top 5 erreurs
3. **Frontend** → Affiche graphiques Recharts

---

## Architecture Frontend (React)

### Structure de Fichiers

```
frontend/
├── SaaS B2B Fiscal Compliance App/
│   ├── src/
│   │   ├── services/
│   │   │   ├── api.ts                  # Axios client + interceptors JWT
│   │   │   ├── authService.ts          # Authentification
│   │   │   ├── invoiceService.ts       # Upload, list, PDF download
│   │   │   └── analyticsService.ts     # KPI dashboard
│   │   ├── pages/
│   │   │   ├── Dashboard.tsx           # Charts BI (Recharts)
│   │   │   ├── Workspace.tsx           # Upload + Audit en direct
│   │   │   ├── Registre.tsx            # Tableau factures paginé
│   │   │   ├── PdfStudio.tsx           # Personnalisation PDF
│   │   │   └── Admin.tsx               # Profil, Équipe, Abonnement
│   │   ├── components/
│   │   │   ├── icons.tsx               # Icônes SVG
│   │   │   └── ui.tsx                  # Composants réutilisables
│   │   ├── App.tsx                     # Layout + Routing
│   │   ├── data.ts                     # Types + Données mock
│   │   ├── index.css                   # Tailwind + Custom
│   │   └── main.tsx                    # Entry point
│   ├── vite.config.ts                  # Config Vite
│   ├── tsconfig.json
│   └── package.json
```

### Services HTTP

#### 1. `api.ts` - Axios Client

```typescript
// Crée une instance Axios avec:
// - Base URL: http://localhost:8086/api/v1
// - Intercepteur request: ajoute JWT au header Authorization
// - Intercepteur response: gère 401 (logout si token expiré)
```

#### 2. `authService.ts`

```typescript
authService.register(email, password)    // POST /auth/register
authService.login(email, password)       // POST /auth/login
authService.logout()                     // Supprime JWT + redirige
authService.getCurrentUser()             // Récupère user depuis localStorage
authService.isAuthenticated()            // Vérifie si JWT existe
```

#### 3. `invoiceService.ts`

```typescript
invoiceService.upload(file)              // POST /invoices/upload (multipart)
invoiceService.list(page, size, status)  // GET /invoices?page=0&size=20
invoiceService.getById(id)               // GET /invoices/{id}
invoiceService.downloadPdf(id)           // GET /invoices/{id}/pdf (blob)
invoiceService.correct(id, corrections)  // PUT /invoices/{id}/correct
```

#### 4. `analyticsService.ts`

```typescript
analyticsService.getCompanyKpis()        // GET /analytics/company
analyticsService.getAdminKpis()          // GET /analytics/admin
```

---

## Architecture Backend (Spring Boot)

### Couches

#### 1. **Security** (`com.audit.dgi.validateur_dgi.security`)

- **TenantContext** → ThreadLocal pour stocker `companyId` par requête
- **JwtTokenProvider** → Crée/valide JWT (JJWT)
- **JwtAuthenticationFilter** → OncePerRequestFilter qui extrait JWT → SecurityContext + TenantContext
- **SecurityConfig** → Configuration stateless + CORS

#### 2. **Domain** (`com.audit.dgi.validateur_dgi.domain`)

- **Invoice** → Entité JPA (avec `companyId` pour multi-tenant)
- **InvoiceItem, InvoiceVatSummary, ComplianceAudit** → Sous-entités
- **Company, AppUser** → Entités de gestion
- **Enums** → PaymentMethod, AuditSeverity, InvoiceStatus, TemplateStyle

#### 3. **DTO** (`com.audit.dgi.validateur_dgi.dto`)

- **InvoiceDTO, IssuerDTO, ClientDTO** → DTOs de transfert avec validation Jakarta
- **AuthRequest, AuthResponse** → Pour auth
- **InvoiceListResponse** → Wrapper pagination

#### 4. **Engine** (`com.audit.dgi.validateur_dgi.engine`)

- **DgiRule** (interface) → Contrat pour règles d'audit
- **RuleResult** (record) → Résultat d'une règle
- **5 Implémentations:**
  - `IssuerFiscalIdentificationsRule` → Vérifie ICE/IF/RC/Patente
  - `ClientIceValidationRule` → ICE client obligatoire B2B
  - `MathematicalCoherenceRule` → Vérifie HT + TVA = TTC
  - `StampDutyRule` → Timbre fiscal si ESPÈCES
  - `VatExemptionClauseRule` → Clause CGI si TVA=0
- **DgiAuditEngine** → Orchestrateur qui exécute tous les règles

#### 5. **Parsing** (`com.audit.dgi.validateur_dgi.service.parser`)

- **PoiExtractionService** → Apache POI (DOCX/XLSX) + fallback PDF
- **SpringAiParsingService** (interface) → À implémenter avec ChatModel
- **FallbackParsingService** → Regex-based fallback parsing

#### 6. **PDF Generation** (`com.audit.dgi.validateur_dgi.service.generator`)

- **PdfGeneratorService** → Thymeleaf + Flying Saucer/ITextRenderer

#### 7. **Service** (Business Logic)

- **InvoiceService** → Upload → Extract → Parse → Audit → Save
- **AuthService** → Register + Login avec JWT

#### 8. **Controller** (REST API)

- **AuthController** → `/api/v1/auth/register`, `/api/v1/auth/login`
- **InvoiceController** → `/api/v1/invoices/upload`, `GET`, `/{id}/pdf`
- **AnalyticsController** → `/api/v1/analytics/company`, `/admin`

#### 9. **Repository** (Data Access)

- **InvoiceRepository** → `findByCompanyId(...)`, `findByCompanyIdAndStatus(...)`
- **UserRepository** → `findByEmail(...)`
- **CompanyRepository** → Accès Company

---

## Configuration JWT

### Propriétés (application.properties)

```properties
# JWT Secret — minimum 32 caractères pour HS256
app.jwt.secret=your-secret-key-here-minimum-32-characters-for-hs256

# Validité en millisecondes (86400000 = 24h)
app.jwt.validity=86400000
```

### Payload JWT

```json
{
  "sub": "user@example.com",      // Subject (email)
  "role": "ROLE_ADMIN",            // Rôle utilisateur
  "companyId": 123,                // Tenant ID pour multi-tenant
  "iat": 1234567890,               // Issued at
  "exp": 1234654290                // Expiration (24h)
}
```

---

## Multi-Tenancy

### Principe

Chaque facture appartient à une `company_id`. Le JWT contient le `companyId`. Le `JwtAuthenticationFilter` place ce `companyId` dans `TenantContext` (ThreadLocal).

### Isolation

```java
// ✅ Correct
Long companyId = TenantContext.getCurrentTenant();
Page<Invoice> invoices = invoiceRepository.findByCompanyId(companyId, pageRequest);

// ❌ Risqué (pas de filtre company_id)
List<Invoice> allInvoices = invoiceRepository.findAll();
```

---

## Appels API d'Exemple

### 1. Register

```bash
POST http://localhost:8086/api/v1/auth/register
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "StrongPass123!"
}

RESPONSE:
{
  "token": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "admin@example.com",
    "companyId": 1,
    "companyName": "Company for admin@example.com",
    "role": "ROLE_ADMIN"
  }
}
```

### 2. Login

```bash
POST http://localhost:8086/api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "StrongPass123!"
}

RESPONSE: [identique à register]
```

### 3. Upload Invoice

```bash
POST http://localhost:8086/api/v1/invoices/upload
Authorization: Bearer eyJhbGc...
Content-Type: multipart/form-data

[Binary DOCX/XLSX/PDF file]

RESPONSE:
{
  "id": 1,
  "invoiceNumber": "FAC-2026-0001",
  "status": "PENDING_AUDIT",
  "isCompliant": false,
  "auditReport": {
    "totalRules": 5,
    "passedRules": 3,
    "failedRules": 2,
    "errors": [
      {
        "ruleCode": "ICE-001",
        "fieldName": "clientIce",
        "severity": "ERROR",
        "message": "ICE Client manquant",
        ...
      }
    ]
  }
}
```

### 4. List Invoices

```bash
GET http://localhost:8086/api/v1/invoices?page=0&size=20&status=COMPLIANT
Authorization: Bearer eyJhbGc...

RESPONSE:
{
  "content": [...],
  "totalElements": 42,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 20
}
```

### 5. Download PDF

```bash
GET http://localhost:8086/api/v1/invoices/1/pdf
Authorization: Bearer eyJhbGc...

RESPONSE: [PDF binary]
```

---

## 🔧 Troubleshooting

### Frontend ne peut pas se connecter au backend

**Symptôme:** `CORS error`, `ERR_CONNECTION_REFUSED`

**Causes possibles:**
1. Backend n'est pas lancé (port 8086 fermé)
2. Mauvaise URL API (vérifier `api.ts` base URL)
3. Firewall bloque le port 8086

**Solutions:**
```bash
# Vérifier que le backend répond
curl -X GET http://localhost:8086/api/v1/auth/register

# Vérifier le port
netstat -ano | findstr :8086

# Relancer le backend
cd C:\Users\amass\IdeaProjects\validateur_DGI
./mvnw.cmd spring-boot:run
```

### JWT Token expiré

**Symptôme:** Erreur 401 après 24h

**Solution:** Logout automatique → Redirection login (implémentée dans `api.ts` interceptor)

### Base de données introuvable

**Symptôme:** `SQLException: Access denied for user 'root'@...`

**Solution:** Vérifier `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3305/validation
spring.datasource.username=root
spring.datasource.password=DataBase@2026!
```

Créer la DB si nécessaire:
```bash
mysql -u root -pDataBase@2026! -e "CREATE DATABASE IF NOT EXISTS validation;"
```

---

## 📚 Prochaines Étapes

### Phase 7: Intégration Spring AI

```java
// Remplacer FallbackParsingService par:
@Service
public class AiSpringParsingService implements SpringAiParsingService {
    private final ChatModel chatModel;
    private final BeanOutputConverter<InvoiceDTO> outputConverter;
    
    public InvoiceDTO parse(String rawText) {
        // Utiliser chatModel + prompt template
        // Retourner InvoiceDTO structuré
    }
}
```

### Phase 8: Frontend Components

Implémenter les pages manquantes:
- Page de Login/Register
- Correction manuelle d'anomalies dans Workspace
- PUT `/{id}/correct` endpoint
- Dashboard BI complet avec Recharts

### Phase 9: Tests & CI/CD

- Tests unitaires (JUnit 5)
- Tests d'intégration (MockMvc)
- GitHub Actions / GitLab CI
- Container Docker

---

## 📞 Support

Pour toute question ou problème:
1. Vérifiez les logs du backend: `target/` ou console de l'IDE
2. Ouvrez DevTools du navigateur (F12) pour voir les erreurs frontend
3. Consultez ce guide de troubleshooting ci-dessus

---

**Dernière mise à jour:** 2026-08-02  
**Version:** 1.0.0 (Frontend-Backend Integration)

