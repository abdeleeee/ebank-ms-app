# ebank-ms-app

Application bancaire en **architecture microservices** construite avec **Spring Boot 4.1.2**, **Spring Cloud** et **Spring AI**. Elle gère des clients et des comptes bancaires, et expose un **chatbot IA** capable d'interroger les données clients via le protocole **MCP**.

## Architecture

| Module | Port | Rôle |
|---|---|---|
| `discovery-service` | 8761 | Serveur de découverte **Eureka** |
| `customer-service` | 8056 | Gestion des clients (REST + serveur **MCP**) |
| `ebank-service` | 8057 | Gestion des comptes bancaires, appelle `customer-service` via **OpenFeign** avec **Resilience4j** (circuit breaker) |
| `gateway-service` | 9999 | **Spring Cloud Gateway** (point d'entrée), enregistré dans Eureka |
| `ebank-bot` | 8058 | Chatbot IA (**Spring AI** + OpenAI `gpt-4o`), client MCP de `customer-service` |

```
                 ┌──────────────────┐
                 │ discovery-service│  (Eureka, 8761)
                 └────────▲─────────┘
        enregistrement    │
 ┌────────────┬───────────┼───────────┬──────────────┐
 │            │           │           │              │
gateway   customer-    ebank-      ebank-bot         │
(9999)    service      service     (8058)            │
          (8056)  ◄──Feign── (8057)  │               │
             ▲                       │               │
             └───────── MCP ─────────┘
```

## Technologies

- Java 21, Maven (Maven Wrapper inclus)
- Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA
- Spring Cloud 2025.1.4 : Eureka, Gateway, OpenFeign, Resilience4j
- Spring AI 2.0.1 : OpenAI, MCP client / MCP server
- Base de données **H2** en mémoire
- Lombok, Springdoc OpenAPI (Swagger UI), Spring Boot Actuator

## Prérequis

- JDK 21
- Maven 3.9+ (ou `mvnw`)
- Une clé API OpenAI (uniquement pour `ebank-bot`)

## Configuration de la clé OpenAI

Ne **jamais** écrire la clé API en clair dans le dépôt. Dans `ebank-bot/src/main/resources/application.properties`, utilisez une variable d'environnement :

```properties
spring.ai.openai.api-key=${OPENAI_API_KEY}
```

Puis définissez la variable avant de lancer le module :

```bash
# Linux / macOS
export OPENAI_API_KEY=votre_cle

# Windows (PowerShell)
$env:OPENAI_API_KEY="votre_cle"
```

## Lancement

Démarrer les services **dans cet ordre**, chacun dans son terminal (ou via IntelliJ) :

```bash
cd discovery-service && ./mvnw spring-boot:run
cd customer-service  && ./mvnw spring-boot:run
cd ebank-service     && ./mvnw spring-boot:run
cd gateway-service   && ./mvnw spring-boot:run
cd ebank-bot         && ./mvnw spring-boot:run
```

Sous Windows, remplacer `./mvnw` par `mvnw.cmd`.

Le tableau de bord Eureka est disponible sur http://localhost:8761.

## API

### customer-service (8056)

| Méthode | URL | Description |
|---|---|---|
| GET | `/customers` | Liste des clients |
| GET | `/customers/{id}` | Client par identifiant |
| POST | `/customers` | Créer un client |

Le serveur MCP est exposé sur `http://localhost:8056/mcp`.

### ebank-service (8057)

| Méthode | URL | Description |
|---|---|---|
| GET | `/accounts` | Liste des comptes |
| GET | `/accounts/{id}` | Compte par identifiant |
| POST | `/accounts` | Créer un compte |

Si `customer-service` est indisponible, le circuit breaker renvoie un client par défaut (« Not available »).

### ebank-bot (8058)

| Méthode | URL | Description |
|---|---|---|
| GET | `/chat?query=...` | Pose une question au chatbot (par défaut : « Bonjour ») |

Exemple :

```
http://localhost:8058/chat?query=Donne-moi la liste des clients
```

## Outils de développement

- Console H2 (`ebank-service`) : http://localhost:8057/h2-console, JDBC URL `jdbc:h2:mem:account-db`
- Swagger UI : `/swagger-ui.html` sur `customer-service` (8056), `ebank-service` (8057) et `ebank-bot` (8058)
- Actuator : `/actuator` sur chaque service

## Structure du projet

```
ebank-ms-app/
├── discovery-service/
├── customer-service/
├── ebank-service/
├── gateway-service/
├── ebank-bot/
└── pom.xml
```

## Auteur

Abdelmoghit, groupId `net.abdelmoghit`
