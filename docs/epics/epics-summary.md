Looking at your architecture and GDD documents, here are the additional epics needed to reach MVP, organized by the core gameplay loop and technical foundations:

## Core Gameplay Epics

### 3. **World Exploration Epic**
- As a player, I can see my party's current location on the world map
- As a player, I can move my party between adjacent zones
- As a player, I can see fog of war revealing explored areas
- As a player, I can discover Points of Interest (POIs) in zones
- As a player, I can interact with discovered POIs
- As a player, I can view environmental hazards and their effects
- As a player, I can see zone details (terrain, danger level, discovered content)

### 4. **Combat Encounter Epic**
- As a player, I can initiate combat encounters with hostile NPCs
- As a player, I can see the tactical combat grid with my party and enemies
- As a player, I can view initiative order and whose turn it is
- As a player, I can spend Action Points to move characters during combat
- As a player, I can execute basic attacks against enemies
- As a player, I can see damage calculations and health changes
- As a player, I can use abilities during combat
- As a player, I can end my turn when out of actions
- As a player, I can see combat resolution and loot rewards
- As a player, I can retreat from combat if needed

### 5. **Party Management Epic**
- As a player, I can view my party roster and member details
- As a player, I can access individual character sheets
- As a player, I can view character attributes, skills, and status
- As a player, I can see character health, stamina, and injuries
- As a player, I can manage character equipment slots
- As a player, I can view party formation and positioning
- As a player, I can rest to recover stamina and health

### 6. **Inventory & Equipment Epic**
- As a player, I can view party shared inventory
- As a player, I can view individual character inventories
- As a player, I can equip items to character slots
- As a player, I can unequip items from characters
- As a player, I can use consumable items
- As a player, I can see item details and properties
- As a player, I can see inventory weight/capacity limits
- As a player, I can drop unwanted items

### 7. **Skills & Progression Epic**
- As a player, I can see my characters' current skill proficiencies
- As a player, I can see skill experience gain from use
- As a player, I can see skill level-up notifications
- As a player, I can see unlocked abilities from skill progression
- As a player, I can view ability details and requirements
- As a player, I can use active abilities in appropriate contexts

### 8. **Crafting System Epic**
- As a player, I can access crafting stations (workbench, forge, etc.)
- As a player, I can view available recipes based on my skills
- As a player, I can see recipe requirements (materials, skill level, time)
- As a player, I can craft items from recipes
- As a player, I can see crafting success and item quality
- As a player, I can repair damaged equipment
- As a player, I can gather raw materials from exploration

## Technical Foundation Epics

### 9. **Game Session Management Epic**
- As a player, I can start a new game
- As a player, I can save my current game state
- As a player, I can load a previously saved game
- As a player, I can see save game metadata (timestamp, location, party)
- As a player, I can have multiple save slots
- As a system, I can auto-save at critical moments
- As a player, I can continue from my most recent save

### 10. **Event Sourcing Infrastructure Epic**
- As a system, I can capture all state changes as domain events
- As a system, I can persist events to the event store
- As a system, I can publish events to Kafka topics
- As a system, I can replay events to reconstitute aggregates
- As a system, I can create and restore from snapshots
- As a system, I can handle event versioning and schema evolution

### 11. **Read Model Projection Epic**
- As a system, I can consume events to build read models
- As a system, I can maintain party view projections
- As a system, I can maintain world/zone view projections
- As a system, I can maintain combat state view projections
- As a system, I can query read models efficiently for UI display
- As a system, I can rebuild projections from event history

### 12. **Rendering & UI Foundation Epic**
- As a player, I can see the game world rendered in WebGL
- As a player, I can see character sprites and animations
- As a player, I can interact with UI panels and components
- As a player, I can see responsive UI updates during gameplay
- As a system, I can efficiently batch sprite rendering
- As a system, I can manage camera positioning and viewport
- As a player, I can see smooth transitions between game states

## Quality & Polish Epic (Can be last)

### 13. **Tutorial & Onboarding Epic**
- As a new player, I can access gameplay tutorials
- As a player, I can see contextual hints during first encounters
- As a player, I can see tooltips explaining UI elements
- As a player, I can access help documentation in-game

---

## Suggested MVP Priority Order:

1. **Player Initiation** (✓ in progress)
2. **Content Authoring** (✓ defined)
3. **Event Sourcing Infrastructure** (foundation for everything)
4. **Rendering & UI Foundation** (need to see the game)
5. **Game Session Management** (start/save/load)
6. **World Exploration** (core loop begins)
7. **Party Management** (need to understand your party)
8. **Inventory & Equipment** (gear matters)
9. **Combat Encounter** (first major challenge)
10. **Skills & Progression** (reward for playing)
11. **Read Model Projection** (performance optimization)
12. **Crafting System** (depth and economy)
13. **Tutorial & Onboarding** (polish)

The first 5 are your critical path - without them, you can't play the game at all. Epics 6-10 form your core gameplay loop. Epics 11-13 add depth and polish.

Would you like me to elaborate on any of these epics with more detailed user stories, or help you prioritize/sequence them differently based on your team's capacity?