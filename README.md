# Andara's World

**Andara's World** is a single-player, party-based tactical RPG set in a post-apocalyptic Earth where fantasy, science fiction, and survival collide. Players lead a small team through a chaotic, emergent world—exploring wastelands, engaging in tactical combat, crafting from scavenged resources, and writing their own story through action rather than scripted narrative.

## Project Status

This project is in early development. The current focus is on establishing the foundational architecture and implementing the core gameplay loop for the prototype phase.

## Architecture

- **Backend**: Spring Boot 3.x (Java 17+) with modular monolith structure
- **Frontend**: React 18+ with TypeScript, Redux Toolkit
- **Database**: PostgreSQL 15+
- **Event Streaming**: Apache Kafka
- **Architecture Pattern**: Event Sourcing + CQRS

## Prerequisites

- **Java**: 17 or 21
- **Node.js**: 20.x (LTS)
- **Docker**: 20.10+ and Docker Compose
- **Gradle**: 8.0+ (wrapper included)

## Local Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd andaras-world
```

### 2. Start Infrastructure Services

Start PostgreSQL and Kafka using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- Zookeeper on port 2181
- Kafka on port 9092

### 3. Backend Setup

```bash
cd andara-server
# First time setup: Download Gradle wrapper (if not already present)
./gradlew wrapper

# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The backend will start on `http://localhost:8080`

**Note**: On first run, you may need to download the Gradle wrapper jar. The `gradlew` script will handle this automatically.

### 4. Frontend Setup

In a new terminal:

```bash
cd andara-client
npm install
npm run dev
```

The frontend will start on `http://localhost:3000` (or the port configured in Vite)

## Project Structure

```
andaras-world/
├── andara-server/          # Spring Boot backend (modular monolith)
│   ├── andara-api/        # REST controllers, WebSocket handlers
│   ├── andara-application/# Application services, command handlers
│   ├── andara-domain/     # Domain model, aggregates, events
│   ├── andara-infrastructure/ # Persistence, Kafka, external services
│   ├── andara-query/      # Read models, projections, query services
│   ├── andara-common/    # Shared utilities, value objects
│   └── andara-server-app/# Main application class
├── andara-client/         # React frontend
│   ├── src/
│   │   ├── api/          # API client
│   │   ├── store/        # Redux store
│   │   ├── components/   # React components
│   │   └── game/         # Game engine (WebGL renderer)
│   └── public/           # Static assets
├── docker/                # Docker configuration files
├── docs/                  # Project documentation
└── docker-compose.yml     # Local development environment
```

## Documentation

- [Project Inception](docs/andaras_world_inception.md) - Vision, scope, and project overview
- [Game Design Document](docs/andaras_world_gdd.md) - Game mechanics and systems
- [Domain Model](docs/andaras_world_domain_model.md) - Domain-driven design structure
- [Architecture Definition](docs/andaras_world_architecture.md) - Technical architecture

## Development

### Running Tests

**Backend:**
```bash
cd andara-server
./gradlew test
```

**Frontend:**
```bash
cd andara-client
npm test
```

### Code Style

- Backend: Follow Java conventions, use `.editorconfig`
- Frontend: ESLint and Prettier configured

## Environment Variables

Copy `.env.example` files and configure as needed:
- Backend: `andara-server/.env.example`
- Frontend: `andara-client/.env.example`

## Contributing

This is a personal project in early development. See documentation for architecture and design decisions.

## License

[To be determined]

