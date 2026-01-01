# Andara's World — Architecture Definition Document

**Version**: 0.1  
**Date**: January 2026  
**Status**: Draft  
**Parent Document**: Project Inception Document v0.1

---

## 1. Overview

### 1.1 Purpose

This document defines the technical architecture for Andara's World. It specifies system components, their interactions, data flows, and implementation patterns. The architecture supports both the immediate prototype scope and the long-term vision of cross-instance synchronization.

### 1.2 Architectural Drivers

| Driver | Requirement | Implication |
|--------|-------------|-------------|
| **Event Sourcing** | All state changes as immutable events | Append-only event store, projections, replay capability |
| **CQRS** | Separate read/write models | Command handlers, query services, eventual consistency |
| **Single-Player First** | Full functionality offline | Local-first architecture, embedded services viable |
| **Future Sync** | Cross-instance markets, world events | Event-based sync protocol, conflict resolution |
| **Browser-Based** | WebGL rendering, React UI | Client-side state management, efficient data transfer |
| **Tactical Combat** | Grid-based, turn-based | Spatial indexing, deterministic resolution |

### 1.3 Technology Stack

| Layer | Technology | Rationale |
|-------|------------|-----------|
| **Frontend Framework** | React 18+ | Component model, ecosystem, team familiarity |
| **Rendering** | WebGL (PixiJS or custom) | 2D orthographic, sprite batching, performance |
| **State Management** | Redux Toolkit | Predictable state, devtools, middleware |
| **Backend Framework** | Spring Boot 3.x | Java ecosystem, mature, production-ready |
| **Database** | PostgreSQL 15+ | JSONB support, reliability, event store capability |
| **Event Streaming** | Apache Kafka | Event sourcing, future sync, scalability |
| **Build/Deploy** | Gradle, Docker | Containerization, reproducible builds |

---

## 2. System Context

### 2.1 Context Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              SYSTEM CONTEXT                                  │
│                                                                             │
│                         ┌─────────────────────┐                             │
│                         │                     │                             │
│         ┌──────────────►│   Andara's World    │◄──────────────┐             │
│         │               │      System         │               │             │
│         │               │                     │               │             │
│         │               └──────────┬──────────┘               │             │
│         │                          │                          │             │
│    ┌────┴────┐                     │                    ┌─────┴─────┐       │
│    │         │                     │                    │           │       │
│    │ Player  │                     │ (Future)           │   LLM     │       │
│    │         │                     │                    │  Service  │       │
│    └─────────┘                     │                    │           │       │
│                                    ▼                    └───────────┘       │
│                         ┌─────────────────────┐                             │
│                         │                     │                             │
│                         │  Global Sync        │ (Future)                    │
│                         │  Service            │                             │
│                         │                     │                             │
│                         └─────────────────────┘                             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Deployment Modes

#### Prototype Mode (Current Target)
- All services run locally (single JVM or Docker Compose)
- Kafka runs embedded or local container
- PostgreSQL runs local container
- No external dependencies
- Single player, single instance

#### Production Mode (Future)
- Backend services containerized (Kubernetes)
- Kafka cluster for event streaming
- PostgreSQL with replication
- Global sync service for cross-instance communication
- LLM service integration

---

## 3. High-Level Architecture

### 3.1 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                 CLIENT                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │              │  │              │  │              │  │              │    │
│  │  React UI    │  │  Game State  │  │   WebGL      │  │   Audio      │    │
│  │  Components  │  │  (Redux)     │  │   Renderer   │  │   Manager    │    │
│  │              │  │              │  │              │  │              │    │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────────────┘    │
│         │                 │                 │                               │
│         └────────────┬────┴────────────────┬┘                               │
│                      │                     │                                │
│              ┌───────┴───────┐     ┌───────┴───────┐                       │
│              │  API Client   │     │  Asset Loader │                       │
│              │  (REST/WS)    │     │               │                       │
│              └───────┬───────┘     └───────────────┘                       │
│                      │                                                      │
└──────────────────────┼──────────────────────────────────────────────────────┘
                       │
            ┌──────────┴──────────┐
            │    HTTP / WebSocket │
            └──────────┬──────────┘
                       │
┌──────────────────────┼──────────────────────────────────────────────────────┐
│                      │              SERVER                                   │
│              ┌───────┴───────┐                                              │
│              │  API Gateway  │                                              │
│              │  (Controllers)│                                              │
│              └───────┬───────┘                                              │
│                      │                                                      │
│    ┌─────────────────┼─────────────────┬─────────────────┐                 │
│    │                 │                 │                 │                 │
│    ▼                 ▼                 ▼                 ▼                 │
│ ┌──────┐        ┌──────┐        ┌──────┐        ┌──────┐                  │
│ │World │        │Party │        │Combat│        │Agent │                  │
│ │Service│       │Service│       │Service│       │Service│                 │
│ └──┬───┘        └──┬───┘        └──┬───┘        └──┬───┘                  │
│    │               │               │               │                       │
│    └───────────────┴───────┬───────┴───────────────┘                       │
│                            │                                                │
│                    ┌───────┴───────┐                                       │
│                    │ Command Bus   │                                       │
│                    └───────┬───────┘                                       │
│                            │                                                │
│    ┌───────────────────────┼───────────────────────┐                       │
│    │                       │                       │                       │
│    ▼                       ▼                       ▼                       │
│ ┌──────────┐        ┌──────────┐        ┌──────────┐                      │
│ │  Event   │        │   Read   │        │  Kafka   │                      │
│ │  Store   │◄──────►│  Models  │        │ Producer │                      │
│ │(Postgres)│        │(Postgres)│        │          │                      │
│ └──────────┘        └──────────┘        └────┬─────┘                      │
│                                              │                             │
└──────────────────────────────────────────────┼─────────────────────────────┘
                                               │
                                        ┌──────┴──────┐
                                        │    Kafka    │
                                        │   Cluster   │
                                        └─────────────┘
```

### 3.2 Layer Responsibilities

| Layer | Responsibility |
|-------|----------------|
| **Presentation** | React components, user interaction, visual rendering |
| **Client State** | Redux store, local game state, optimistic updates |
| **API Gateway** | REST endpoints, WebSocket connections, authentication |
| **Application Services** | Use cases, command handling, orchestration |
| **Domain** | Business logic, aggregates, domain events |
| **Infrastructure** | Persistence, messaging, external integrations |

---

## 4. Backend Architecture

### 4.1 Module Structure

The backend follows a modular monolith pattern, organized by bounded context:

```
andara-server/
├── andara-api/                 # REST controllers, WebSocket handlers
├── andara-application/         # Application services, command handlers
├── andara-domain/              # Domain model, aggregates, events
│   ├── world/
│   ├── party/
│   ├── encounter/
│   ├── agent/
│   ├── economy/
│   └── sync/
├── andara-infrastructure/      # Persistence, Kafka, external services
├── andara-query/               # Read models, projections, query services
└── andara-common/              # Shared utilities, value objects
```

### 4.2 Command Handling Flow

```
┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ Client  │───►│ Controller  │───►│  Command    │───►│  Aggregate  │
│ Request │    │             │    │  Handler    │    │             │
└─────────┘    └─────────────┘    └─────────────┘    └──────┬──────┘
                                                            │
                                                            ▼
┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ Client  │◄───│ Response    │◄───│  Event      │◄───│  Domain     │
│         │    │ DTO         │    │  Publisher  │    │  Events     │
└─────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                                         │
                                         ▼
                                  ┌─────────────┐
                                  │   Kafka     │
                                  │   Topic     │
                                  └─────────────┘
```

### 4.3 Command Example

```java
// Command
public record MovePartyCommand(
    PartyId partyId,
    ZoneId targetZoneId,
    AgentId issuedBy
) implements Command {}

// Command Handler
@Component
public class MovePartyCommandHandler implements CommandHandler<MovePartyCommand> {
    
    private final PartyRepository partyRepository;
    private final WorldRepository worldRepository;
    private final EventPublisher eventPublisher;
    
    @Override
    @Transactional
    public Result<List<DomainEvent>> handle(MovePartyCommand command) {
        // Load aggregates
        Party party = partyRepository.load(command.partyId());
        Zone currentZone = worldRepository.loadZone(party.currentZoneId());
        Zone targetZone = worldRepository.loadZone(command.targetZoneId());
        
        // Validate
        if (!currentZone.isAdjacentTo(targetZone)) {
            return Result.failure("Target zone is not adjacent");
        }
        
        // Execute domain logic
        List<DomainEvent> events = party.moveTo(targetZone, command.issuedBy());
        
        // Persist events
        partyRepository.save(party, events);
        
        // Publish to Kafka
        eventPublisher.publish(events);
        
        return Result.success(events);
    }
}
```

### 4.4 Event Store Schema

```sql
-- Event Store Table
CREATE TABLE domain_events (
    event_id        UUID PRIMARY KEY,
    event_type      VARCHAR(255) NOT NULL,
    aggregate_id    VARCHAR(255) NOT NULL,
    aggregate_type  VARCHAR(100) NOT NULL,
    instance_id     UUID NOT NULL,
    agent_id        UUID NOT NULL,
    sequence_number BIGINT NOT NULL,
    timestamp       TIMESTAMP WITH TIME ZONE NOT NULL,
    payload         JSONB NOT NULL,
    metadata        JSONB,
    
    UNIQUE (aggregate_id, aggregate_type, sequence_number)
);

CREATE INDEX idx_events_aggregate ON domain_events(aggregate_id, aggregate_type);
CREATE INDEX idx_events_instance ON domain_events(instance_id);
CREATE INDEX idx_events_timestamp ON domain_events(timestamp);
CREATE INDEX idx_events_type ON domain_events(event_type);

-- Snapshot Table (Optimization)
CREATE TABLE aggregate_snapshots (
    aggregate_id    VARCHAR(255) NOT NULL,
    aggregate_type  VARCHAR(100) NOT NULL,
    sequence_number BIGINT NOT NULL,
    snapshot_data   JSONB NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    
    PRIMARY KEY (aggregate_id, aggregate_type)
);
```

### 4.5 Aggregate Loading

```java
@Repository
public class EventSourcedPartyRepository implements PartyRepository {
    
    private final JdbcTemplate jdbc;
    private final SnapshotRepository snapshots;
    
    @Override
    public Party load(PartyId id) {
        // Try to load from snapshot first
        Optional<Snapshot> snapshot = snapshots.findLatest(id, "Party");
        
        long fromSequence = snapshot.map(Snapshot::sequenceNumber).orElse(0L);
        
        // Load events since snapshot
        List<DomainEvent> events = jdbc.query(
            """
            SELECT * FROM domain_events 
            WHERE aggregate_id = ? AND aggregate_type = 'Party'
            AND sequence_number > ?
            ORDER BY sequence_number
            """,
            eventRowMapper,
            id.value(), fromSequence
        );
        
        // Reconstitute aggregate
        Party party = snapshot
            .map(s -> Party.fromSnapshot(s.data()))
            .orElse(Party.empty(id));
            
        // Apply events
        for (DomainEvent event : events) {
            party.apply(event);
        }
        
        return party;
    }
}
```

---

## 5. Domain Services

### 5.1 World Service

Manages regions, zones, points of interest, and environmental state.

```java
@Service
public class WorldService {
    
    // Commands
    public Result<List<DomainEvent>> enterZone(EnterZoneCommand cmd);
    public Result<List<DomainEvent>> discoverPOI(DiscoverPOICommand cmd);
    public Result<List<DomainEvent>> triggerEnvironmentChange(EnvironmentChangeCommand cmd);
    
    // Queries (via Read Models)
    public ZoneView getZone(ZoneId id, AgentId viewer);
    public List<ZoneView> getAdjacentZones(ZoneId from);
    public RegionView getRegion(RegionId id);
}
```

### 5.2 Party Service

Manages party composition, character state, inventory, and skills.

```java
@Service
public class PartyService {
    
    // Commands
    public Result<List<DomainEvent>> moveParty(MovePartyCommand cmd);
    public Result<List<DomainEvent>> equipItem(EquipItemCommand cmd);
    public Result<List<DomainEvent>> useItem(UseItemCommand cmd);
    public Result<List<DomainEvent>> useSkill(UseSkillCommand cmd);
    
    // Queries
    public PartyView getParty(PartyId id);
    public CharacterView getCharacter(CharacterId id);
    public InventoryView getInventory(PartyId id);
}
```

### 5.3 Encounter Service

Manages combat and other encounter types.

```java
@Service
public class EncounterService {
    
    // Commands
    public Result<List<DomainEvent>> startCombat(StartCombatCommand cmd);
    public Result<List<DomainEvent>> executeAction(CombatActionCommand cmd);
    public Result<List<DomainEvent>> endTurn(EndTurnCommand cmd);
    public Result<List<DomainEvent>> resolveCombat(ResolveCombatCommand cmd);
    
    // Queries
    public CombatStateView getCombatState(EncounterId id);
    public List<AvailableAction> getAvailableActions(EncounterId id, AgentId agent);
}
```

### 5.4 Agent Service

Manages agent identity, sessions, and authorization.

```java
@Service
public class AgentService {
    
    // Commands
    public Result<List<DomainEvent>> registerAgent(RegisterAgentCommand cmd);
    public Result<List<DomainEvent>> startSession(StartSessionCommand cmd);
    public Result<List<DomainEvent>> endSession(EndSessionCommand cmd);
    
    // Authorization
    public boolean canExecute(AgentId agent, CommandType commandType);
    public Role getRole(AgentId agent);
}
```

---

## 6. Read Models (Projections)

### 6.1 Projection Architecture

Read models are derived from events and optimized for queries.

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Events    │────►│  Projection │────►│  Read Model │
│   (Kafka)   │     │  Handler    │     │  (Postgres) │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   Query     │
                    │   Service   │
                    └─────────────┘
```

### 6.2 Read Model Schemas

```sql
-- Party Read Model
CREATE TABLE party_view (
    party_id        UUID PRIMARY KEY,
    instance_id     UUID NOT NULL,
    position_region VARCHAR(255),
    position_zone   VARCHAR(255),
    member_count    INT NOT NULL,
    total_credits   BIGINT NOT NULL,
    formation       VARCHAR(50),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    data            JSONB NOT NULL  -- Full party state for complex queries
);

-- Character Read Model
CREATE TABLE character_view (
    character_id    UUID PRIMARY KEY,
    party_id        UUID NOT NULL,
    name            VARCHAR(255) NOT NULL,
    is_protagonist  BOOLEAN NOT NULL,
    health_current  INT NOT NULL,
    health_max      INT NOT NULL,
    status          VARCHAR(50) NOT NULL,
    attributes      JSONB NOT NULL,
    skills          JSONB NOT NULL,
    equipment       JSONB NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Zone Read Model (includes visibility per party)
CREATE TABLE zone_view (
    zone_id         UUID NOT NULL,
    party_id        UUID NOT NULL,  -- Visibility is per-party
    region_id       UUID NOT NULL,
    name            VARCHAR(255),
    terrain         VARCHAR(100),
    visibility      VARCHAR(50) NOT NULL,  -- hidden, known, explored
    discovered_pois JSONB,
    hazards         JSONB,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    
    PRIMARY KEY (zone_id, party_id)
);

-- Combat State Read Model
CREATE TABLE combat_view (
    encounter_id    UUID PRIMARY KEY,
    status          VARCHAR(50) NOT NULL,
    turn_number     INT NOT NULL,
    current_turn    UUID,  -- Agent whose turn it is
    initiative_order JSONB NOT NULL,
    combatants      JSONB NOT NULL,
    battlefield     JSONB NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### 6.3 Projection Handler Example

```java
@Component
public class PartyProjectionHandler {
    
    private final JdbcTemplate jdbc;
    
    @KafkaListener(topics = "party-events")
    public void handle(DomainEvent event) {
        switch (event) {
            case PartyCreated e -> createPartyView(e);
            case PartyMoved e -> updatePosition(e);
            case CharacterRecruited e -> addCharacter(e);
            case CharacterDied e -> updateCharacterStatus(e);
            case ItemAcquired e -> updateInventory(e);
            case SkillImproved e -> updateSkills(e);
            default -> {} // Ignore unhandled events
        }
    }
    
    private void updatePosition(PartyMoved event) {
        jdbc.update(
            """
            UPDATE party_view 
            SET position_region = ?, position_zone = ?, updated_at = ?
            WHERE party_id = ?
            """,
            event.newRegionId(),
            event.newZoneId(),
            event.timestamp(),
            event.partyId()
        );
    }
}
```

---

## 7. Kafka Architecture

### 7.1 Topic Design

```
andara.events.world          # World context events
andara.events.party          # Party context events  
andara.events.encounter      # Encounter context events
andara.events.agent          # Agent context events
andara.events.economy        # Economy context events (future)
andara.events.sync           # Sync context events (future)

andara.commands.pending      # Commands awaiting processing (optional)
andara.sync.outbound         # Events to sync globally (future)
andara.sync.inbound          # Events received from global (future)
```

### 7.2 Event Schema

```java
public record EventEnvelope(
    UUID eventId,
    String eventType,
    Instant timestamp,
    UUID instanceId,
    UUID agentId,
    String aggregateId,
    String aggregateType,
    long version,
    JsonNode payload,
    Map<String, String> metadata
) {}
```

### 7.3 Kafka Configuration

```yaml
# application.yml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
    consumer:
      group-id: andara-server
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.andara.domain.events"
```

### 7.4 Event Publisher

```java
@Component
public class KafkaEventPublisher implements EventPublisher {
    
    private final KafkaTemplate<String, EventEnvelope> kafka;
    
    @Override
    public void publish(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            String topic = topicFor(event);
            String key = event.aggregateId();
            EventEnvelope envelope = wrap(event);
            
            kafka.send(topic, key, envelope)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event: {}", event.eventId(), ex);
                    }
                });
        }
    }
    
    private String topicFor(DomainEvent event) {
        return switch (event.aggregateType()) {
            case "Party", "Character", "Inventory" -> "andara.events.party";
            case "Region", "Zone", "POI" -> "andara.events.world";
            case "Encounter", "CombatState" -> "andara.events.encounter";
            case "Agent", "Session" -> "andara.events.agent";
            default -> "andara.events.general";
        };
    }
}
```

---

## 8. API Design

### 8.1 REST API Structure

```
/api/v1
├── /game
│   ├── POST   /start              # Start new game
│   ├── POST   /load/{saveId}      # Load saved game
│   ├── POST   /save               # Save current game
│   └── GET    /state              # Get current game state
│
├── /party
│   ├── GET    /                   # Get party state
│   ├── POST   /move               # Move party to zone
│   ├── GET    /characters         # List characters
│   ├── GET    /characters/{id}    # Get character detail
│   └── POST   /characters/{id}/equip  # Equip item
│
├── /world
│   ├── GET    /region/{id}        # Get region info
│   ├── GET    /zone/{id}          # Get zone info
│   ├── GET    /zones/adjacent     # Get adjacent zones
│   └── POST   /poi/{id}/interact  # Interact with POI
│
├── /combat
│   ├── GET    /{encounterId}      # Get combat state
│   ├── POST   /{encounterId}/action   # Execute action
│   ├── POST   /{encounterId}/end-turn # End current turn
│   └── GET    /{encounterId}/actions  # Get available actions
│
├── /inventory
│   ├── GET    /                   # Get inventory
│   ├── POST   /use                # Use item
│   ├── POST   /drop               # Drop item
│   └── POST   /craft              # Craft item
│
└── /skills
    ├── GET    /{characterId}      # Get character skills
    └── POST   /{characterId}/use  # Use skill ability
```

### 8.2 WebSocket Events

For real-time updates during gameplay:

```
Client → Server:
  SUBSCRIBE_COMBAT     { encounterId }
  UNSUBSCRIBE_COMBAT   { encounterId }
  SUBSCRIBE_PARTY      { partyId }

Server → Client:
  COMBAT_STATE_CHANGED { encounterId, state }
  TURN_STARTED         { encounterId, agentId, availableAP }
  ACTION_EXECUTED      { encounterId, action, result }
  PARTY_UPDATED        { partyId, changes }
  ZONE_DISCOVERED      { zoneId, zone }
  EVENT_OCCURRED       { eventType, data }
```

### 8.3 API Response Format

```java
// Standard response wrapper
public record ApiResponse<T>(
    boolean success,
    T data,
    List<String> errors,
    Map<String, Object> metadata
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, List.of(), Map.of());
    }
    
    public static <T> ApiResponse<T> failure(List<String> errors) {
        return new ApiResponse<>(false, null, errors, Map.of());
    }
}

// Command response includes resulting events
public record CommandResponse(
    boolean success,
    List<EventSummary> events,
    List<String> errors
) {}

public record EventSummary(
    UUID eventId,
    String eventType,
    String description
) {}
```

### 8.4 DTO Examples

```java
// Party state returned to client
public record PartyView(
    UUID partyId,
    List<CharacterSummary> members,
    PositionView position,
    InventorySummary inventory,
    String formation,
    Map<UUID, Integer> factionStandings
) {}

// Combat state for UI rendering
public record CombatStateView(
    UUID encounterId,
    String status,
    int turnNumber,
    UUID currentTurn,
    List<CombatantView> combatants,
    BattlefieldView battlefield,
    List<AvailableAction> availableActions
) {}

// Available action for current combatant
public record AvailableAction(
    String actionType,
    String name,
    int apCost,
    List<UUID> validTargets,
    Map<String, Object> parameters
) {}
```

---

## 9. Frontend Architecture

### 9.1 React Application Structure

```
andara-client/
├── src/
│   ├── api/                    # API client, WebSocket manager
│   │   ├── client.ts
│   │   ├── websocket.ts
│   │   └── types.ts
│   │
│   ├── store/                  # Redux store
│   │   ├── index.ts
│   │   ├── slices/
│   │   │   ├── gameSlice.ts
│   │   │   ├── partySlice.ts
│   │   │   ├── worldSlice.ts
│   │   │   ├── combatSlice.ts
│   │   │   └── uiSlice.ts
│   │   └── middleware/
│   │       ├── apiMiddleware.ts
│   │       └── websocketMiddleware.ts
│   │
│   ├── game/                   # Game engine
│   │   ├── renderer/           # WebGL rendering
│   │   │   ├── WorldRenderer.ts
│   │   │   ├── CombatRenderer.ts
│   │   │   ├── SpriteManager.ts
│   │   │   └── Camera.ts
│   │   ├── input/              # Input handling
│   │   │   ├── InputManager.ts
│   │   │   └── KeyBindings.ts
│   │   └── systems/            # Game systems
│   │       ├── MovementSystem.ts
│   │       ├── CombatSystem.ts
│   │       └── AnimationSystem.ts
│   │
│   ├── components/             # React components
│   │   ├── game/
│   │   │   ├── GameView.tsx
│   │   │   ├── WorldView.tsx
│   │   │   └── CombatView.tsx
│   │   ├── ui/
│   │   │   ├── PartyPanel.tsx
│   │   │   ├── InventoryPanel.tsx
│   │   │   ├── CharacterSheet.tsx
│   │   │   ├── CombatHUD.tsx
│   │   │   └── DialoguePanel.tsx
│   │   └── common/
│   │       ├── Button.tsx
│   │       ├── Modal.tsx
│   │       └── Tooltip.tsx
│   │
│   ├── hooks/                  # Custom React hooks
│   │   ├── useGameLoop.ts
│   │   ├── useWebSocket.ts
│   │   └── useKeyBindings.ts
│   │
│   └── utils/                  # Utilities
│       ├── pathfinding.ts
│       ├── dice.ts
│       └── formatters.ts
│
├── public/
│   └── assets/
│       ├── sprites/
│       ├── tiles/
│       ├── ui/
│       └── audio/
│
└── package.json
```

### 9.2 Redux Store Structure

```typescript
interface RootState {
  game: {
    instanceId: string | null;
    sessionId: string | null;
    status: 'menu' | 'loading' | 'playing' | 'paused' | 'combat';
    worldTime: WorldTime;
  };
  
  party: {
    partyId: string | null;
    members: Record<string, Character>;
    position: WorldPosition;
    inventory: Inventory;
    formation: Formation;
    factionStandings: Record<string, number>;
  };
  
  world: {
    currentRegion: Region | null;
    zones: Record<string, Zone>;
    discoveredPOIs: Record<string, POI>;
    visibilityMap: Record<string, VisibilityState>;
  };
  
  combat: {
    encounterId: string | null;
    status: CombatStatus;
    turnNumber: number;
    currentTurn: string | null;
    combatants: Record<string, Combatant>;
    battlefield: Battlefield;
    availableActions: AvailableAction[];
    actionHistory: ActionResult[];
  };
  
  ui: {
    selectedCharacter: string | null;
    selectedTarget: string | null;
    openPanels: string[];
    notifications: Notification[];
    modalStack: Modal[];
  };
}
```

### 9.3 Game Loop Integration

```typescript
// useGameLoop.ts
export function useGameLoop(renderer: WorldRenderer) {
  const dispatch = useAppDispatch();
  const gameStatus = useAppSelector(state => state.game.status);
  
  useEffect(() => {
    if (gameStatus !== 'playing' && gameStatus !== 'combat') return;
    
    let frameId: number;
    let lastTime = performance.now();
    
    const loop = (currentTime: number) => {
      const deltaTime = (currentTime - lastTime) / 1000;
      lastTime = currentTime;
      
      // Update game systems
      if (gameStatus === 'playing') {
        updateExplorationSystems(deltaTime);
      }
      
      // Render
      renderer.render(deltaTime);
      
      frameId = requestAnimationFrame(loop);
    };
    
    frameId = requestAnimationFrame(loop);
    
    return () => cancelAnimationFrame(frameId);
  }, [gameStatus, renderer]);
}
```

### 9.4 WebGL Renderer Architecture

```typescript
// WorldRenderer.ts
export class WorldRenderer {
  private app: PIXI.Application;
  private camera: Camera;
  private layers: {
    terrain: PIXI.Container;
    objects: PIXI.Container;
    entities: PIXI.Container;
    effects: PIXI.Container;
    ui: PIXI.Container;
  };
  private spriteManager: SpriteManager;
  
  constructor(canvas: HTMLCanvasElement) {
    this.app = new PIXI.Application({
      view: canvas,
      width: canvas.clientWidth,
      height: canvas.clientHeight,
      backgroundColor: 0x1a1a2e,
    });
    
    this.camera = new Camera();
    this.spriteManager = new SpriteManager();
    this.initializeLayers();
  }
  
  render(deltaTime: number): void {
    this.camera.update(deltaTime);
    this.updateVisibleTiles();
    this.updateEntities(deltaTime);
    this.app.render();
  }
  
  private updateVisibleTiles(): void {
    const visibleBounds = this.camera.getVisibleBounds();
    // Only render tiles within camera bounds
    // Use sprite batching for performance
  }
}
```

---

## 10. Combat System Architecture

### 10.1 Combat State Machine

```
┌─────────────┐
│   IDLE      │
└──────┬──────┘
       │ StartCombat
       ▼
┌─────────────┐
│ INITIALIZING│──────────────────────────────────┐
└──────┬──────┘                                  │
       │ InitComplete                            │
       ▼                                         │
┌─────────────┐                                  │
│ TURN_START  │◄─────────────────────┐           │
└──────┬──────┘                      │           │
       │ ProcessTurn                 │           │
       ▼                             │           │
┌─────────────┐                      │           │
│   ACTING    │                      │           │
└──────┬──────┘                      │           │
       │ ActionComplete              │           │
       ▼                             │           │
┌─────────────┐     MoreActions      │           │
│ ACTION_     ├──────────────────────┘           │
│ RESOLVED    │                                  │
└──────┬──────┘                                  │
       │ NoMoreActions                           │
       ▼                                         │
┌─────────────┐     MoreCombatants   ┌───────────┘
│  TURN_END   ├─────────────────────►│
└──────┬──────┘                      │
       │ CombatEnded                 │
       ▼                             │
┌─────────────┐                      │
│  RESOLVING  │                      │
└──────┬──────┘                      │
       │                             │
       ▼                             │
┌─────────────┐                      │
│ COMPLETED   │                      │
└─────────────┘                      │
```

### 10.2 Combat Domain Model

```java
@Aggregate
public class CombatEncounter {
    private EncounterId id;
    private EncounterStatus status;
    private int turnNumber;
    private AgentId currentTurn;
    private List<AgentId> initiativeOrder;
    private Map<AgentId, CombatantState> combatants;
    private Battlefield battlefield;
    private List<DomainEvent> uncommittedEvents;
    
    public List<DomainEvent> executeAction(CombatAction action, AgentId actor) {
        validateTurn(actor);
        validateAction(action);
        
        List<DomainEvent> events = new ArrayList<>();
        
        switch (action) {
            case MoveAction move -> events.addAll(executeMove(move, actor));
            case AttackAction attack -> events.addAll(executeAttack(attack, actor));
            case AbilityAction ability -> events.addAll(executeAbility(ability, actor));
            case ItemAction item -> events.addAll(executeItem(item, actor));
        }
        
        // Check for combat end conditions
        if (shouldEndCombat()) {
            events.add(new CombatEnded(id, determineVictor(), timestamp()));
        }
        
        applyEvents(events);
        return events;
    }
    
    private List<DomainEvent> executeAttack(AttackAction attack, AgentId attacker) {
        List<DomainEvent> events = new ArrayList<>();
        
        CombatantState attackerState = combatants.get(attacker);
        CombatantState targetState = combatants.get(attack.targetId());
        
        // Calculate hit
        AttackResult result = calculateAttack(attackerState, targetState, attack);
        
        events.add(new AttackExecuted(
            id, attacker, attack.targetId(), 
            attack.weapon(), result
        ));
        
        if (result.hit()) {
            int damage = calculateDamage(attackerState, targetState, attack, result);
            events.add(new DamageDealt(id, attack.targetId(), damage, attack.damageType()));
            
            // Check for incapacitation
            if (targetState.health() - damage <= 0) {
                events.add(new CombatantIncapacitated(id, attack.targetId()));
            }
        }
        
        // Deduct AP
        events.add(new APSpent(id, attacker, attack.apCost()));
        
        return events;
    }
}
```

### 10.3 Combat Resolution Service

```java
@Service
public class CombatResolutionService {
    
    private final DiceRoller dice;
    private final SkillService skills;
    
    public AttackResult resolveAttack(
        CombatantState attacker,
        CombatantState target,
        AttackAction action
    ) {
        // Base hit chance
        int hitChance = 50;
        
        // Skill bonus
        int skillLevel = skills.getLevel(attacker.characterId(), action.weapon().skillType());
        hitChance += skillLevel / 2;
        
        // Attribute bonus
        int attrBonus = (attacker.relevantAttribute(action.weapon()) - 10) * 2;
        hitChance += attrBonus;
        
        // Target evasion
        hitChance -= target.agility() / 2;
        
        // Cover
        hitChance -= battlefield.getCoverBonus(target.position());
        
        // Range penalty
        int distance = battlefield.distance(attacker.position(), target.position());
        int optimalRange = action.weapon().optimalRange();
        if (distance > optimalRange) {
            hitChance -= (distance - optimalRange) * 5;
        }
        
        // Roll
        int roll = dice.roll(1, 100);
        boolean hit = roll <= hitChance;
        
        // Critical check
        int critChance = 5 + (attacker.perception() / 4);
        boolean critical = hit && roll <= critChance;
        
        return new AttackResult(hit, critical, roll, hitChance);
    }
    
    public int resolveDamage(
        CombatantState attacker,
        CombatantState target,
        AttackAction action,
        AttackResult result
    ) {
        Weapon weapon = action.weapon();
        
        // Base damage
        int damage = dice.roll(weapon.damageMin(), weapon.damageMax());
        
        // Attribute bonus
        damage += attacker.damageBonus(weapon);
        
        // Attack type modifier
        damage = (int)(damage * action.damageModifier());
        
        // Critical multiplier
        if (result.critical()) {
            damage = (int)(damage * 1.5);
        }
        
        // Armor reduction (crits bypass 50%)
        int armorReduction = target.armorValue();
        if (result.critical()) {
            armorReduction /= 2;
        }
        damage = Math.max(1, damage - armorReduction);
        
        // Damage type modifiers
        damage = applyDamageTypeModifiers(damage, weapon.damageType(), target);
        
        return damage;
    }
}
```

---

## 11. Persistence Architecture

### 11.1 Repository Pattern

```java
// Generic event-sourced repository
public interface EventSourcedRepository<T extends AggregateRoot, ID> {
    T load(ID id);
    void save(T aggregate, List<DomainEvent> events);
    boolean exists(ID id);
}

// Aggregate root base class
public abstract class AggregateRoot {
    protected String id;
    protected long version;
    protected List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    protected void applyEvent(DomainEvent event) {
        when(event);
        version++;
        uncommittedEvents.add(event);
    }
    
    protected abstract void when(DomainEvent event);
    
    public List<DomainEvent> getUncommittedEvents() {
        return List.copyOf(uncommittedEvents);
    }
    
    public void markCommitted() {
        uncommittedEvents.clear();
    }
}
```

### 11.2 Save/Load System

```java
@Service
public class GamePersistenceService {
    
    private final EventStore eventStore;
    private final SnapshotRepository snapshots;
    
    public SaveGameResult saveGame(UUID instanceId, String saveName) {
        // Create snapshots of all aggregates
        Instance instance = loadInstance(instanceId);
        Party party = loadParty(instance.partyId());
        
        // Get latest event ID
        UUID lastEventId = eventStore.getLatestEventId(instanceId);
        
        // Store save metadata
        SaveGame save = new SaveGame(
            UUID.randomUUID(),
            instanceId,
            saveName,
            lastEventId,
            Instant.now(),
            buildSaveMetadata(instance, party)
        );
        
        saveRepository.save(save);
        
        // Create snapshots
        snapshots.saveSnapshot(party);
        snapshots.saveSnapshot(instance);
        
        return new SaveGameResult(save.id(), saveName);
    }
    
    public void loadGame(UUID saveId) {
        SaveGame save = saveRepository.load(saveId);
        
        // Restore instance state by replaying events up to save point
        // Snapshots make this efficient
    }
}
```

### 11.3 Database Schema Overview

```sql
-- Core Tables
CREATE TABLE instances (
    instance_id     UUID PRIMARY KEY,
    owner_agent_id  UUID NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    status          VARCHAR(50) NOT NULL
);

CREATE TABLE saves (
    save_id         UUID PRIMARY KEY,
    instance_id     UUID NOT NULL REFERENCES instances,
    name            VARCHAR(255) NOT NULL,
    last_event_id   UUID NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    metadata        JSONB NOT NULL
);

-- Event Store (from section 4.4)
-- domain_events
-- aggregate_snapshots

-- Read Models (from section 6.2)
-- party_view
-- character_view
-- zone_view
-- combat_view

-- Reference Data
CREATE TABLE item_templates (
    template_id     UUID PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    category        VARCHAR(100) NOT NULL,
    base_value      INT NOT NULL,
    weight          DECIMAL NOT NULL,
    properties      JSONB NOT NULL
);

CREATE TABLE skill_definitions (
    skill_id        VARCHAR(100) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    category        VARCHAR(100) NOT NULL,
    description     TEXT,
    abilities       JSONB NOT NULL
);

CREATE TABLE recipes (
    recipe_id       UUID PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    skill_required  VARCHAR(100) NOT NULL,
    skill_level     INT NOT NULL,
    workstation     VARCHAR(100),
    inputs          JSONB NOT NULL,
    outputs         JSONB NOT NULL
);
```

---

## 12. System Manager (Background Processes)

### 12.1 System Manager Agent

The System Manager is an Agent with admin role that runs background processes.

```java
@Component
public class SystemManagerAgent implements Agent {
    
    private final AgentId id = AgentId.system();
    private final Role role = Role.ADMIN;
    private final ControlSource controlSource = ControlSource.SYSTEM;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void worldTick() {
        // Process world time advancement
        // Trigger environmental changes
        // Process NPC behaviors
        // Check for world events
    }
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void maintenanceTick() {
        // Clean up expired data
        // Update read model consistency
        // Snapshot frequently-accessed aggregates
    }
}
```

### 12.2 NPC Behavior System

```java
@Service
public class NPCBehaviorService {
    
    private final BehaviorTreeExecutor behaviorExecutor;
    private final CommandBus commandBus;
    
    public void processNPCTurn(AgentId npcId, EncounterId encounterId) {
        NPC npc = npcRepository.load(npcId);
        CombatState combat = combatRepository.load(encounterId);
        
        // Execute behavior tree
        BehaviorContext context = new BehaviorContext(npc, combat);
        CombatAction action = behaviorExecutor.decide(npc.behaviorTree(), context);
        
        // Issue command as NPC agent
        commandBus.send(new ExecuteCombatActionCommand(
            encounterId,
            action,
            npcId
        ));
    }
}
```

---

## 13. Security & Authorization

### 13.1 Agent Authorization

```java
@Component
public class AuthorizationService {
    
    public boolean canExecute(AgentId agent, Command command) {
        Agent agentEntity = agentRepository.load(agent);
        CommandType commandType = CommandType.from(command);
        
        return switch (agentEntity.role()) {
            case ADMIN -> true; // System manager can do anything
            case PLAYER -> isPlayerAllowed(agentEntity, command);
            case NPC -> isNPCAllowed(agentEntity, command);
        };
    }
    
    private boolean isPlayerAllowed(Agent agent, Command command) {
        // Players can only affect their own party/instance
        if (command instanceof PartyCommand pc) {
            return pc.partyId().equals(agent.partyId());
        }
        if (command instanceof InstanceCommand ic) {
            return ic.instanceId().equals(agent.instanceId());
        }
        return false;
    }
    
    private boolean isNPCAllowed(Agent agent, Command command) {
        // NPCs can only act during their turn in combat
        // Or perform allowed world actions
        // ...
    }
}
```

### 13.2 Command Validation

```java
@Aspect
@Component
public class CommandAuthorizationAspect {
    
    private final AuthorizationService authService;
    
    @Around("@annotation(ValidateAuthorization)")
    public Object validateCommand(ProceedingJoinPoint joinPoint) throws Throwable {
        Command command = extractCommand(joinPoint);
        AgentId agent = command.issuedBy();
        
        if (!authService.canExecute(agent, command)) {
            throw new UnauthorizedCommandException(agent, command);
        }
        
        return joinPoint.proceed();
    }
}
```

---

## 14. Testing Strategy

### 14.1 Test Pyramid

```
                    ┌─────────────┐
                    │     E2E     │  Few, slow, high confidence
                    │   Tests     │
                    └──────┬──────┘
                           │
                    ┌──────┴──────┐
                    │ Integration │  Some, medium speed
                    │   Tests     │
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              │       Unit Tests        │  Many, fast
              │   (Domain, Services)    │
              └─────────────────────────┘
```

### 14.2 Domain Testing

```java
class CombatEncounterTest {
    
    @Test
    void attack_shouldDealDamage_whenHitSucceeds() {
        // Given
        CombatEncounter encounter = CombatEncounterFixture.withTwoCombatants();
        AttackAction attack = new AttackAction(targetId, weapon, AttackType.STANDARD);
        
        // Mock dice to guarantee hit
        when(diceRoller.roll(1, 100)).thenReturn(10);
        
        // When
        List<DomainEvent> events = encounter.executeAction(attack, attackerId);
        
        // Then
        assertThat(events).hasSize(3);
        assertThat(events.get(0)).isInstanceOf(AttackExecuted.class);
        assertThat(events.get(1)).isInstanceOf(DamageDealt.class);
        assertThat(events.get(2)).isInstanceOf(APSpent.class);
    }
    
    @Test
    void executeAction_shouldFail_whenNotActorsTurn() {
        // Given
        CombatEncounter encounter = CombatEncounterFixture.withTwoCombatants();
        AgentId wrongAgent = AgentId.random();
        
        // When/Then
        assertThrows(NotYourTurnException.class, () -> 
            encounter.executeAction(anyAction, wrongAgent)
        );
    }
}
```

### 14.3 Event Sourcing Testing

```java
class EventSourcedPartyRepositoryTest {
    
    @Test
    void load_shouldReconstitute_fromEvents() {
        // Given
        PartyId partyId = PartyId.random();
        List<DomainEvent> events = List.of(
            new PartyCreated(partyId, protagonistId, instanceId),
            new CharacterRecruited(partyId, companionId),
            new PartyMoved(partyId, zoneA, zoneB)
        );
        eventStore.append(events);
        
        // When
        Party party = repository.load(partyId);
        
        // Then
        assertThat(party.members()).hasSize(2);
        assertThat(party.currentZoneId()).isEqualTo(zoneB);
    }
}
```

---

## 15. Deployment Architecture

### 15.1 Local Development (Docker Compose)

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: andara
      POSTGRES_USER: andara
      POSTGRES_PASSWORD: dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  andara-server:
    build: ./andara-server
    depends_on:
      - postgres
      - kafka
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/andara
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092

  andara-client:
    build: ./andara-client
    ports:
      - "3000:80"

volumes:
  postgres_data:
```

### 15.2 Production Architecture (Future)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              PRODUCTION                                      │
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │   CDN       │     │   Load      │     │   API       │                   │
│  │  (Static)   │     │  Balancer   │     │  Gateway    │                   │
│  └──────┬──────┘     └──────┬──────┘     └──────┬──────┘                   │
│         │                   │                   │                           │
│         │           ┌───────┴───────┐           │                           │
│         │           │               │           │                           │
│         │      ┌────┴────┐    ┌────┴────┐      │                           │
│         │      │ Server  │    │ Server  │      │                           │
│         │      │   Pod   │    │   Pod   │      │                           │
│         │      └────┬────┘    └────┬────┘      │                           │
│         │           │              │           │                           │
│         │           └──────┬───────┘           │                           │
│         │                  │                   │                           │
│  ┌──────┴──────┐    ┌──────┴──────┐    ┌──────┴──────┐                    │
│  │   Static    │    │   Kafka     │    │  Postgres   │                    │
│  │   Assets    │    │   Cluster   │    │   Cluster   │                    │
│  └─────────────┘    └─────────────┘    └─────────────┘                    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 16. Future: Sync Architecture

### 16.1 Sync Protocol (Design Preview)

```
┌─────────────────┐                           ┌─────────────────┐
│    Instance A   │                           │    Instance B   │
│                 │                           │                 │
│  Local Events   │                           │  Local Events   │
│       │         │                           │       │         │
└───────┼─────────┘                           └───────┼─────────┘
        │                                             │
        ▼                                             ▼
┌───────────────────────────────────────────────────────────────┐
│                      SYNC SERVICE                              │
│                                                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│  │   Market    │  │   World     │  │  Conflict   │           │
│  │   Sync      │  │   Events    │  │  Resolution │           │
│  └─────────────┘  └─────────────┘  └─────────────┘           │
│                                                                │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │  Global Event   │
                    │     Store       │
                    └─────────────────┘
```

### 16.2 Conflict Resolution Rules

| Conflict Type | Resolution Strategy |
|---------------|---------------------|
| Market listing (same item) | First-write-wins |
| Market transaction | Validate at execution time |
| World event timing | Merge (both happen) |
| POI state | Last-write-wins with timestamp |

---

## 17. Prototype Implementation Plan

### 17.1 Phase 1: Foundation (Weeks 1-4)

- [ ] Project setup (Spring Boot, React, Docker Compose)
- [ ] Event store implementation
- [ ] Basic aggregate framework
- [ ] REST API skeleton
- [ ] Redux store setup
- [ ] WebGL renderer foundation

### 17.2 Phase 2: Core Loop (Weeks 5-8)

- [ ] Party aggregate and commands
- [ ] World/Zone aggregates
- [ ] Movement system
- [ ] Basic combat (attack, damage, AP)
- [ ] World rendering (tiles, sprites)
- [ ] Party UI panel

### 17.3 Phase 3: Combat & Polish (Weeks 9-12)

- [ ] Full combat system (abilities, cover, status effects)
- [ ] Combat UI (action selection, targeting)
- [ ] Inventory and equipment
- [ ] Basic crafting
- [ ] Save/Load system
- [ ] Polish and bug fixes

---

## Appendix A: Technology Decisions Record

| Decision | Choice | Rationale | Alternatives Considered |
|----------|--------|-----------|------------------------|
| Backend Framework | Spring Boot | Mature, well-documented, team familiarity | Quarkus, Micronaut |
| Database | PostgreSQL | JSONB support, reliability, event store capability | MongoDB, EventStoreDB |
| Event Streaming | Kafka | Durability, scalability, ecosystem | RabbitMQ, Pulsar |
| Frontend Framework | React | Component model, ecosystem, hooks | Vue, Svelte |
| State Management | Redux Toolkit | Predictable state, devtools | Zustand, MobX |
| WebGL Library | PixiJS | 2D optimized, sprite batching | Three.js, raw WebGL |
| Build Tool | Gradle | Flexible, good IDE support | Maven |

---

## Appendix B: API Endpoint Reference

See Section 8.1 for full REST API structure.

---

## Appendix C: Event Type Catalog

See Domain Model document, Section 5 for complete event catalog.

---

*End of Document*
