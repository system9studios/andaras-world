# Andara's World — Domain Model & Ubiquitous Language

**Version**: 0.1  
**Date**: January 2026  
**Status**: Draft  
**Parent Document**: Project Inception Document v0.1

---

## 1. Purpose

This document defines the **domain model** for Andara's World and establishes the **ubiquitous language** used across all project artifacts—code, documentation, and team communication. Consistent terminology reduces cognitive load and prevents translation errors between design and implementation.

The domain model is organized using Domain-Driven Design (DDD) principles:
- **Bounded Contexts** define autonomous subsystems with clear boundaries
- **Aggregates** define transactional consistency boundaries
- **Entities** have identity and lifecycle
- **Value Objects** are immutable and identity-less
- **Domain Events** capture state changes as immutable facts

---

## 2. Ubiquitous Language — Core Glossary

### 2.1 World & Cosmology

| Term | Definition |
|------|------------|
| **Convergence** | The cataclysmic event in which multiple dimensions collided with Earth, merging realities and collapsing civilization. Source of rifts, magic, and dimensional instability. |
| **Dimension** | A parallel plane of existence. Pre-Convergence, dimensions were separate. Post-Convergence, boundaries are permeable. |
| **Rift** | A tear in dimensional fabric. Rifts leak energy, creatures, and physical laws from other dimensions. Source of both power and danger. |
| **Instance** | A single player's world timeline—one parallel dimension among many. Each instance diverges from common origins but exists in the same cosmology. |
| **Region** | A discrete geographic area within an instance. Regions have independent state, boundaries, and characteristics. The unit of world partitioning. |
| **Zone** | A subdivision within a region. Used for finer-grained state management (e.g., a ruined city block, a forest clearing). |
| **Point of Interest (POI)** | A discoverable location within a zone: a ruin, cache, hazard, settlement, or anomaly. |

### 2.2 Agents & Control

| Term | Definition |
|------|------------|
| **Agent** | Any autonomous entity that interfaces with the game system. The base abstraction for all actors. Agents have identity, state, and can issue commands. |
| **Player** | An agent controlled by a human, interfacing through the game client with the `player` role. A player controls a Party. |
| **NPC (Non-Player Character)** | An agent controlled by AI/scripted behavior, interfacing with the game system with the `npc` role. NPCs inhabit the world and interact with players. |
| **System Manager** | An agent controlled by the system itself, interfacing with admin privileges. Responsible for background processes: world events, market reconciliation, decay, spawning. |
| **Role** | The authorization level of an agent: `player`, `npc`, or `admin`. Determines permitted commands. |
| **Control Source** | What drives an agent's decisions: `human`, `ai`, or `system`. |

### 2.3 Characters & Party

| Term | Definition |
|------|------------|
| **Party** | A group of Characters controlled collectively by a Player. The unit of player agency in the world. |
| **Character** | An individual member of a Party. Has skills, inventory, status, and relationships. Not synonymous with Agent—a Character is the in-world representation; the Player is the agent controlling the Party. |
| **Protagonist** | The initial Character created with a new game. The narrative anchor of the Party. |
| **Companion** | A Character recruited to the Party during play. Has their own personality, history, and potential story arcs. |
| **Skill** | A learnable capability with a proficiency level. Skills improve through use and training. |
| **Proficiency** | Numeric measure of skill mastery. Affects success probability and unlocks abilities. |
| **Ability** | A specific action unlocked by skill proficiency. Active abilities are used deliberately; passive abilities apply automatically. |
| **Status** | Current condition of a Character: health, injuries, buffs, debuffs, morale. |
| **Injury** | A persistent negative status resulting from damage. Requires time, resources, or treatment to heal. Injuries have severity and affected body systems. |
| **Incapacitation** | Combat state where a Character is down but not dead. Can be stabilized, will die without intervention. |
| **Death** | Permanent removal of a Character from the Party. Occurs from unresolved incapacitation or narrative events. Soft death—consequential but not save-ending. |

### 2.4 Encounters & Combat

| Term | Definition |
|------|------------|
| **Encounter** | A discrete interaction event. Encompasses combat, dialogue, discovery, environmental challenge. An Encounter has a type, participants, and resolution. |
| **Combat Encounter** | An Encounter resolved through tactical combat. Involves hostile agents, turn structure, and violence. |
| **Turn** | A discrete unit of combat time in which an agent takes actions. |
| **Action** | Something an agent does during their turn: attack, move, use ability, use item, interact. |
| **Action Points (AP)** | Resource spent to take actions during a turn. Regenerates each turn. |
| **Initiative** | Determines turn order. Derived from character attributes and situational modifiers. |
| **Cover** | Positional protection reducing incoming damage or hit probability. |
| **Engagement** | State of being in active melee range with a hostile. Restricts movement. |

### 2.5 Items & Crafting

| Term | Definition |
|------|------------|
| **Item** | Any physical object in the game world. Has type, properties, condition, and location. |
| **Equipment** | Items that can be worn or wielded: weapons, armor, accessories. |
| **Consumable** | Items destroyed on use: food, medicine, ammunition. |
| **Resource** | Raw materials used in crafting: scrap metal, chemicals, rift crystals. |
| **Artifact** | Rare items of power, often of Convergence origin. May have unique properties or lore. |
| **Inventory** | Collection of items carried by a Character or stored in a container. Has capacity limits. |
| **Container** | A world object or item that holds other items: chest, backpack, vehicle cargo. |
| **Condition** | Measure of an item's integrity. Degrades with use; can be repaired. |
| **Recipe** | Formula for crafting an item from resources and components. Requires skill proficiency. |
| **Workstation** | A location or object enabling specific crafting types: forge, lab, electronics bench. |

### 2.6 Factions & Relationships

| Term | Definition |
|------|------------|
| **Faction** | An organized group with shared goals, territory, and identity. Factions have relationships with other factions and with the player's Party. |
| **Standing** | Numeric measure of relationship between Party and a Faction. Affects NPC behavior, access, and opportunities. |
| **Reputation** | Qualitative summary of standing: hostile, unfriendly, neutral, friendly, allied. |
| **Territory** | Regions or zones claimed by a Faction. Affects spawns, patrols, and encounters. |

### 2.7 Economy & Markets

| Term | Definition |
|------|------------|
| **Dimensional Market** | A trading venue that exists outside normal space, connecting parallel instances. Players list items; prices emerge from cross-instance supply and demand. |
| **Listing** | An offer to sell an item at a price in a Dimensional Market. |
| **Bid** | An offer to buy an item at a price. |
| **Transaction** | A completed exchange of items for currency. |
| **Currency** | Medium of exchange. May vary by region or faction (barter, scrip, energy cells, etc.). |
| **Price Index** | Aggregate price data for an item type across the market. Reflects supply/demand. |

### 2.8 Persistence & Synchronization

| Term | Definition |
|------|------------|
| **Event** | An immutable record of something that happened. Events are facts, not commands. Events are the source of truth for world state. |
| **Command** | A request to change state, issued by an agent. Commands are validated and may produce events. |
| **Event Stream** | Ordered sequence of events. The append-only log of state changes. |
| **Projection** | A read model derived from events. Optimized for queries. Can be rebuilt from events. |
| **Snapshot** | A point-in-time capture of aggregate state. Optimization to avoid replaying full event history. |
| **Sync** | Process of propagating events between an instance and global state. |
| **Sync Point** | A marker in the event stream indicating successful synchronization. |
| **Conflict** | Divergent state between instances that must be resolved during sync. |
| **Resolution** | Strategy for handling conflicts: last-write-wins, merge, manual, or narrative. |

### 2.9 System & Technical

| Term | Definition |
|------|------------|
| **Session** | A continuous period of play. Begins at load, ends at save or quit. |
| **Save** | Persisted snapshot of instance state. |
| **World Tick** | A unit of simulated world time. Used for background processes, decay, NPC behavior. |
| **Spawn** | Creation of an agent or item in the world, typically by a System Manager. |
| **Despawn** | Removal of an agent or item from active simulation. May be temporary (unloaded zone) or permanent (death, consumption). |

---

## 3. Bounded Contexts

The system is divided into bounded contexts—autonomous subsystems with clear responsibilities and interfaces. Each context owns its domain model and data.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              ANDARA'S WORLD                                 │
│                                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │                 │  │                 │  │                 │             │
│  │   WORLD        │  │   PARTY         │  │   ENCOUNTER     │             │
│  │   CONTEXT      │  │   CONTEXT       │  │   CONTEXT       │             │
│  │                 │  │                 │  │                 │             │
│  │  Regions       │  │  Characters     │  │  Combat         │             │
│  │  Zones         │  │  Skills         │  │  Resolution     │             │
│  │  POIs          │  │  Inventory      │  │  Dialogue       │             │
│  │  Environment   │  │  Status         │  │  Discovery      │             │
│  │                 │  │                 │  │                 │             │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘             │
│           │                    │                    │                       │
│           ▼                    ▼                    ▼                       │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        EVENT BUS (Kafka)                            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│           ▲                    ▲                    ▲                       │
│           │                    │                    │                       │
│  ┌────────┴────────┐  ┌────────┴────────┐  ┌────────┴────────┐             │
│  │                 │  │                 │  │                 │             │
│  │   AGENT        │  │   ECONOMY       │  │   SYNC          │             │
│  │   CONTEXT      │  │   CONTEXT       │  │   CONTEXT       │             │
│  │                 │  │                 │  │                 │             │
│  │  Players       │  │  Markets        │  │  Instance Mgmt  │             │
│  │  NPCs          │  │  Transactions   │  │  Conflict Res   │             │
│  │  System Mgrs   │  │  Pricing        │  │  Event Replay   │             │
│  │  Authorization │  │  Listings       │  │  Snapshots      │             │
│  │                 │  │                 │  │                 │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.1 World Context

**Responsibility**: Manages the physical world—regions, zones, points of interest, environmental state, hazards, and world time.

**Key Aggregates**:
- `Region` — root aggregate for geographic area
- `Zone` — child of Region
- `PointOfInterest` — discoverable location

**Key Events**:
- `RegionDiscovered`
- `ZoneEntered`
- `POIDiscovered`
- `EnvironmentChanged`
- `WorldTickProcessed`

### 3.2 Party Context

**Responsibility**: Manages the player's party—characters, skills, inventory, status, relationships between party members.

**Key Aggregates**:
- `Party` — root aggregate for player's team
- `Character` — member of party
- `Inventory` — items held by character or party

**Key Events**:
- `CharacterRecruited`
- `CharacterDied`
- `SkillImproved`
- `ItemAcquired`
- `ItemConsumed`
- `InjurySustained`
- `InjuryHealed`

### 3.3 Encounter Context

**Responsibility**: Manages all encounters—combat resolution, dialogue, discovery events. Orchestrates turn-based combat.

**Key Aggregates**:
- `Encounter` — root aggregate for interaction event
- `CombatState` — tactical state during combat

**Key Events**:
- `EncounterStarted`
- `EncounterResolved`
- `TurnStarted`
- `ActionTaken`
- `DamageDealt`
- `CharacterIncapacitated`
- `CombatEnded`

### 3.4 Agent Context

**Responsibility**: Manages all agents—identity, authentication, authorization, control source. Unified interface for players, NPCs, and system managers.

**Key Aggregates**:
- `Agent` — root aggregate for any actor
- `AgentSession` — active session for an agent

**Key Events**:
- `AgentRegistered`
- `SessionStarted`
- `SessionEnded`
- `CommandIssued`
- `CommandAuthorized`
- `CommandRejected`

### 3.5 Economy Context

**Responsibility**: Manages dimensional markets, transactions, pricing, listings. Handles cross-instance economic activity.

**Key Aggregates**:
- `Market` — root aggregate for trading venue
- `Listing` — offer to sell
- `Transaction` — completed trade

**Key Events**:
- `ListingCreated`
- `ListingCancelled`
- `TransactionCompleted`
- `PriceIndexUpdated`

### 3.6 Sync Context

**Responsibility**: Manages instance lifecycle, event synchronization, conflict resolution, snapshot management.

**Key Aggregates**:
- `Instance` — root aggregate for player's world
- `SyncState` — current sync position and status

**Key Events**:
- `InstanceCreated`
- `SyncStarted`
- `SyncCompleted`
- `ConflictDetected`
- `ConflictResolved`
- `SnapshotCreated`

---

## 4. Core Entity Definitions

### 4.1 Agent Hierarchy

```
┌─────────────────────────────────────────────────────────────────┐
│                          «abstract»                             │
│                            Agent                                │
├─────────────────────────────────────────────────────────────────┤
│  id: AgentId                                                    │
│  role: Role [player | npc | admin]                              │
│  controlSource: ControlSource [human | ai | system]             │
│  status: AgentStatus                                            │
├─────────────────────────────────────────────────────────────────┤
│  issueCommand(command: Command): Result<Event[]>                │
│  canExecute(commandType: CommandType): boolean                  │
└─────────────────────────────────────────────────────────────────┘
                              △
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
┌─────────┴─────────┐ ┌───────┴───────┐ ┌─────────┴─────────┐
│      Player       │ │      NPC      │ │   SystemManager   │
├───────────────────┤ ├───────────────┤ ├───────────────────┤
│ partyId: PartyId  │ │ behaviorTree  │ │ managedProcess    │
│ sessionId         │ │ factionId     │ │ schedule          │
│ preferences       │ │ homeRegion    │ │ permissions       │
└───────────────────┘ └───────────────┘ └───────────────────┘
```

### 4.2 World Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                          Instance                               │
├─────────────────────────────────────────────────────────────────┤
│  id: InstanceId                                                 │
│  ownerId: AgentId (Player)                                      │
│  createdAt: Timestamp                                           │
│  lastSyncPoint: EventId                                         │
│  regions: Map<RegionId, Region>                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ contains
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                           Region                                │
├─────────────────────────────────────────────────────────────────┤
│  id: RegionId                                                   │
│  name: String                                                   │
│  biome: Biome                                                   │
│  dangerLevel: Int                                               │
│  discoveredAt: Timestamp?                                       │
│  zones: Map<ZoneId, Zone>                                       │
│  factionControl: Map<FactionId, ControlLevel>                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ contains
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            Zone                                 │
├─────────────────────────────────────────────────────────────────┤
│  id: ZoneId                                                     │
│  regionId: RegionId                                             │
│  position: GridPosition                                         │
│  terrain: TerrainType                                           │
│  visibility: VisibilityState                                    │
│  pointsOfInterest: List<POI>                                    │
│  agents: List<AgentId>                                          │
│  items: List<ItemId>                                            │
│  hazards: List<Hazard>                                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ contains
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      PointOfInterest                            │
├─────────────────────────────────────────────────────────────────┤
│  id: POIId                                                      │
│  type: POIType [ruin | cache | settlement | rift | landmark]    │
│  position: GridPosition                                         │
│  discoveredAt: Timestamp?                                       │
│  interactionState: InteractionState                             │
│  loot: List<ItemId>                                             │
│  encounters: List<EncounterId>                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.3 Party & Character

```
┌─────────────────────────────────────────────────────────────────┐
│                            Party                                │
├─────────────────────────────────────────────────────────────────┤
│  id: PartyId                                                    │
│  instanceId: InstanceId                                         │
│  members: List<Character> (max 6)                               │
│  sharedInventory: Inventory                                     │
│  position: WorldPosition                                        │
│  formation: Formation                                           │
│  factionStandings: Map<FactionId, Standing>                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ contains
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                          Character                              │
├─────────────────────────────────────────────────────────────────┤
│  id: CharacterId                                                │
│  name: String                                                   │
│  isProtagonist: Boolean                                         │
│  background: Background                                         │
│  attributes: Attributes                                         │
│  skills: Map<SkillId, Proficiency>                              │
│  abilities: List<Ability>                                       │
│  equipment: EquipmentSlots                                      │
│  inventory: Inventory                                           │
│  status: CharacterStatus                                        │
│  injuries: List<Injury>                                         │
│  relationships: Map<CharacterId, Relationship>                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ has
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       CharacterStatus                           │
├─────────────────────────────────────────────────────────────────┤
│  health: Int                                                    │
│  maxHealth: Int                                                 │
│  stamina: Int                                                   │
│  morale: Int                                                    │
│  conditions: List<Condition>                                    │
│  incapacitated: Boolean                                         │
│  dead: Boolean                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.4 Skills & Abilities

```
┌─────────────────────────────────────────────────────────────────┐
│                            Skill                                │
├─────────────────────────────────────────────────────────────────┤
│  id: SkillId                                                    │
│  name: String                                                   │
│  category: SkillCategory                                        │
│  description: String                                            │
│  abilityUnlocks: Map<Proficiency, AbilityId>                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      SkillCategory                              │
├─────────────────────────────────────────────────────────────────┤
│  COMBAT: [melee, ranged, energy_weapons, unarmed, tactics]      │
│  SURVIVAL: [scavenging, tracking, medicine, navigation]        │
│  TECHNICAL: [mechanics, electronics, chemistry, engineering]    │
│  ARCANE: [rift_manipulation, binding, wards, perception]        │
│  SOCIAL: [barter, intimidation, leadership, deception]          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          Ability                                │
├─────────────────────────────────────────────────────────────────┤
│  id: AbilityId                                                  │
│  name: String                                                   │
│  type: AbilityType [active | passive | reaction]                │
│  sourceSkill: SkillId                                           │
│  requiredProficiency: Int                                       │
│  apCost: Int (for active)                                       │
│  cooldown: Int (turns, if any)                                  │
│  effects: List<Effect>                                          │
└─────────────────────────────────────────────────────────────────┘
```

### 4.5 Items

```
┌─────────────────────────────────────────────────────────────────┐
│                         «abstract»                              │
│                            Item                                 │
├─────────────────────────────────────────────────────────────────┤
│  id: ItemId                                                     │
│  templateId: ItemTemplateId                                     │
│  name: String                                                   │
│  description: String                                            │
│  weight: Decimal                                                │
│  value: Int                                                     │
│  condition: Int (0-100)                                         │
│  stackable: Boolean                                             │
│  quantity: Int                                                  │
└─────────────────────────────────────────────────────────────────┘
                              △
                              │
     ┌────────────────────────┼────────────────────────┐
     │                        │                        │
┌────┴─────┐           ┌──────┴──────┐          ┌──────┴──────┐
│ Equipment│           │ Consumable  │          │  Resource   │
├──────────┤           ├─────────────┤          ├─────────────┤
│ slot     │           │ uses        │          │ resourceType│
│ stats    │           │ effects     │          │ purity      │
│ mods     │           │             │          │             │
└──────────┘           └─────────────┘          └─────────────┘
```

### 4.6 Encounters

```
┌─────────────────────────────────────────────────────────────────┐
│                         Encounter                               │
├─────────────────────────────────────────────────────────────────┤
│  id: EncounterId                                                │
│  type: EncounterType [combat | dialogue | discovery | hazard]   │
│  status: EncounterStatus [pending | active | resolved]          │
│  participants: List<AgentId>                                    │
│  location: WorldPosition                                        │
│  startedAt: Timestamp                                           │
│  resolvedAt: Timestamp?                                         │
│  outcome: EncounterOutcome?                                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ for combat type
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        CombatState                              │
├─────────────────────────────────────────────────────────────────┤
│  encounterId: EncounterId                                       │
│  turnNumber: Int                                                │
│  currentTurn: AgentId                                           │
│  initiativeOrder: List<AgentId>                                 │
│  combatants: Map<AgentId, CombatantState>                       │
│  battlefield: BattlefieldState                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      CombatantState                             │
├─────────────────────────────────────────────────────────────────┤
│  agentId: AgentId                                               │
│  characterId: CharacterId?                                      │
│  position: BattlefieldPosition                                  │
│  actionPoints: Int                                              │
│  movementRemaining: Int                                         │
│  engagedWith: List<AgentId>                                     │
│  activeCooldowns: Map<AbilityId, Int>                           │
└─────────────────────────────────────────────────────────────────┘
```

### 4.7 Market & Economy

```
┌─────────────────────────────────────────────────────────────────┐
│                      DimensionalMarket                          │
├─────────────────────────────────────────────────────────────────┤
│  id: MarketId                                                   │
│  name: String                                                   │
│  currencyTypes: List<CurrencyType>                              │
│  listings: List<Listing>                                        │
│  priceIndices: Map<ItemTemplateId, PriceIndex>                  │
│  lastUpdated: Timestamp                                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          Listing                                │
├─────────────────────────────────────────────────────────────────┤
│  id: ListingId                                                  │
│  marketId: MarketId                                             │
│  sellerId: AgentId                                              │
│  sellerInstanceId: InstanceId                                   │
│  item: ItemSnapshot                                             │
│  askingPrice: Money                                             │
│  listedAt: Timestamp                                            │
│  expiresAt: Timestamp                                           │
│  status: ListingStatus [active | sold | expired | cancelled]    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        Transaction                              │
├─────────────────────────────────────────────────────────────────┤
│  id: TransactionId                                              │
│  listingId: ListingId                                           │
│  buyerId: AgentId                                               │
│  buyerInstanceId: InstanceId                                    │
│  finalPrice: Money                                              │
│  completedAt: Timestamp                                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. Domain Events Catalog

Events are the source of truth. Each event is immutable, timestamped, and contains all data needed to understand what happened.

### 5.1 Event Envelope

```
┌─────────────────────────────────────────────────────────────────┐
│                        DomainEvent                              │
├─────────────────────────────────────────────────────────────────┤
│  eventId: EventId (UUID)                                        │
│  eventType: String                                              │
│  timestamp: Timestamp                                           │
│  instanceId: InstanceId                                         │
│  agentId: AgentId (who caused it)                               │
│  aggregateId: String                                            │
│  aggregateType: String                                          │
│  version: Int                                                   │
│  payload: EventPayload                                          │
│  metadata: Map<String, String>                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 Key Events by Context

#### World Context
| Event | Payload |
|-------|---------|
| `RegionDiscovered` | regionId, discoveredBy, timestamp |
| `ZoneEntered` | zoneId, partyId, previousZoneId |
| `ZoneExited` | zoneId, partyId, nextZoneId |
| `POIDiscovered` | poiId, zoneId, discoveredBy |
| `POIInteracted` | poiId, interactionType, outcome |
| `HazardTriggered` | hazardId, zoneId, affectedAgents |
| `EnvironmentChanged` | zoneId, changeType, newState |
| `WorldTickProcessed` | tickNumber, changesApplied |

#### Party Context
| Event | Payload |
|-------|---------|
| `PartyCreated` | partyId, protagonistId, instanceId |
| `CharacterCreated` | characterId, name, background, attributes |
| `CharacterRecruited` | characterId, partyId, recruitedAt |
| `CharacterDismissed` | characterId, partyId, reason |
| `CharacterDied` | characterId, causeOfDeath, location |
| `SkillUsed` | characterId, skillId, context, outcome |
| `SkillImproved` | characterId, skillId, oldProficiency, newProficiency |
| `AbilityUnlocked` | characterId, abilityId, fromSkill |
| `ItemAcquired` | characterId, itemId, source |
| `ItemDropped` | characterId, itemId, location |
| `ItemEquipped` | characterId, itemId, slot |
| `ItemUnequipped` | characterId, itemId, slot |
| `ItemConsumed` | characterId, itemId, effects |
| `ItemCrafted` | characterId, recipeId, resultItemId, resources |
| `InjurySustained` | characterId, injuryType, severity, source |
| `InjuryHealed` | characterId, injuryId, method |
| `StatusEffectApplied` | characterId, effectType, duration, source |
| `StatusEffectRemoved` | characterId, effectType, reason |

#### Encounter Context
| Event | Payload |
|-------|---------|
| `EncounterStarted` | encounterId, type, participants, location |
| `EncounterResolved` | encounterId, outcome, rewards, casualties |
| `CombatInitiated` | encounterId, initiativeOrder, battlefield |
| `TurnStarted` | encounterId, agentId, turnNumber, availableAP |
| `TurnEnded` | encounterId, agentId, actionsToken |
| `MovementExecuted` | encounterId, agentId, fromPos, toPos, apSpent |
| `AttackExecuted` | encounterId, attackerId, targetId, weapon, roll, outcome |
| `DamageDealt` | encounterId, targetId, damageAmount, damageType, source |
| `AbilityUsed` | encounterId, agentId, abilityId, targets, effects |
| `CharacterIncapacitated` | encounterId, characterId, incapacitatingBlow |
| `CharacterStabilized` | encounterId, characterId, stabilizedBy |
| `CombatEnded` | encounterId, victor, survivors, loot |

#### Agent Context
| Event | Payload |
|-------|---------|
| `AgentRegistered` | agentId, role, controlSource |
| `SessionStarted` | sessionId, agentId, startedAt |
| `SessionEnded` | sessionId, agentId, endedAt, reason |
| `CommandReceived` | commandId, agentId, commandType, payload |
| `CommandAuthorized` | commandId, agentId |
| `CommandRejected` | commandId, agentId, reason |
| `CommandExecuted` | commandId, resultingEvents |

#### Economy Context
| Event | Payload |
|-------|---------|
| `ListingCreated` | listingId, marketId, sellerId, item, price |
| `ListingCancelled` | listingId, reason |
| `ListingExpired` | listingId |
| `TransactionCompleted` | transactionId, listingId, buyerId, finalPrice |
| `PriceIndexUpdated` | marketId, itemTemplateId, newIndex, sampleSize |
| `MarketSynced` | marketId, syncTimestamp, listingsAdded, transactionsProcessed |

#### Sync Context
| Event | Payload |
|-------|---------|
| `InstanceCreated` | instanceId, ownerId, createdAt |
| `InstanceLoaded` | instanceId, loadedAt, eventCount |
| `InstanceSaved` | instanceId, savedAt, snapshotId |
| `SyncInitiated` | instanceId, fromEventId, toGlobal |
| `SyncCompleted` | instanceId, eventsPublished, eventsReceived |
| `ConflictDetected` | instanceId, conflictType, localEvent, globalEvent |
| `ConflictResolved` | instanceId, resolutionStrategy, winningEvent |
| `SnapshotCreated` | instanceId, snapshotId, atEventId |

---

## 6. Aggregate Boundaries

Aggregates define transactional consistency boundaries. Operations within an aggregate are atomic; operations across aggregates are eventually consistent.

| Aggregate Root | Contains | Consistency Boundary |
|---------------|----------|---------------------|
| `Instance` | Regions, world metadata | World structure changes are atomic |
| `Region` | Zones, POIs, regional state | Zone state changes within region are atomic |
| `Party` | Characters, shared inventory, standings | Party-level operations are atomic |
| `Character` | Skills, equipment, personal inventory, status | Character state changes are atomic |
| `Encounter` | Combat state, participants, resolution | Combat turns are atomic |
| `Market` | Listings, price indices | Market operations are atomic (global) |
| `Agent` | Session, preferences, authorization | Agent operations are atomic |

### 6.1 Cross-Aggregate Interactions

These interactions require eventual consistency via events:

| Interaction | Source Aggregate | Target Aggregate | Via Event |
|-------------|-----------------|------------------|-----------|
| Party enters zone | Party | Zone | `ZoneEntered` |
| Character loots POI | Character | POI | `ItemAcquired`, `POIInteracted` |
| Combat damages character | Encounter | Character | `DamageDealt` |
| Market transaction | Market | Character (buyer/seller) | `TransactionCompleted` |
| System spawns NPC | SystemManager | Zone | `AgentSpawned` |

---

## 7. Value Objects

Value objects are immutable, compared by value, and have no identity.

| Value Object | Properties |
|--------------|------------|
| `WorldPosition` | instanceId, regionId, zoneId, gridX, gridY |
| `GridPosition` | x: Int, y: Int |
| `Money` | amount: Decimal, currency: CurrencyType |
| `Proficiency` | level: Int, experience: Int, milestone: Int |
| `Attributes` | strength, agility, endurance, intellect, perception, charisma |
| `DamageRoll` | min: Int, max: Int, type: DamageType |
| `TimeSpan` | ticks: Int |
| `Timestamp` | instant: Instant |
| `ItemSnapshot` | templateId, name, condition, quantity (for market listings) |

---

## 8. Invariants & Business Rules

### 8.1 Party Invariants
- Party size: 1 ≤ members ≤ 6
- Party must always have at least one living member (TPK triggers special handling)
- Protagonist cannot be dismissed (only death removes them)
- Dead characters remain in party roster (for narrative/memorial) but cannot act

### 8.2 Character Invariants
- Health: 0 ≤ health ≤ maxHealth
- Proficiency: 0 ≤ proficiency ≤ 100 (soft cap at 80, hard cap at 100)
- Equipment slots are exclusive (one item per slot)
- Incapacitated characters cannot take actions
- Dead characters cannot be healed (death is final)

### 8.3 Combat Invariants
- Turn order is deterministic given initiative values
- Action points cannot go negative
- Incapacitation occurs at health ≤ 0
- Death occurs after N turns incapacitated without stabilization (N = 3?)
- Combat ends when one side is eliminated or flees

### 8.4 Market Invariants
- Listings expire after N time units (configurable)
- Seller must possess item at time of transaction
- Buyer must possess sufficient currency at time of transaction
- Price is fixed at listing time (no bidding in v1)
- Cross-instance transactions are atomic from market's perspective

### 8.5 Sync Invariants
- Events are append-only
- Event order within instance is total
- Global event order is partial (causal consistency)
- Snapshots reference a specific event ID
- Conflict resolution must be deterministic

---

## 9. Context Mapping

How bounded contexts communicate:

```
┌──────────────┐         ┌──────────────┐
│    Party     │◄───────►│    World     │
│   Context    │ Partner │   Context    │
└──────────────┘         └──────────────┘
       │                        │
       │ Customer               │ Customer
       ▼                        ▼
┌──────────────┐         ┌──────────────┐
│  Encounter   │         │    Agent     │
│   Context    │         │   Context    │
└──────────────┘         └──────────────┘
       │                        │
       │ Conformist             │ Supplier
       ▼                        ▼
┌──────────────┐         ┌──────────────┐
│   Economy    │◄────────│    Sync      │
│   Context    │ Partner │   Context    │
└──────────────┘         └──────────────┘
```

| Relationship | Type | Description |
|--------------|------|-------------|
| Party ↔ World | Partnership | Co-evolve; party navigates world, world affects party |
| Party → Encounter | Customer-Supplier | Party initiates encounters; encounter consumes party state |
| Encounter → Economy | Conformist | Encounter loot follows economy item definitions |
| Agent ↔ All | Supplier | Agent context provides identity/auth to all other contexts |
| Sync ↔ Economy | Partnership | Sync propagates market data; market generates sync events |

---

## 10. Open Questions for Architecture Phase

1. **Event schema versioning**: How do we handle event schema evolution over time?
2. **Snapshot frequency**: How often to snapshot aggregates vs. replay from events?
3. **Conflict resolution specifics**: What's the resolution strategy for each conflict type?
4. **Offline duration limits**: How long can an instance be offline before sync becomes problematic?
5. **Agent behavior system**: Behavior trees, GOAP, or utility AI for NPCs?
6. **LLM integration points**: Which events trigger LLM calls? How are responses cached?

---

## Appendix A: Skill Tree (Preliminary)

```
COMBAT
├── Melee
│   ├── Blades
│   ├── Blunt
│   └── Polearms
├── Ranged
│   ├── Handguns
│   ├── Rifles
│   └── Bows
├── Energy Weapons
├── Unarmed
└── Tactics
    ├── Leadership
    └── Positioning

SURVIVAL
├── Scavenging
├── Tracking
├── Medicine
│   ├── First Aid
│   └── Surgery
└── Navigation

TECHNICAL
├── Mechanics
├── Electronics
├── Chemistry
└── Engineering

ARCANE
├── Rift Manipulation
├── Binding
├── Wards
└── Perception

SOCIAL
├── Barter
├── Intimidation
├── Leadership
└── Deception
```

---

## Appendix B: Injury System (Preliminary)

| Severity | Heal Time | Penalty | Example |
|----------|-----------|---------|---------|
| Minor | Hours | -5% to related skill | Cuts, bruises, sprain |
| Moderate | Days | -15% to related skill, -1 AP | Fracture, concussion, deep wound |
| Severe | Weeks | -30% to related skill, -2 AP, condition | Broken limb, internal bleeding, trauma |
| Critical | Permanent | Lasting debuff, potential death | Lost limb, organ damage, coma |

Body systems affected: Head, Torso, Left Arm, Right Arm, Left Leg, Right Leg

---

*End of Document*
