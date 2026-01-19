# MVP Implementation Plan

**Source References**: [STORY_TRACKER.md](../../STORY_TRACKER.md), [PROJECT_STATUS.md](../../PROJECT_STATUS.md), [Epic Summary](../epics/epics-summary.md)

## Epic Readiness Overview

| Epic | Remaining stories | Blockers | Dependencies | References |
| --- | --- | --- | --- | --- |
| Event Store Infrastructure | None (complete) | None | None | [Epic doc](../epics/event-store-infrastructure.md) · [Story tracker](../../STORY_TRACKER.md#epic-1-event-store-infrastructure-2323-pts--100) |
| Rendering & UI Foundation | None (complete) | None | Event Store Infrastructure (done) | [Epic doc](../epics/rendering-ui-foundation.md) · [Story tracker](../../STORY_TRACKER.md#epic-2-rendering--ui-foundation-5252-pts--100) |
| Player Initiation | PI-11 Polish: “Begin Adventure” transition | Blocked by World Exploration destination | Rendering & UI Foundation (done) | [Epic doc](../epics/player-initiation.md) · [Story tracker](../../STORY_TRACKER.md#epic-3-player-initiation-2022-pts--92) |
| Content Authoring System | CMS-7 Diff/Merge tool, CMS-8 Admin Panel CRUD + audit log, CMS-9 Seed content files, CMS-10 Doc generator | None (current critical path) | Event Store Infrastructure (done) | [Epic doc](../epics/content-authoring.md) · [Story tracker](../../STORY_TRACKER.md#epic-4-content-authoring-system-3550-pts--70) · [Project status](../../PROJECT_STATUS.md#epic-4-content-authoring-system-) |
| Game Session Management | GSM-1 through GSM-7 (all stories) | None | Event Store Infrastructure (done), Player Initiation (done), Content Authoring (for metadata + content-driven saves) | [Epic doc](../epics/game-session-management.md) · [Story tracker](../../STORY_TRACKER.md#epic-5-game-session-management-039-pts--0) · [Epic summary](../epics/epics-summary.md#9-game-session-management-epic) |
| World Exploration | All stories in epic summary (zone navigation, POIs, fog of war, travel) | Blocked by Content Authoring seed data | Content Authoring, Rendering & UI Foundation | [Epic summary](../epics/epics-summary.md#3-world-exploration-epic) · [Story tracker](../../STORY_TRACKER.md#unstarted-epics-from-epics-summarymd) |
| Combat Encounter | All stories in epic summary (grid, initiative, actions, loot) | Blocked by Content Authoring seed data | Content Authoring, Rendering & UI Foundation, World Exploration | [Epic summary](../epics/epics-summary.md#4-combat-encounter-epic) · [Story tracker](../../STORY_TRACKER.md#unstarted-epics-from-epics-summarymd) |
| Party Management | All stories in epic summary (roster, sheets, formation) | Blocked by Content Authoring seed data | Content Authoring, Rendering & UI Foundation | [Epic summary](../epics/epics-summary.md#5-party-management-epic) · [Story tracker](../../STORY_TRACKER.md#unstarted-epics-from-epics-summarymd) |
| Inventory & Equipment | All stories in epic summary (inventory UI, equip/unequip, tooltips) | Blocked by Content Authoring seed data | Content Authoring, Rendering & UI Foundation | [Epic summary](../epics/epics-summary.md#6-inventory--equipment-epic) · [Story tracker](../../STORY_TRACKER.md#unstarted-epics-from-epics-summarymd) |
| Skills & Progression | All stories in epic summary (skill XP, unlocks, checks) | Blocked by Content Authoring seed data | Content Authoring, Combat Encounter, World Exploration | [Epic summary](../epics/epics-summary.md#7-skills--progression-epic) · [Story tracker](../../STORY_TRACKER.md#unstarted-epics-from-epics-summarymd) |
| Crafting System | All stories in epic summary (recipes, stations, materials) | Blocked by Content Authoring seed data | Content Authoring, Inventory & Equipment | [Epic summary](../epics/epics-summary.md#8-crafting-system-epic) · [Story tracker](../../STORY_TRACKER.md#unstarted-epics-from-epics-summarymd) |
| Read Model Projection | All stories in epic summary (projections + rebuild) | None | Event Store Infrastructure (done) | [Epic summary](../epics/epics-summary.md#11-read-model-projection-epic) |
| Tutorial & Onboarding | All stories in epic summary (tutorials, hints, tooltips) | None | Core gameplay loop (World Exploration + Combat + Inventory) | [Epic summary](../epics/epics-summary.md#13-tutorial--onboarding-epic) |

## Next up for MVP

1. **Content Authoring System completion**
   - CMS-8 Admin Panel: schema-driven CRUD, delete/rename, audit log
   - CMS-9 Seed content files (items, skills, recipes, regions)
   - CMS-7 Diff/Merge tool (if needed for collaboration) and CMS-10 Doc generator (if time allows)

2. **Game Session Management**
   - GSM-1 New Game Initialization
   - GSM-2 Manual Save Game
   - GSM-3 Load Game from Save Slot
   - GSM-4 Save Slot Management UI
   - GSM-5 Auto-Save System
   - GSM-6 Continue Game Quick Start
   - GSM-7 Save Metadata Display

3. **Unblock Player Initiation polish**
   - PI-11 “Begin Adventure” transition once World Exploration landing view exists

4. **World Exploration (start after content seeding)**
   - Zone navigation, POI discovery, fog-of-war rendering

## Traceability Links

- **Status**: [Story tracker](../../STORY_TRACKER.md) · [Project status report](../../PROJECT_STATUS.md)
- **Epic docs**: [Event Store](../epics/event-store-infrastructure.md), [Rendering & UI](../epics/rendering-ui-foundation.md), [Player Initiation](../epics/player-initiation.md), [Content Authoring](../epics/content-authoring.md), [Game Session Management](../epics/game-session-management.md), [Epic summary](../epics/epics-summary.md)
