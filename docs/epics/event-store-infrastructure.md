# Epic: Event Sourcing Infrastructure

**Epic Goal**: Establish the foundational event sourcing architecture that captures all state changes as immutable events, enabling event replay, debugging, and future cross-instance synchronization.

**Technical Context**: This epic implements the write-side of CQRS. All domain state changes flow through events stored in PostgreSQL and published to Kafka. Aggregates are reconstituted by replaying their event history.

---

## Story 1: Event Store Schema and Basic Persistence

**As a** system architect  
**I want** to create the event store database schema and basic persistence layer  
**So that** domain events can be durably stored in append-only fashion

### Acceptance Criteria

- [ ] `domain_events` table created with proper schema:
  - `event_id` (UUID, PRIMARY KEY)
  - `event_type` (VARCHAR(255), NOT NULL)
  - `aggregate_id` (VARCHAR(255), NOT NULL)
  - `aggregate_type` (VARCHAR(100), NOT NULL)
  - `instance_id` (UUID, NOT NULL)
  - `agent_id` (UUID, NOT NULL)
  - `sequence_number` (BIGINT, NOT NULL)
  - `timestamp` (TIMESTAMP WITH TIME ZONE, NOT NULL)
  - `payload` (JSONB, NOT NULL)
  - `metadata` (JSONB)
  - UNIQUE constraint on `(aggregate_id, aggregate_type, sequence_number)`

- [ ] Indexes created:
  - `idx_events_aggregate` on `(aggregate_id, aggregate_type)`
  - `idx_events_instance` on `instance_id`
  - `idx_events_timestamp` on `timestamp`
  - `idx_events_type` on `event_type`

- [ ] `EventStore` interface defined with methods:
  - `append(List<DomainEvent> events): void`
  - `getEvents(AggregateId id, AggregateType type): List<DomainEvent>`
  - `getEvents(AggregateId id, AggregateType type, long fromSequence): List<DomainEvent>`
  - `getLatestEventId(InstanceId instanceId): Optional<UUID>`

- [ ] `JdbcEventStore` implementation created using Spring's JdbcTemplate

- [ ] Events are serialized to/from JSONB using Jackson

- [ ] Optimistic locking prevents duplicate sequence numbers for same aggregate

- [ ] Unit tests verify:
  - Events can be appended and retrieved
  - Events are returned in sequence order
  - Concurrent appends to same aggregate are handled correctly
  - JSONB serialization round-trips correctly

### Technical Notes

```java
// Example domain event base class
public abstract class DomainEvent {
    private final UUID eventId;
    private final String eventType;
    private final Instant timestamp;
    private final UUID instanceId;
    private final UUID agentId;
    private final String aggregateId;
    private final String aggregateType;
    private final long version;
    private final Map<String, String> metadata;
    
    // Payload is in subclasses
}

// Example concrete event
@JsonTypeName("CharacterCreated")
public class CharacterCreated extends DomainEvent {
    private final CharacterId characterId;
    private final String name;
    private final Background background;
    private final Attributes attributes;
    // ... constructor, getters
}
```

**Effort**: 5 points  
**Dependencies**: None  
**Priority**: P0 (blocking)

---

## Story 2: Kafka Event Publishing

**As a** system architect  
**I want** events to be published to Kafka topics after being stored  
**So that** read models and future sync services can react to state changes

### Acceptance Criteria

- [ ] Kafka configuration established in `application.yml`:
  - Bootstrap servers configured
  - Serializers/deserializers configured for JSON
  - Producer acknowledgment set to `all`
  - Retry configuration set

- [ ] Topic naming convention implemented:
  - `andara.events.world` for World context events
  - `andara.events.party` for Party context events
  - `andara.events.encounter` for Encounter context events
  - `andara.events.agent` for Agent context events

- [ ] `EventPublisher` interface defined:
  - `publish(List<DomainEvent> events): CompletableFuture<Void>`
  - `publishAsync(List<DomainEvent> events): CompletableFuture<Void>`

- [ ] `KafkaEventPublisher` implementation created using Spring Kafka's `KafkaTemplate`

- [ ] Events are wrapped in `EventEnvelope` with consistent structure

- [ ] Publishing errors are logged with retry mechanism (3 attempts)

- [ ] Events are published to correct topic based on aggregate type

- [ ] Aggregate ID used as Kafka message key (for partition ordering)

- [ ] Integration tests verify:
  - Events published to Kafka can be consumed
  - Events land in correct topics
  - Message ordering preserved within partition (same aggregate)
  - Failed publishes are retried

### Technical Notes

```java
@Component
public class KafkaEventPublisher implements EventPublisher {
    private final KafkaTemplate<String, EventEnvelope> kafka;
    
    @Override
    public CompletableFuture<Void> publish(List<DomainEvent> events) {
        List<CompletableFuture<SendResult<String, EventEnvelope>>> futures = 
            events.stream()
                .map(event -> {
                    String topic = topicFor(event);
                    String key = event.aggregateId();
                    EventEnvelope envelope = wrap(event);
                    return kafka.send(topic, key, envelope);
                })
                .toList();
                
        return CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
    }
    
    private String topicFor(DomainEvent event) {
        return switch (event.aggregateType()) {
            case "Party", "Character", "Inventory" -> "andara.events.party";
            case "Region", "Zone", "POI" -> "andara.events.world";
            // ...
        };
    }
}
```

**Effort**: 3 points  
**Dependencies**: Story 1  
**Priority**: P0 (blocking)

---

## Story 3: Aggregate Root Framework

**As a** domain developer  
**I want** a base aggregate framework that handles event application and uncommitted events  
**So that** I can focus on domain logic rather than event sourcing mechanics

### Acceptance Criteria

- [ ] `AggregateRoot` abstract base class created with:
  - `id` field (String)
  - `version` field (long, tracks sequence number)
  - `uncommittedEvents` field (List<DomainEvent>)
  - `applyEvent(DomainEvent event)` method (applies and records)
  - `abstract when(DomainEvent event)` method (domain-specific application)
  - `getUncommittedEvents()` method (returns copy)
  - `markCommitted()` method (clears uncommitted)

- [ ] `AggregateId` value object created with:
  - UUID-based identifier
  - String representation
  - Factory methods for creation and parsing

- [ ] Example aggregate implemented (e.g., `Party`) demonstrating:
  - Event application via `when()` method
  - State changes only via `applyEvent()`
  - Command methods that produce and apply events

- [ ] `AggregateNotFoundException` exception created

- [ ] `ConcurrencyException` created for version conflicts

- [ ] Unit tests verify:
  - Events are applied in order
  - Version increments with each event
  - Uncommitted events are tracked
  - State is correctly reconstituted from events
  - Domain invariants are enforced

### Technical Notes

```java
public abstract class AggregateRoot {
    protected String id;
    protected long version;
    private List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    protected void applyEvent(DomainEvent event) {
        when(event);  // Update state
        version++;
        uncommittedEvents.add(event);
    }
    
    public void applyHistoricalEvent(DomainEvent event) {
        when(event);  // Update state only, don't track
        version = event.version();
    }
    
    protected abstract void when(DomainEvent event);
    
    public List<DomainEvent> getUncommittedEvents() {
        return List.copyOf(uncommittedEvents);
    }
    
    public void markCommitted() {
        uncommittedEvents.clear();
    }
}

// Example usage in Party aggregate
public class Party extends AggregateRoot {
    private PartyId partyId;
    private List<CharacterId> members;
    private WorldPosition position;
    
    public static Party create(PartyId id, CharacterId protagonist, InstanceId instance) {
        Party party = new Party();
        party.applyEvent(new PartyCreated(id, protagonist, instance));
        return party;
    }
    
    public void moveTo(ZoneId target, AgentId issuedBy) {
        // Validate move...
        applyEvent(new PartyMoved(partyId, position.zoneId(), target, issuedBy));
    }
    
    @Override
    protected void when(DomainEvent event) {
        switch (event) {
            case PartyCreated e -> {
                this.id = e.partyId().value();
                this.partyId = e.partyId();
                this.members = new ArrayList<>(List.of(e.protagonistId()));
            }
            case PartyMoved e -> {
                this.position = new WorldPosition(
                    position.instanceId(),
                    position.regionId(),
                    e.newZoneId(),
                    position.gridX(),
                    position.gridY()
                );
            }
            // ... other events
        }
    }
}
```

**Effort**: 5 points  
**Dependencies**: Story 1  
**Priority**: P0 (blocking)

---

## Story 4: Event-Sourced Repository Pattern

**As a** domain developer  
**I want** repositories that load aggregates from events and save new events  
**So that** I can persist and retrieve domain entities using event sourcing

### Acceptance Criteria

- [ ] `EventSourcedRepository<T, ID>` interface created with:
  - `load(ID id): T` - Load aggregate by replaying events
  - `save(T aggregate, List<DomainEvent> events): void` - Persist events
  - `exists(ID id): boolean` - Check if aggregate exists

- [ ] `AbstractEventSourcedRepository<T, ID>` base implementation created with:
  - Generic event loading from EventStore
  - Event persistence to EventStore
  - Event publishing to Kafka
  - Transaction management (events persisted and published atomically)

- [ ] Example concrete repository implemented (e.g., `EventSourcedPartyRepository`)

- [ ] Repository handles:
  - Empty event stream (aggregate doesn't exist)
  - Aggregate reconstitution by replaying all events
  - Optimistic concurrency (version checking)
  - Transactional save (events + publish)

- [ ] Integration tests verify:
  - Aggregate can be saved and loaded
  - Events are replayed in correct order
  - State is correctly reconstituted
  - Concurrent modifications are detected
  - Events are both stored and published

### Technical Notes

```java
public abstract class AbstractEventSourcedRepository<T extends AggregateRoot, ID> 
    implements EventSourcedRepository<T, ID> {
    
    protected final EventStore eventStore;
    protected final EventPublisher eventPublisher;
    
    @Override
    @Transactional
    public void save(T aggregate, List<DomainEvent> events) {
        if (events.isEmpty()) return;
        
        // Persist to event store
        eventStore.append(events);
        
        // Publish to Kafka
        eventPublisher.publish(events)
            .exceptionally(ex -> {
                log.error("Failed to publish events", ex);
                throw new EventPublishException(ex);
            });
        
        aggregate.markCommitted();
    }
    
    @Override
    public T load(ID id) {
        List<DomainEvent> events = eventStore.getEvents(
            id.value(), 
            getAggregateType()
        );
        
        if (events.isEmpty()) {
            throw new AggregateNotFoundException(id, getAggregateType());
        }
        
        T aggregate = createEmpty(id);
        for (DomainEvent event : events) {
            aggregate.applyHistoricalEvent(event);
        }
        
        return aggregate;
    }
    
    @Override
    public boolean exists(ID id) {
        return !eventStore.getEvents(id.value(), getAggregateType()).isEmpty();
    }
    
    protected abstract String getAggregateType();
    protected abstract T createEmpty(ID id);
}

@Repository
public class EventSourcedPartyRepository 
    extends AbstractEventSourcedRepository<Party, PartyId> 
    implements PartyRepository {
    
    @Override
    protected String getAggregateType() {
        return "Party";
    }
    
    @Override
    protected Party createEmpty(PartyId id) {
        return Party.empty(id);  // Factory method for empty aggregate
    }
}
```

**Effort**: 5 points  
**Dependencies**: Stories 1, 2, 3  
**Priority**: P0 (blocking)

---

## Story 5: Command Handler Framework

**As a** domain developer  
**I want** a command handler framework that orchestrates command validation, aggregate loading, and event persistence  
**So that** I have a consistent pattern for executing domain operations

### Acceptance Criteria

- [ ] `Command` marker interface created

- [ ] `CommandHandler<C extends Command>` interface created with:
  - `handle(C command): Result<List<DomainEvent>>`

- [ ] `Result<T>` type created representing success or failure:
  - `success(T value): Result<T>`
  - `failure(String error): Result<T>`
  - `isSuccess(): boolean`
  - `getValue(): T`
  - `getError(): String`

- [ ] `CommandBus` service created with:
  - `send(Command command): Result<List<DomainEvent>>`
  - Handler registration mechanism
  - Handler lookup by command type

- [ ] `@CommandHandler` annotation created for auto-registration

- [ ] Example command and handler implemented (e.g., `MovePartyCommand` and `MovePartyCommandHandler`)

- [ ] Command handlers can:
  - Load required aggregates from repositories
  - Execute domain logic
  - Save events and publish
  - Return structured results

- [ ] Authorization hooks integrated via `AuthorizationService`

- [ ] Unit tests verify:
  - Commands route to correct handlers
  - Handlers load aggregates, execute logic, save events
  - Authorization is checked
  - Results indicate success/failure correctly

### Technical Notes

```java
public interface Command {
    AgentId issuedBy();  // Who issued the command
}

public record MovePartyCommand(
    PartyId partyId,
    ZoneId targetZoneId,
    AgentId issuedBy
) implements Command {}

@Component
@CommandHandler
public class MovePartyCommandHandler implements CommandHandler<MovePartyCommand> {
    private final PartyRepository partyRepository;
    private final WorldRepository worldRepository;
    private final AuthorizationService authService;
    
    @Override
    @Transactional
    public Result<List<DomainEvent>> handle(MovePartyCommand command) {
        // Authorize
        if (!authService.canExecute(command.issuedBy(), command)) {
            return Result.failure("Not authorized");
        }
        
        // Load aggregates
        Party party = partyRepository.load(command.partyId());
        Zone currentZone = worldRepository.loadZone(party.currentZoneId());
        Zone targetZone = worldRepository.loadZone(command.targetZoneId());
        
        // Validate
        if (!currentZone.isAdjacentTo(targetZone)) {
            return Result.failure("Target zone not adjacent");
        }
        
        // Execute domain logic
        party.moveTo(command.targetZoneId(), command.issuedBy());
        
        // Get uncommitted events
        List<DomainEvent> events = party.getUncommittedEvents();
        
        // Persist and publish
        partyRepository.save(party, events);
        
        return Result.success(events);
    }
}

@Service
public class CommandBus {
    private final Map<Class<? extends Command>, CommandHandler<?>> handlers = new HashMap<>();
    
    public <C extends Command> void register(Class<C> commandType, CommandHandler<C> handler) {
        handlers.put(commandType, handler);
    }
    
    @SuppressWarnings("unchecked")
    public <C extends Command> Result<List<DomainEvent>> send(C command) {
        CommandHandler<C> handler = (CommandHandler<C>) handlers.get(command.getClass());
        if (handler == null) {
            throw new NoHandlerException(command.getClass());
        }
        return handler.handle(command);
    }
}
```

**Effort**: 5 points  
**Dependencies**: Stories 3, 4  
**Priority**: P0 (blocking)

---

## Story 6: Aggregate Snapshot Management

**As a** system operator  
**I want** aggregates with long event histories to be snapshotted  
**So that** aggregate loading performance remains acceptable

### Acceptance Criteria

- [ ] `aggregate_snapshots` table created with schema:
  - `aggregate_id` (VARCHAR(255), NOT NULL)
  - `aggregate_type` (VARCHAR(100), NOT NULL)
  - `sequence_number` (BIGINT, NOT NULL)
  - `snapshot_data` (JSONB, NOT NULL)
  - `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
  - PRIMARY KEY `(aggregate_id, aggregate_type)`

- [ ] `SnapshotRepository` interface created with:
  - `saveSnapshot(AggregateRoot aggregate): void`
  - `findLatest(AggregateId id, AggregateType type): Optional<Snapshot>`

- [ ] `Snapshot` value object created containing:
  - Aggregate ID, type
  - Sequence number (version at snapshot)
  - Serialized state data (JSONB)
  - Timestamp

- [ ] Repository implementations enhanced to:
  - Check for snapshot before replaying events
  - Load snapshot and replay only events since snapshot
  - Create snapshots periodically (configurable threshold)

- [ ] Snapshot strategy: Create snapshot every N events (configurable, default 100)

- [ ] Aggregates can be reconstituted from:
  - Full event replay (no snapshot)
  - Snapshot + recent events
  - Snapshots can be rebuilt from event history

- [ ] Integration tests verify:
  - Snapshot created after threshold events
  - Loading uses snapshot when available
  - Events since snapshot are still replayed
  - Aggregate state matches between snapshot+replay vs full replay

### Technical Notes

```java
public interface AggregateRoot {
    // ... existing methods
    
    // New methods for snapshot support
    JsonNode toSnapshot();
    void fromSnapshot(JsonNode snapshot);
}

@Repository
public class EventSourcedPartyRepository {
    private final SnapshotRepository snapshots;
    private final int snapshotThreshold = 100;
    
    @Override
    public Party load(PartyId id) {
        // Try snapshot first
        Optional<Snapshot> snapshot = snapshots.findLatest(id.value(), "Party");
        
        long fromSequence = snapshot.map(Snapshot::sequenceNumber).orElse(0L);
        
        // Load events since snapshot
        List<DomainEvent> events = eventStore.getEvents(
            id.value(), 
            "Party",
            fromSequence
        );
        
        // Reconstitute
        Party party;
        if (snapshot.isPresent()) {
            party = Party.empty(id);
            party.fromSnapshot(snapshot.get().data());
        } else {
            party = Party.empty(id);
        }
        
        // Apply remaining events
        for (DomainEvent event : events) {
            party.applyHistoricalEvent(event);
        }
        
        return party;
    }
    
    @Override
    @Transactional
    public void save(Party aggregate, List<DomainEvent> events) {
        // Standard save
        eventStore.append(events);
        eventPublisher.publish(events);
        
        // Check if snapshot needed
        if (aggregate.version() % snapshotThreshold == 0) {
            snapshots.saveSnapshot(aggregate);
        }
        
        aggregate.markCommitted();
    }
}
```

**Effort**: 5 points  
**Dependencies**: Story 4  
**Priority**: P1 (important for performance)

---

## Story 7: Event Schema Versioning

**As a** system maintainer  
**I want** event schemas to be versioned and upgradeable  
**So that** I can evolve the domain model without breaking existing event history

### Acceptance Criteria

- [ ] `EventVersion` annotation created for marking event versions:
  - `@EventVersion("1.0")`

- [ ] `EventUpcaster` interface created:
  - `canUpcast(String eventType, String version): boolean`
  - `upcast(JsonNode oldEvent, String fromVersion): JsonNode`

- [ ] `EventUpcasterChain` service created that:
  - Maintains registry of upcasters
  - Applies upcasters in version order
  - Logs upcasting operations

- [ ] Example upcaster implemented (e.g., upgrading `CharacterCreated` v1.0 to v2.0)

- [ ] Event deserialization enhanced to:
  - Check event version in stored data
  - Apply upcasters if needed
  - Deserialize to current schema

- [ ] Events include `schemaVersion` field in metadata

- [ ] Documentation created for:
  - How to create upcasters
  - Versioning conventions
  - Testing upcast logic

- [ ] Tests verify:
  - Old events can be loaded and upcasted
  - Upcasters chain correctly
  - Aggregates reconstitute correctly from mixed versions

### Technical Notes

```java
public interface EventUpcaster {
    boolean canUpcast(String eventType, String fromVersion);
    JsonNode upcast(JsonNode oldEvent, String fromVersion);
    String targetVersion();
}

@Component
public class CharacterCreatedV1ToV2Upcaster implements EventUpcaster {
    @Override
    public boolean canUpcast(String eventType, String fromVersion) {
        return "CharacterCreated".equals(eventType) && "1.0".equals(fromVersion);
    }
    
    @Override
    public JsonNode upcast(JsonNode oldEvent, String fromVersion) {
        ObjectNode upgraded = oldEvent.deepCopy();
        
        // Example: Add new field with default value
        if (!upgraded.has("origin")) {
            upgraded.put("origin", "VAULT_DWELLER");
        }
        
        // Update version
        upgraded.put("schemaVersion", "2.0");
        
        return upgraded;
    }
    
    @Override
    public String targetVersion() {
        return "2.0";
    }
}

@Service
public class EventUpcasterChain {
    private final List<EventUpcaster> upcasters;
    
    public JsonNode upcastIfNeeded(JsonNode event, String eventType) {
        String currentVersion = event.has("schemaVersion") 
            ? event.get("schemaVersion").asText() 
            : "1.0";
            
        JsonNode result = event;
        
        // Apply upcasters in chain until current version reached
        for (EventUpcaster upcaster : upcasters) {
            if (upcaster.canUpcast(eventType, currentVersion)) {
                log.info("Upcasting {} from {} to {}", 
                    eventType, currentVersion, upcaster.targetVersion());
                result = upcaster.upcast(result, currentVersion);
                currentVersion = upcaster.targetVersion();
            }
        }
        
        return result;
    }
}
```

**Effort**: 3 points  
**Dependencies**: Story 1  
**Priority**: P2 (can defer to post-MVP)

---

## Story 8: Event Store Performance Monitoring

**As a** system operator  
**I want** metrics on event store performance  
**So that** I can identify bottlenecks and optimize as the game scales

### Acceptance Criteria

- [ ] Micrometer metrics integration added for:
  - Event append duration (timer)
  - Event load duration (timer)
  - Events loaded per aggregate (gauge)
  - Snapshot creation duration (timer)
  - Kafka publish duration (timer)
  - Event store table size (gauge)

- [ ] Metrics exposed via Spring Boot Actuator endpoint (`/actuator/metrics`)

- [ ] Slow query logging enabled for event store operations (threshold: 500ms)

- [ ] Health check endpoint includes:
  - Event store connectivity
  - Kafka connectivity
  - Recent error rate

- [ ] Dashboard queries documented for common metrics:
  - Average aggregate event count
  - 95th percentile load time
  - Event throughput (events/second)

- [ ] Alerts configured for:
  - Event store operation > 1 second
  - Kafka publish failure rate > 1%
  - Aggregate event count > 1000 (snapshot candidate)

### Technical Notes

```java
@Component
public class InstrumentedEventStore implements EventStore {
    private final EventStore delegate;
    private final MeterRegistry metrics;
    
    @Override
    public void append(List<DomainEvent> events) {
        Timer.Sample sample = Timer.start(metrics);
        try {
            delegate.append(events);
        } finally {
            sample.stop(metrics.timer("eventstore.append.duration"));
            metrics.counter("eventstore.events.appended").increment(events.size());
        }
    }
    
    @Override
    public List<DomainEvent> getEvents(String aggregateId, String aggregateType) {
        return Timer.sample(metrics).record(() -> {
            List<DomainEvent> events = delegate.getEvents(aggregateId, aggregateType);
            metrics.gauge("eventstore.aggregate.event_count", events.size());
            return events;
        }, Timer.builder("eventstore.load.duration")
            .tag("aggregate_type", aggregateType)
            .register(metrics));
    }
}
```

**Effort**: 3 points  
**Dependencies**: Stories 1-6  
**Priority**: P2 (helpful for optimization, not blocking)

---

## Epic Summary

### Total Effort: 34 points

### Critical Path (P0 - MVP Blocking):
1. Event Store Schema (5 pts)
2. Kafka Publishing (3 pts)
3. Aggregate Framework (5 pts)
4. Repository Pattern (5 pts)
5. Command Handlers (5 pts)

**Subtotal P0: 23 points** ← Must complete for MVP

### Important (P1 - Performance):
6. Snapshot Management (5 pts) ← Implement after P0, before heavy testing

### Nice to Have (P2 - Post-MVP):
7. Event Versioning (3 pts)
8. Performance Monitoring (3 pts)

---

## Implementation Order Recommendation

**Sprint 1** (Foundation):
- Story 1: Event Store Schema
- Story 2: Kafka Publishing

**Sprint 2** (Domain Framework):
- Story 3: Aggregate Framework
- Story 4: Repository Pattern

**Sprint 3** (Command Layer):
- Story 5: Command Handlers
- Begin using this infrastructure in actual domain logic (Party creation, etc.)

**Sprint 4** (Optimization - after some aggregates built):
- Story 6: Snapshot Management
- Test with realistic aggregate sizes

**Post-MVP**:
- Story 7: Event Versioning (when you need to evolve schemas)
- Story 8: Performance Monitoring (when you need production insights)

---

## Testing Strategy

Each story should include:

1. **Unit Tests**: Domain logic, event application, command handling
2. **Integration Tests**: Database operations, Kafka publishing, end-to-end event flow
3. **Performance Tests**: For Stories 6 & 8, measure impact of snapshots
4. **Contract Tests**: Event schema contracts (helpful for Story 7)

## Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Event store performance degrades with large aggregates | Medium | High | Story 6 (snapshots), monitor event counts |
| Kafka publish failures lose events | Low | Critical | Transactional outbox pattern (future enhancement) |
| Event schema evolution breaks replay | Medium | High | Story 7 (versioning), comprehensive tests |
| JSONB serialization issues | Low | Medium | Strict event schema validation, tests |
