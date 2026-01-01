# Andara's World — Project Inception Document

**Version**: 0.1  
**Date**: January 2026  
**Status**: Draft  

---

## 1. Vision Statement

**Andara's World** is a single-player, party-based tactical RPG set in a post-apocalyptic Earth where fantasy, science fiction, and survival collide. Players lead a small team through a chaotic, emergent world—exploring wastelands, engaging in tactical combat, crafting from scavenged resources, and writing their own story through action rather than scripted narrative.

The world persists. Player choices ripple outward through a shared multiverse of parallel instances, connected by dimensional markets and synchronized world events. Every playthrough exists in the same cosmology—isolated yet connected.

**Core Fantasy**: *Carve meaning from chaos. Your party's story emerges from the ruins.*

---

## 2. Influences & Aesthetic Anchors

| Influence | What We Take |
|-----------|--------------|
| **Baldur's Gate** | Party dynamics, tactical pausable combat, character depth |
| **Wasteland 2/3** | Post-apocalyptic tone, skill-based progression, morally gray choices |
| **EverQuest (classic)** | Meaningful death, player-driven economy, world persistence, earned progression |
| **Dark Souls** | Asynchronous multiplayer, environmental storytelling, indirect player interaction |
| **Caves of Qud / Dwarf Fortress** | Emergent narrative, procedural depth, world-as-simulation |

**Setting Tone**: Gritty survival punctuated by wonder. Technology and magic coexist as misunderstood remnants. Factions war over resources and ideology. Hope is scarce but present.

---

## 3. Core Gameplay Loop

```
EXPLORE → ENCOUNTER → RESOLVE → ACQUIRE → CRAFT/TRADE → PROGRESS → EXPLORE
    ↑                                                              |
    └──────────────────────────────────────────────────────────────┘
```

### 3.1 Exploration
- Orthographic view of interconnected regions
- Fog of war / discovery mechanics
- Environmental hazards, hidden caches, faction territories
- Time passes; world state evolves

### 3.2 Combat
- Tactical, turn-based or hybrid (TBD in prototype)
- Party-based with individual skill loadouts
- Positioning, cover, environmental interaction
- Soft death: incapacitation, injury system, retreat mechanics—not permadeath but *consequential* death

### 3.3 Crafting & Economy
- Resource scavenging from exploration
- Crafting tied to skills (no class restrictions)
- Player-driven economy via **Dimensional Markets**—asynchronous trade across parallel world instances
- Supply/demand emerges from aggregate player behavior

### 3.4 Narrative
- **Emergent only**—no scripted main quest
- Story arises from faction dynamics, world events, party composition, player choices
- Environmental storytelling: ruins tell history
- **LLM Integration** (exploratory): Dynamic NPC dialogue, reactive world narration, procedural lore generation

---

## 4. World Model

### 4.1 The Cataclysm (Setting Origin)
Earth suffered a convergence event—dimensional rifts tore through reality, collapsing technological civilization while bleeding through creatures, energies, and physical laws from other planes. Centuries later, the survivors exist among the ruins: mutants, baseline humans, dimensional refugees, and stranger things.

Magic and technology are both present, both poorly understood, both dangerous.

### 4.2 Parallel Instances & Synchronization
Each player's world is a **parallel instance**—a distinct timeline branching from common origins. Instances are:
- **Isolated in real-time**: No direct player-to-player interaction during play
- **Connected asynchronously**: Shared markets, synchronized macro-events, discoverable player artifacts
- **Regional**: World divided into regions; instance state is per-region

**Dimensional Markets**: Trade hubs exist outside normal space. Players list goods; prices emerge from cross-instance supply/demand. Narratively framed as markets that "exist in the spaces between worlds."

### 4.3 Persistence Model
| Element | Persistence Scope |
|---------|-------------------|
| Party state | Per-instance (local) |
| Region state | Per-instance, sync snapshots to global |
| NPC faction standing | Per-instance with global influence |
| Market prices/inventory | Global (eventually consistent) |
| World events | Global triggers, local manifestation |
| Player-created content | Publishable to global, discoverable by others |

---

## 5. Character & Progression System

### 5.1 Party Composition
- Player controls a **small party** (target: 4-6 members)
- Members are **recruited** from the world (not created at start beyond protagonist)
- Each party member has individual skills, personality, and potential story arcs
- Permadeath: **No** — but injuries, trauma, and relationship damage have lasting effects

### 5.2 Skill-Based Progression
- No classes; characters develop skills through use and training
- Skill categories (preliminary):
  - **Combat**: Melee, ranged, energy weapons, unarmed, tactics
  - **Survival**: Scavenging, tracking, medicine, navigation
  - **Technical**: Mechanics, electronics, chemistry, engineering
  - **Arcane**: Rift manipulation, binding, wards, perception
  - **Social**: Barter, intimidation, leadership, deception
- Skills unlock abilities/perks at thresholds
- Soft caps encourage specialization but permit generalization

### 5.3 Death & Consequence
- **Incapacitation** in combat, not instant death
- Downed characters can be stabilized, extracted, or left behind (with consequences)
- **Injuries** persist: broken limbs, trauma, lasting debuffs requiring time/resources to heal
- **Total party kill**: Narrative continuation via rescue, capture, or new party inheriting the world state

---

## 6. Technical Architecture (Overview)

> *Full architecture definition document to follow.*

### 6.1 Stack
| Layer | Technology |
|-------|------------|
| **Frontend** | React SPA, WebGL canvas, orthographic renderer |
| **Backend** | Java Spring Boot |
| **Persistence** | PostgreSQL |
| **Event Streaming** | Apache Kafka |

### 6.2 Architectural Drivers
- **Event Sourcing**: World state changes as event streams; enables replay, sync, debugging
- **CQRS**: Separate read/write models for complex world state queries
- **Eventual Consistency**: Cross-instance sync tolerates latency; markets don't need real-time
- **Offline-First Consideration**: Single-player core must function without connectivity; sync when available

### 6.3 Key Technical Challenges
| Challenge | Mitigation Strategy |
|-----------|---------------------|
| World state synchronization across instances | Event-based sync with conflict resolution rules; regional partitioning |
| LLM integration latency | Async generation, caching, graceful degradation |
| Complex game state in browser | State management architecture (Redux or similar); WebGL optimization |
| Tactical combat performance | Efficient pathfinding, spatial indexing, WebGL batching |

---

## 7. Prototype Scope (3 Months)

### 7.1 Goals
Demonstrate the **core loop** in a constrained environment:
- Single player, single machine
- One region (small, handcrafted)
- Party of 3-4 characters
- Low NPC count (< 20 autonomous agents)
- No cross-instance sync (deferred)
- No LLM integration (deferred)

### 7.2 Prototype Deliverables
| Deliverable | Description |
|-------------|-------------|
| **Playable region** | Explorable area with fog of war, points of interest, hazards |
| **Party management** | Control party, manage inventory, view skills |
| **Tactical combat** | Engage hostiles, use skills, resolve encounters |
| **Basic crafting** | Gather resources, craft simple items |
| **NPC interaction** | Static dialogue trees (LLM deferred) |
| **Persistence** | Save/load world and party state |
| **Event architecture** | Kafka pipeline operational (local), events flowing |

### 7.3 Explicitly Deferred
- Dimensional markets / cross-instance sync
- LLM-driven narrative
- Multiple regions
- Faction systems (beyond basic hostility)
- Character recruitment (start with fixed party)
- Advanced crafting trees

---

## 8. Domain Model (Preliminary)

> *Full domain model and ubiquitous language document to follow.*

### 8.1 Core Entities
| Entity | Description |
|--------|-------------|
| **World Instance** | A player's unique parallel world; contains regions |
| **Region** | Geographic area with its own state, NPCs, resources |
| **Party** | Player-controlled group of Characters |
| **Character** | Individual with skills, inventory, status, relationships |
| **Agent** | Autonomous NPC or creature with behavior model |
| **Encounter** | Combat or interaction event |
| **Item** | Physical object—equipment, consumable, resource, artifact |
| **Skill** | Learnable capability with proficiency level |
| **Faction** | Group with shared goals, territory, relationships |
| **Market** | Cross-instance trading venue |
| **Event** | Immutable record of state change |

### 8.2 Ubiquitous Language (Initial Glossary)
| Term | Definition |
|------|------------|
| **Instance** | A single player's world timeline |
| **Sync** | Process of propagating state between instances and global |
| **Dimensional Market** | Async cross-instance trading mechanism |
| **Incapacitation** | Combat state where character is down but not dead |
| **Injury** | Persistent negative status requiring healing |
| **Rift** | Dimensional anomaly; source of magic and danger |
| **Convergence** | The cataclysm that created the current world |
| **Agent** | Any autonomous entity (NPC, creature) with AI behavior |
| **Encounter** | Discrete interaction event (combat, dialogue, discovery) |

---

## 9. Risks & Open Questions

### 9.1 Technical Risks
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| WebGL performance on complex scenes | Medium | High | Early profiling; LOD; culling; sprite batching |
| Event sourcing complexity | Medium | Medium | Start simple; add projections incrementally |
| LLM reliability/latency | High | Medium | Defer to post-prototype; design for graceful fallback |
| State synchronization conflicts | Medium | High | Strong conflict resolution rules; last-write-wins where safe |

### 9.2 Design Risks
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Emergent narrative feels empty | Medium | High | Strong environmental storytelling; faction dynamics; meaningful world events |
| Economy manipulation via markets | Medium | Medium | Rate limiting; anomaly detection; narrative framing of "market instability" |
| Combat pacing (turn-based can drag) | Medium | Medium | Prototype multiple approaches; gather playtest feedback |

### 9.3 Open Questions
1. **Combat model**: Pure turn-based, real-time-with-pause, or phase-based hybrid?
2. **Party size**: 4 or 6? Affects UI, balance, tactical depth.
3. **Injury system granularity**: Abstract (light/medium/severe) or specific (broken arm, lung damage)?
4. **LLM integration scope**: Dialogue only? World narration? Procedural quests?
5. **Player-created content**: What can players leave behind for others to find?

---

## 10. Success Criteria

### 10.1 Prototype Success
- [ ] Player can explore region, discovering points of interest
- [ ] Tactical combat resolves correctly with injury system
- [ ] Resources gathered and basic items crafted
- [ ] Party and world state persist across sessions
- [ ] Event stream captures all state changes (Kafka operational)
- [ ] Subjective: *It feels like the beginning of something worth playing*

### 10.2 Project Success (Long-term)
- Players create stories they want to share
- Economy emerges organically from player behavior
- World feels alive despite async isolation
- Technical architecture supports future scale

---

## 11. Next Steps

1. **Review & refine this document** with stakeholders
2. **Expand into Game Design Document** (mechanics detail, content design)
3. **Expand into Architecture Definition** (technical deep-dive, component design)
4. **Expand into Domain Model** (full entity relationships, ubiquitous language)
5. **Establish development environment** (repo, CI/CD, local Kafka/Postgres)
6. **Begin prototype sprint planning**

---

## Appendix A: Reference Architecture Sketch

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT (Browser)                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │
│  │ React SPA   │  │ Game State  │  │ WebGL       │  │ Local      │ │
│  │ UI Layer    │  │ Manager     │  │ Renderer    │  │ Storage    │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼ REST / WebSocket
┌─────────────────────────────────────────────────────────────────────┐
│                        BACKEND (Spring Boot)                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │
│  │ API Gateway │  │ Game        │  │ Sync        │  │ Market     │ │
│  │             │  │ Services    │  │ Service     │  │ Service    │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
          │                                │                │
          ▼                                ▼                ▼
┌─────────────────┐              ┌─────────────────┐  ┌───────────┐
│   PostgreSQL    │              │     Kafka       │  │ LLM API   │
│  (Event Store   │              │ (Event Stream)  │  │ (Future)  │
│   + Read Models)│              │                 │  │           │
└─────────────────┘              └─────────────────┘  └───────────┘
```

---

## Appendix B: Mood & Tone References

**Visual**: Muted palette with pops of rift-energy color. Ruined architecture. Nature reclaiming. Weathered technology. Strange creatures.

**Audio** (future): Ambient dread, moments of beauty, percussive combat.

**Writing**: Terse. Observational. Let the world speak through objects and environments. When NPCs speak, they have agendas.

---

*End of Document*
