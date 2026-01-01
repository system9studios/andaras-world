# Andara's World — Game Design Document

**Version**: 0.1  
**Date**: January 2026  
**Status**: Draft  
**Parent Document**: Project Inception Document v0.1

---

## 1. Overview

This document defines the core game systems for Andara's World, with emphasis on **resource economies**—what players accumulate, spend, and transform. Each system is designed to create meaningful choices through resource tension and interconnection.

### 1.1 Design Pillars

| Pillar | Meaning |
|--------|---------|
| **Scarcity Creates Story** | Resources are limited; how you spend them defines your playthrough |
| **Specialization Over Optimization** | No perfect builds; trade-offs everywhere |
| **Emergence Over Script** | Systems interact to produce narrative, not authored plot |
| **Consequence Without Punishment** | Failure changes the game, doesn't end it (except protagonist death) |
| **Crafted Identity** | Players leave marks on the world through unique creations |

### 1.2 Master Resource Map

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           RESOURCE ECONOMY                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  CURRENCIES              CONSUMABLES            PROGRESSION                 │
│  ├── Credits             ├── Ammunition         ├── Skill Experience       │
│  ├── Faction Standing    ├── Medical Supplies   ├── Ability Unlocks        │
│  └── Reputation          ├── Food/Water         └── Attribute Growth       │
│                          ├── Fuel                                          │
│                          └── Rift Charges                                  │
│                                                                             │
│  CHARACTER POOLS         MATERIALS              TIME                        │
│  ├── Health              ├── Scrap              ├── World Ticks            │
│  ├── Stamina             ├── Components         ├── Travel Duration        │
│  ├── Morale              ├── Chemicals          ├── Crafting Duration      │
│  └── Action Points       ├── Rift Crystals      └── Healing Duration       │
│                          ├── Tech Salvage                                  │
│                          └── Exotic Matter                                 │
│                                                                             │
│  CAPACITY                UNIQUE                                             │
│  ├── Inventory Weight    ├── Crafted Artifacts                             │
│  ├── Party Size          ├── Discovered Recipes                            │
│  └── Equipment Slots     └── Named Items                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Character Creation

### 2.1 Protagonist Creation

The player creates a single **Protagonist** at game start. This character is the narrative anchor—their death ends the run (reload required).

#### 2.1.1 Creation Steps

```
1. ORIGIN        → Background that affects starting skills, gear, and faction standing
2. ATTRIBUTES    → Distribute points across six core attributes
3. SKILLS        → Select starting skill focuses (2-3 skills at elevated proficiency)
4. APPEARANCE    → Visual customization
5. NAME          → Character identity
```

#### 2.1.2 Origins (Backgrounds)

Origins provide narrative context and mechanical starting conditions.

| Origin | Starting Bonus | Starting Penalty | Faction Effect |
|--------|---------------|------------------|----------------|
| **Vault Dweller** | +Tech skills, good starting gear | Low survival skills | Neutral to all |
| **Wastelander** | +Survival skills, +Scavenging | Limited tech knowledge | +Nomad factions |
| **Rift-Touched** | +Arcane skills, rift perception | Health penalty, social distrust | -Settlement factions |
| **Caravan Guard** | +Combat skills, +Barter | Narrow skill base | +Merchant factions |
| **Settlement Militia** | +Ranged, +Tactics | Low mobility skills | +Home settlement |
| **Outcast** | +Stealth, +Deception | Faction penalties | -Most factions |

#### 2.1.3 Attributes

Six core attributes, point-buy system (27 points, range 6-16 per attribute, base 8).

| Attribute | Affects |
|-----------|---------|
| **Strength (STR)** | Melee damage, carry capacity, physical checks |
| **Agility (AGI)** | Initiative, ranged accuracy, evasion, movement |
| **Endurance (END)** | Health pool, stamina, resistance to injury/toxins |
| **Intellect (INT)** | Tech skills, crafting quality, ability cooldowns |
| **Perception (PER)** | Detection, ranged accuracy, critical chance, arcane sensitivity |
| **Charisma (CHA)** | Barter prices, companion morale, faction standing gains |

#### 2.1.4 Starting Skills

Player selects **two skill focuses** at character creation:
- Focus skills start at Proficiency 20 (vs. default 0)
- One additional skill from Origin starts at Proficiency 15

### 2.2 Companion Recruitment

Companions are recruited during play, not created. They come with:
- Pre-defined personality and background
- Established skill profile (can be developed further)
- Personal quest hooks (optional engagement)
- Relationship dynamics with other companions

**Companion Death**: Unlike the protagonist, companions have the full injury/incapacitation system. They can die permanently, but the game continues.

---

## 3. Survival System

### 3.1 Survival Resources

Survival creates the baseline tension that drives exploration and scavenging.

| Resource | Depletion Rate | Effect When Depleted |
|----------|---------------|----------------------|
| **Food** | 1 unit/day per character | Stamina doesn't regenerate; eventual health loss |
| **Water** | 1 unit/day per character | Faster stamina drain; perception penalties |
| **Medical Supplies** | Per injury treatment | Cannot treat injuries; must rest longer |
| **Fuel** | Per vehicle travel | Vehicles immobilized; must walk |

### 3.2 Environmental Hazards

The post-Convergence world is hostile. Hazards consume resources or inflict status effects.

| Hazard Type | Effect | Mitigation |
|-------------|--------|------------|
| **Radiation Zones** | Accumulating rad damage | Rad-Away consumables, hazmat gear |
| **Rift Storms** | Random dimensional effects | Shelter, rift wards |
| **Toxic Air** | Stamina drain, poison | Respirators, antidotes |
| **Extreme Temperature** | Stamina drain, exposure injuries | Appropriate gear, shelter |
| **Hostile Flora/Fauna** | Ambush encounters | Perception checks, repellents |

### 3.3 Rest & Recovery

Rest consumes time and potentially resources, but restores character pools.

| Rest Type | Duration | Requires | Restores |
|-----------|----------|----------|----------|
| **Short Rest** | 1 hour | Safe location | Stamina, some AP abilities |
| **Long Rest** | 8 hours | Shelter, food/water | Full stamina, health (slow), ability cooldowns |
| **Extended Rest** | 24+ hours | Settlement or camp | Injury healing, morale recovery |

---

## 4. Travel System

### 4.1 Movement Scales

The game operates at two movement scales:

| Scale | Unit | Speed Factor | Encounters |
|-------|------|--------------|------------|
| **Region (Overworld)** | Zone to zone | Hours/days | Random encounters, discoveries |
| **Tactical (Local)** | Grid squares | Action points | Combat, detailed exploration |

### 4.2 Overworld Travel

Travel between zones consumes time and potentially fuel.

```
TRAVEL COST = Base Time × Terrain Modifier × Party Condition × Weather

Base Time: Determined by zone distance
Terrain Modifier: Road (0.5x) → Clear (1x) → Rough (1.5x) → Hazardous (2x)
Party Condition: Injuries, encumbrance, vehicle status
Weather: Clear (1x) → Poor (1.5x) → Severe (2x or impassable)
```

#### 4.2.1 Travel Modes

| Mode | Speed | Capacity | Fuel Cost | Risk |
|------|-------|----------|-----------|------|
| **On Foot** | Slow | Limited by carry weight | None | High encounter rate |
| **Pack Animal** | Medium | +100 weight capacity | Food/water | Medium encounter rate |
| **Vehicle (Ground)** | Fast | High capacity | Fuel | Lower encounter rate, breakdown risk |
| **Vehicle (Rift-Powered)** | Very Fast | Medium capacity | Rift charges | Rift anomaly risk |

#### 4.2.2 Travel Events

Random events occur during travel based on region danger level and travel duration.

| Event Type | Trigger | Outcome |
|------------|---------|---------|
| **Encounter** | Danger roll | Combat, dialogue, or flight |
| **Discovery** | Perception check | New POI revealed |
| **Hazard** | Terrain type | Resource loss or damage |
| **Opportunity** | Random | Temporary benefit (cache, friendly NPC) |
| **Breakdown** | Vehicle condition | Repair or abandon vehicle |

### 4.3 Fuel Economy

Fuel is a critical resource for vehicle travel.

| Fuel Type | Source | Vehicle Type | Efficiency |
|-----------|--------|--------------|------------|
| **Petroleum** | Scavenged, refined | Combustion vehicles | Standard |
| **Biofuel** | Crafted from organics | Combustion vehicles | Lower power |
| **Energy Cells** | Scavenged, charged | Electric vehicles | High efficiency |
| **Rift Charges** | Harvested from rifts | Rift-tech vehicles | Dangerous but fast |

---

## 5. Combat System

### 5.1 Combat Model

Turn-based tactical combat on a grid. Party vs. hostiles, with positioning, cover, and ability usage.

#### 5.1.1 Turn Structure

```
ROUND
├── Initiative Phase (determine order)
├── Turn 1: Agent acts (AP expenditure)
├── Turn 2: Agent acts
├── ...
├── Turn N: Last agent acts
├── End of Round (status effects tick, cooldowns reduce)
└── Next Round
```

#### 5.1.2 Action Points (AP)

Each character has AP per turn, spent on actions.

| Base AP | Modifier Sources |
|---------|------------------|
| 3 AP | +1 from high Agility (16+) |
|      | -1 from heavy armor |
|      | -1 from severe injuries |
|      | +1 from Tactics abilities |

#### 5.1.3 Action Costs

| Action | AP Cost | Notes |
|--------|---------|-------|
| **Move (per square)** | 0.5 AP | Difficult terrain costs 1 AP |
| **Basic Attack** | 1 AP | Melee or ranged |
| **Heavy Attack** | 2 AP | Higher damage, may have effects |
| **Use Ability** | Varies | 1-3 AP depending on ability |
| **Use Item** | 1 AP | Consumables, equipment swap |
| **Take Cover** | 0.5 AP | Improves defense until move |
| **Reload** | 1 AP | Required when ammunition depleted |
| **Stabilize Ally** | 2 AP | Prevent death from incapacitation |
| **Interact** | 1 AP | Environment, objects |

### 5.2 Combat Resources

#### 5.2.1 Ammunition

Ranged weapons require ammunition. Different weapons use different ammo types.

| Ammo Type | Used By | Scarcity | Craftable |
|-----------|---------|----------|-----------|
| **Ballistic (Common)** | Handguns, SMGs | Common | Yes |
| **Ballistic (Rifle)** | Rifles, snipers | Moderate | Yes |
| **Shells** | Shotguns | Moderate | Yes |
| **Arrows/Bolts** | Bows, crossbows | Common | Yes, recoverable |
| **Energy Cells** | Energy weapons | Rare | Requires tech skill |
| **Rift Charges** | Rift weapons | Very Rare | Harvested only |

#### 5.2.2 Health & Damage

```
DAMAGE CALCULATION:
Damage Dealt = Weapon Base + Skill Modifier + Attribute Modifier
Damage Taken = Damage Dealt - Armor Reduction - Cover Bonus

If Health ≤ 0:
  - Companion: Incapacitated (3 turns to stabilize or death)
  - Protagonist: Death (reload required)
```

#### 5.2.3 Injury System

Significant hits (critical damage, incapacitation recovery) inflict injuries.

| Severity | Heal Time | Combat Penalty | Skill Penalty |
|----------|-----------|----------------|---------------|
| **Minor** | 8 hours rest | None | -5% related skills |
| **Moderate** | 3 days + treatment | -1 AP | -15% related skills |
| **Severe** | 1 week + surgery | -2 AP | -30% related skills, condition |
| **Critical** | Permanent | Lasting debuff | May require prosthetic |

### 5.3 Combat Loot

Defeated enemies drop:
- Equipped gear (condition reduced)
- Carried consumables/ammo
- Materials (butchering creatures, scrapping equipment)
- Occasionally unique items or artifacts

---

## 6. Crafting System

### 6.1 Crafting Philosophy

Crafting transforms scavenged materials into useful items. No item is truly unique in base crafting—but the **Artifact Forge** system (Section 11) allows creation of one-of-a-kind items.

### 6.2 Material Types

| Category | Examples | Primary Use |
|----------|----------|-------------|
| **Scrap** | Metal scraps, plastic, glass | Basic components, repairs |
| **Components** | Gears, wiring, circuits, springs | Mechanical/electronic crafting |
| **Chemicals** | Acids, solvents, compounds | Medicine, ammunition, explosives |
| **Organics** | Leather, bone, plant fiber | Armor, food, medicine |
| **Tech Salvage** | Processors, sensors, power cells | High-tech equipment |
| **Rift Crystals** | Raw rift energy, crystallized | Arcane crafting, rift-tech |
| **Exotic Matter** | Dimensional materials, anomalies | Artifact creation, rare crafting |

### 6.3 Crafting Stations

Crafting requires appropriate workstations (found in world or built).

| Station | Enables | Skill Required |
|---------|---------|----------------|
| **Workbench** | Basic repairs, simple items | Mechanics |
| **Forge** | Metal weapons, armor | Mechanics, Engineering |
| **Chemistry Lab** | Medicine, chemicals, ammo | Chemistry |
| **Electronics Bench** | Tech items, energy weapons | Electronics |
| **Rift Altar** | Arcane items, rift-tech | Rift Manipulation |
| **Artifact Forge** | Unique items (see Section 11) | Multiple skills |

### 6.4 Recipe System

Recipes are learned through:
- Skill progression (automatic unlocks at proficiency thresholds)
- Discovery (finding schematics, studying artifacts)
- Experimentation (combining materials at stations—risky)

#### 6.4.1 Recipe Structure

```
RECIPE: Combat Stimulant
├── Station: Chemistry Lab
├── Skill: Chemistry 25
├── Inputs:
│   ├── Chemical Compound × 2
│   ├── Medical Supplies × 1
│   └── Empty Injector × 1
├── Time: 30 minutes
├── Output: Combat Stimulant × 2
└── Quality: Affected by Chemistry proficiency
```

### 6.5 Item Quality

Crafted item quality depends on:
- Crafter's skill proficiency
- Material quality (some materials have purity ratings)
- Station quality (damaged stations reduce quality)
- Recipe complexity vs. skill level

| Quality Tier | Effect | Skill Threshold |
|--------------|--------|-----------------|
| **Poor** | -20% effectiveness | Below recipe requirement |
| **Standard** | Base stats | Meets requirement |
| **Quality** | +10% effectiveness | Requirement + 15 |
| **Superior** | +20% effectiveness | Requirement + 30 |
| **Masterwork** | +30% effectiveness, unique property | Requirement + 50 |

### 6.6 Repair System

Equipment degrades with use. Repair requires materials and skill.

```
REPAIR COST:
Materials = (100 - Current Condition) / 20 × Base Material Cost
Time = (100 - Current Condition) / 10 × Base Time
Skill Check = Item Complexity vs. Relevant Skill
```

---

## 7. Magic System (Arcane)

### 7.1 Magic Origin

Magic in Andara's World comes from the Convergence—it's dimensional energy bleeding through rifts. It's not supernatural but rather alternative physics from other dimensions. This makes it:
- **Dangerous**: Rifts are unstable; magic use has risks
- **Alien**: Effects may not follow intuitive rules
- **Learnable**: Anyone can develop arcane skills, but sensitivity varies

### 7.2 Rift Energy

The core magical resource is **Rift Energy**, accumulated and spent.

#### 7.2.1 Rift Energy Sources

| Source | Yield | Risk |
|--------|-------|------|
| **Passive Ambient** | Slow regeneration near rifts | Rift exposure effects |
| **Rift Crystals** | Consumable, moderate energy | None (already harvested) |
| **Rift Harvesting** | High yield from active rifts | Dangerous, attracts entities |
| **Dimensional Anchors** | Steady regeneration in area | Must be found/created |
| **Sacrifice** | Convert health/stamina to energy | Direct character cost |

#### 7.2.2 Rift Energy Pool

Characters with Arcane skills have a Rift Energy pool.

```
Max Rift Energy = Base (10) + (Perception × 2) + (Arcane Skills Average × 0.5)
Regeneration = 1/hour base, modified by location and equipment
```

### 7.3 Arcane Skills

| Skill | Function | Abilities |
|-------|----------|-----------|
| **Rift Manipulation** | Direct energy control | Blasts, shields, telekinesis |
| **Binding** | Control entities and objects | Summon, banish, dominate |
| **Wards** | Protective magic | Barriers, cleansing, sealing |
| **Perception** | Sensing and knowledge | Detect magic, identify, scry |

### 7.4 Arcane Abilities

Abilities cost Rift Energy and may have additional requirements.

| Ability | Skill | Proficiency | Energy Cost | Effect |
|---------|-------|-------------|-------------|--------|
| **Rift Bolt** | Manipulation | 10 | 5 | Ranged energy damage |
| **Phase Step** | Manipulation | 25 | 10 | Short-range teleport |
| **Summon Wisp** | Binding | 15 | 8 | Light source, minor scout |
| **Bind Entity** | Binding | 40 | 20 | Control rift creature |
| **Energy Ward** | Wards | 20 | 12 | Damage resistance aura |
| **Seal Rift** | Wards | 50 | 30 | Close minor rift |
| **Rift Sight** | Perception | 10 | 3 | See magical auras |
| **Identify Artifact** | Perception | 30 | 10 | Reveal item properties |

### 7.5 Arcane Risks

Magic use carries risks, especially at high power or near unstable rifts.

| Risk | Trigger | Effect |
|------|---------|--------|
| **Rift Flare** | Critical failure | Uncontrolled energy release, damage to user |
| **Dimensional Bleed** | Extended use | Temporary perception distortion |
| **Rift Sickness** | Overuse/exposure | Accumulating debuff, requires rest |
| **Entity Attention** | Binding failures | Hostile rift creature summoned |
| **Anchor Instability** | Massive energy use | Local rift becomes unstable |

---

## 8. Technology System

### 8.1 Tech Tiers

Post-Convergence technology exists in layers:

| Tier | Description | Availability | Examples |
|------|-------------|--------------|----------|
| **Salvage** | Pre-Convergence tech, degraded | Common | Basic firearms, vehicles, tools |
| **Maintained** | Salvage kept working | Moderate | Working electronics, powered vehicles |
| **Restored** | Fully repaired to original spec | Rare | Pre-war weapons, computers |
| **Adapted** | Modified for post-Convergence use | Rare | Rift-shielded tech, hybrid systems |
| **Rift-Tech** | Technology using dimensional energy | Very Rare | Rift drives, dimensional storage |

### 8.2 Technology Skills

| Skill | Function | Applications |
|-------|----------|--------------|
| **Mechanics** | Physical systems | Vehicles, weapons, structures |
| **Electronics** | Circuits and power | Computers, sensors, energy weapons |
| **Chemistry** | Substances and reactions | Medicine, explosives, fuel |
| **Engineering** | Complex systems, design | Advanced construction, optimization |

### 8.3 Tech Items

#### 8.3.1 Weapons

| Weapon Type | Tech Tier | Ammo | Special |
|-------------|-----------|------|---------|
| **Pipe Weapons** | Salvage | Ballistic | Cheap, unreliable |
| **Firearms** | Maintained | Ballistic | Standard performance |
| **Precision Rifles** | Restored | Rifle | High accuracy, range |
| **Energy Pistol** | Adapted | Energy Cell | No recoil, armor piercing |
| **Plasma Rifle** | Rift-Tech | Rift Charge | Devastating, dangerous |

#### 8.3.2 Armor

| Armor Type | Tech Tier | Protection | Penalty |
|------------|-----------|------------|---------|
| **Scrap Armor** | Salvage | Low | None |
| **Leather/Composite** | Maintained | Medium | None |
| **Combat Armor** | Restored | High | -1 AP |
| **Power Armor** | Adapted | Very High | Requires power, training |
| **Rift-Weave** | Rift-Tech | Medium + Magic Resist | Rift exposure |

#### 8.3.3 Tools & Equipment

| Item | Function | Skill Enhanced |
|------|----------|----------------|
| **Multi-tool** | Field repairs | Mechanics |
| **Diagnostic Scanner** | Identify tech, find faults | Electronics |
| **Chem Analyzer** | Identify substances | Chemistry |
| **Portable Workbench** | Basic crafting anywhere | All Tech |
| **Rift Detector** | Sense dimensional energy | Perception (Arcane) |

### 8.4 Power Systems

High-tech equipment requires power.

| Power Source | Duration | Recharge Method | Availability |
|--------------|----------|-----------------|--------------|
| **Batteries** | Limited | Replace or solar | Common |
| **Fusion Cells** | Long | Cannot recharge, find new | Rare |
| **Energy Cells** | Medium | Charge at power source | Moderate |
| **Rift Core** | Indefinite | Near rift or rift crystal | Very Rare |

---

## 9. Skills & Progression

### 9.1 Skill System Overview

Skills improve through use. Each successful skill check grants experience toward that skill.

```
SKILL PROGRESSION:
Experience Gained = Base (10) × Difficulty Modifier × Success Quality
Level Up Threshold = Current Level × 100

Difficulty Modifiers:
  Trivial (DC 5): 0.5x
  Easy (DC 10): 0.75x
  Moderate (DC 15): 1.0x
  Hard (DC 20): 1.5x
  Very Hard (DC 25): 2.0x
  Extreme (DC 30+): 3.0x
```

### 9.2 Skill Categories

#### 9.2.1 Combat Skills

| Skill | Governs | Key Abilities |
|-------|---------|---------------|
| **Melee** | Close combat with weapons | Power Attack, Riposte, Cleave |
| **Ranged** | Firearms and thrown | Aimed Shot, Suppression, Quick Draw |
| **Energy Weapons** | Laser, plasma, rift weapons | Overcharge, Beam Sweep |
| **Unarmed** | Hand-to-hand, martial arts | Stunning Blow, Grapple, Counter |
| **Tactics** | Combat leadership, positioning | Coordinate, Overwatch, Rally |

#### 9.2.2 Survival Skills

| Skill | Governs | Key Abilities |
|-------|---------|---------------|
| **Scavenging** | Finding resources | Keen Eye, Efficient Salvage |
| **Tracking** | Following trails, hunting | Read Signs, Predict Movement |
| **Medicine** | Healing, treating injuries | First Aid, Surgery, Diagnosis |
| **Navigation** | Travel efficiency, orientation | Shortcut, Avoid Hazard |

#### 9.2.3 Technical Skills

| Skill | Governs | Key Abilities |
|-------|---------|---------------|
| **Mechanics** | Physical construction/repair | Jury Rig, Optimize, Fortify |
| **Electronics** | Circuits, hacking, sensors | Bypass, Overload, Enhance |
| **Chemistry** | Compounds, medicine, explosives | Synthesize, Analyze, Stabilize |
| **Engineering** | Complex design, efficiency | Blueprint, Automate, Improve |

#### 9.2.4 Arcane Skills

| Skill | Governs | Key Abilities |
|-------|---------|---------------|
| **Rift Manipulation** | Direct energy use | Bolt, Shield, Telekinesis |
| **Binding** | Entity control | Summon, Banish, Command |
| **Wards** | Protection, sealing | Barrier, Cleanse, Seal |
| **Perception (Arcane)** | Magical sensing | Detect, Identify, Scry |

#### 9.2.5 Social Skills

| Skill | Governs | Key Abilities |
|-------|---------|---------------|
| **Barter** | Trading, prices | Haggle, Appraise, Connect |
| **Intimidation** | Threats, dominance | Threaten, Interrogate, Unnerve |
| **Leadership** | Party morale, NPCs | Inspire, Command, Unite |
| **Deception** | Lying, disguise | Bluff, Misdirect, Forge |

### 9.3 Ability Unlocks

Abilities unlock at proficiency thresholds.

| Threshold | Unlock |
|-----------|--------|
| 10 | Basic ability |
| 25 | Intermediate ability |
| 40 | Advanced ability |
| 60 | Expert ability |
| 80 | Master ability |

### 9.4 Soft Caps

Skills have soft caps that make further progression expensive.

| Range | Experience Multiplier |
|-------|----------------------|
| 0-40 | 1.0x |
| 41-60 | 1.5x |
| 61-80 | 2.0x |
| 81-100 | 4.0x |

---

## 10. Economy System

### 10.1 Credits

**Credits** are the universal currency for player-facing transactions, including the Dimensional Market.

#### 10.1.1 Credit Sources

| Source | Typical Yield |
|--------|---------------|
| **Selling Items** | Item value × condition × barter modifier |
| **Quest Rewards** | Variable, usually 50-500 |
| **Looting** | Enemies carry 0-50 typically |
| **Market Arbitrage** | Buy low, sell high across instances |

#### 10.1.2 Credit Sinks

| Sink | Typical Cost |
|------|--------------|
| **Buying Items** | Item value × vendor markup × barter modifier |
| **Services** | 10-200 (repairs, healing, information) |
| **Faction Tributes** | Access fees, protection money |
| **Market Listing Fees** | Percentage of asking price |

### 10.2 Faction Standing

Standing with factions unlocks access and improves prices.

| Standing Level | Value Range | Effects |
|----------------|-------------|---------|
| **Hostile** | -100 to -50 | Attack on sight, no trade |
| **Unfriendly** | -49 to -10 | Refused entry, no trade |
| **Neutral** | -9 to +9 | Basic access, standard prices |
| **Friendly** | +10 to +49 | Full access, 10% discount |
| **Allied** | +50 to +100 | Special access, 20% discount, quests |

#### 10.2.1 Standing Changes

| Action | Standing Effect |
|--------|-----------------|
| **Complete faction quest** | +10 to +30 |
| **Trade with faction** | +1 per 100 credits |
| **Kill faction member** | -20 to -50 |
| **Help faction enemy** | -5 to -15 |
| **Donate resources** | +1 per 50 credits value |

### 10.3 Local Trade

NPCs and settlements trade at local rates.

```
BUY PRICE = Base Value × Vendor Markup (1.5-2.0) × Scarcity Modifier / Barter Modifier
SELL PRICE = Base Value × Condition × Demand Modifier × Barter Modifier

Barter Modifier = 1.0 + (Barter Skill × 0.005) + (Charisma × 0.02)
```

### 10.4 Dimensional Market

The Dimensional Market connects all player instances for asynchronous trade. See Section 11 for unique items specifically created for market trade.

#### 10.4.1 Market Structure

```
DIMENSIONAL MARKET
├── Listings (items for sale by players across instances)
├── Price Index (aggregate prices by item type)
├── Search & Filter (find specific items)
├── Transaction History (your sales and purchases)
└── Reputation (buyer/seller reliability—future feature)
```

#### 10.4.2 Market Rules

| Rule | Value |
|------|-------|
| **Listing Fee** | 5% of asking price (non-refundable) |
| **Transaction Fee** | 5% of sale price (from seller) |
| **Listing Duration** | 7 days (game time) |
| **Maximum Active Listings** | 20 per player |

#### 10.4.3 Price Discovery

Prices emerge from supply and demand across instances.

```
PRICE INDEX CALCULATION:
Rolling average of last 100 transactions for item type
Displayed to players as guidance
Players set their own prices; market determines what sells
```

---

## 11. Artifact Forge System (Unique Item Creation)

### 11.1 Design Goals

The Artifact Forge allows players to create **one-of-a-kind items** that have trade value in the Dimensional Market. This system:
- Rewards exploration (finding exotic materials)
- Rewards skill investment (crafting quality)
- Creates market differentiation (your items are unique)
- Enables player identity (signature creations)

### 11.2 Artifact Forge Overview

Unlike standard crafting (which produces known items from recipes), the Artifact Forge combines materials in ways that produce **unique outcomes** based on inputs and crafter skill.

```
ARTIFACT CREATION:
┌─────────────────────────────────────────────────────────────────┐
│                      ARTIFACT FORGE                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  INPUTS                          PROCESS                        │
│  ├── Base Item (weapon/armor)    ├── Skill Checks              │
│  ├── Primary Material            ├── Random Seed               │
│  ├── Secondary Material          ├── Crafter Signature         │
│  ├── Catalyst (optional)         └── Time Investment           │
│  └── Rift Energy                                                │
│                                                                 │
│  OUTPUT                                                         │
│  └── Named Artifact with unique property combination           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 11.3 Artifact Components

#### 11.3.1 Base Item

The foundation determines the artifact type (weapon, armor, accessory).

| Base Type | Determines |
|-----------|------------|
| **Weapon** | Damage type, range, base stats |
| **Armor** | Slot, base protection |
| **Accessory** | Slot (ring, amulet, etc.), base effect |
| **Tool** | Function, base efficiency |

#### 11.3.2 Primary Material

The main material determines the dominant property theme.

| Material | Property Theme | Rarity |
|----------|---------------|--------|
| **Rift Crystal (Red)** | Damage, aggression | Rare |
| **Rift Crystal (Blue)** | Protection, stability | Rare |
| **Rift Crystal (Green)** | Healing, regeneration | Rare |
| **Exotic Alloy** | Durability, physical power | Very Rare |
| **Dimensional Silk** | Evasion, speed | Very Rare |
| **Void Fragment** | Energy, arcane power | Very Rare |
| **Temporal Shard** | Cooldowns, action economy | Extremely Rare |
| **Living Metal** | Adaptation, growth | Extremely Rare |

#### 11.3.3 Secondary Material

Modifies and specializes the primary theme.

| Material | Modification |
|----------|--------------|
| **Beast Parts** | Adds creature-related effects |
| **Tech Components** | Adds technological effects |
| **Alchemical Compounds** | Adds elemental effects |
| **Faction Relics** | Adds faction-related effects |
| **Player-Crafted Component** | Inherits quality and properties |

#### 11.3.4 Catalyst (Optional)

Catalysts add risk/reward—they can enhance outcomes or cause failures.

| Catalyst | Effect | Risk |
|----------|--------|------|
| **Stabilizer** | Reduces variance | Lower ceiling |
| **Amplifier** | Increases power | Higher failure chance |
| **Mutagen** | Adds random property | May be negative |
| **Essence** | Guarantees specific effect | Expensive, consumed |

### 11.4 Artifact Properties

Artifacts gain properties based on materials, skill, and randomization.

#### 11.4.1 Property Generation

```
PROPERTY ALGORITHM:
1. Primary Material → 1-2 themed properties
2. Secondary Material → 0-1 modifier properties
3. Crafter Skill → Quality modifier on all properties
4. Random Roll → Additional property (rare)
5. Catalyst → Modified outcome per catalyst type
6. Name Generation → Based on materials and properties
```

#### 11.4.2 Property Types

| Category | Example Properties |
|----------|-------------------|
| **Offensive** | +Damage, +Critical, +Penetration, Elemental damage |
| **Defensive** | +Protection, +Resistance, +Evasion, Damage reduction |
| **Utility** | +Skill bonus, Reduced weight, Extended duration |
| **Arcane** | Rift energy bonus, Spell enhancement, Magic resistance |
| **Unique** | Named effects (Lifestealing, Phasing, Time Dilation) |

#### 11.4.3 Property Strength

Property strength scales with:
- Material rarity
- Crafter skill in relevant disciplines
- Base item quality
- Random variance (within bounds)

| Tier | Strength Range | Requires |
|------|---------------|----------|
| **Minor** | +1 to +5 | Rare materials |
| **Moderate** | +6 to +10 | Very Rare materials, Skill 40+ |
| **Major** | +11 to +15 | Extremely Rare materials, Skill 60+ |
| **Legendary** | +16 to +20 | Perfect materials, Skill 80+, luck |

### 11.5 Artifact Naming

Artifacts receive generated names based on their properties.

```
NAME GENERATION:
[Prefix] + [Base Item] + "of" + [Suffix]

Prefix: Derived from primary material (Rift-Touched, Void-Forged, etc.)
Base Item: The foundation (Blade, Armor, Ring, etc.)
Suffix: Derived from dominant property (the Wolf, Shadows, Flames, etc.)

Example: "Void-Forged Blade of the Consuming Shadow"
```

Players can rename artifacts, but the generated name is preserved as lore.

### 11.6 Crafter Signature

Each artifact records its crafter.

```
ARTIFACT METADATA:
├── Crafter Name (Protagonist name)
├── Instance Origin (dimension ID)
├── Creation Date (world time)
├── Materials Used (list)
└── Forge Location (where crafted)
```

This creates provenance—buyers know who made it and where.

### 11.7 Market Value

Artifact value in the Dimensional Market depends on:

| Factor | Value Impact |
|--------|--------------|
| **Property Strength** | Primary driver |
| **Property Synergy** | Complementary properties worth more |
| **Rarity of Materials** | Visible to buyers |
| **Crafter Reputation** | Future feature: track sale history |
| **Uniqueness** | Rare property combinations |

### 11.8 Artifact Forge Progression

The Artifact Forge itself can be improved.

| Forge Level | Requires | Benefit |
|-------------|----------|---------|
| **Basic** | Found or built | Standard crafting |
| **Enhanced** | Rare components + Engineering 40 | +1 property slot |
| **Master** | Exotic components + Engineering 60 | Better quality floor |
| **Legendary** | Unique quest/discovery | Unique property access |

---

## 12. Resource Flow Diagram

### 12.1 Core Loops

```
EXPLORATION LOOP:
Travel → Consume (Time, Fuel, Food) → Discover POI → 
  → Encounter → Resolve (Combat/Dialogue) → 
  → Loot (Materials, Items, Credits) → Return

CRAFTING LOOP:
Gather Materials → Identify Recipe → 
  → Craft at Station → Consume (Materials, Time) → 
  → Produce Item → Use or Trade

COMBAT LOOP:
Encounter → Initiative → 
  → Actions (Consume AP, Ammo, Abilities) → 
  → Damage/Effects → Resolution → 
  → Loot → Injury Treatment (Consume Medical)

ECONOMIC LOOP:
Produce/Find Items → Value Assessment → 
  → Local Sale or Market Listing → 
  → Credits Received → Purchase Needed Resources

ARTIFACT LOOP:
Explore for Exotic Materials → Accumulate Rare Components →
  → Artifact Forge → Unique Creation →
  → Use or Market Listing → Premium Credits → Reinvest
```

### 12.2 Resource Interconnections

```
┌─────────────────────────────────────────────────────────────────┐
│                    RESOURCE DEPENDENCIES                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  EXPLORATION ─────→ Materials ─────→ CRAFTING                   │
│       │                                  │                      │
│       ▼                                  ▼                      │
│  COMBAT ←──────── Equipment ←─────── Items                     │
│       │                                  │                      │
│       ▼                                  ▼                      │
│  Loot/Credits ──→ ECONOMY ────────→ Supplies ──→ SURVIVAL      │
│                      │                                          │
│                      ▼                                          │
│              Dimensional Market                                 │
│                      │                                          │
│                      ▼                                          │
│              Artifacts ←──────────── ARTIFACT FORGE             │
│                                           ▲                     │
│                                           │                     │
│                        Exotic Materials ──┘                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 13. Balance Targets

### 13.1 Pacing Targets

| Metric | Target |
|--------|--------|
| **Combat Encounter Length** | 5-15 minutes |
| **Exploration Session** | 30-60 minutes |
| **Full Play Session** | 2-4 hours |
| **Skill Proficiency 0→40** | 10-15 hours |
| **Skill Proficiency 40→80** | 30-50 hours |
| **Full Region Exploration** | 15-25 hours |

### 13.2 Resource Pressure

| Resource | Target Pressure |
|----------|-----------------|
| **Food/Water** | Mild—manageable with minimal scavenging |
| **Ammunition** | Moderate—encourages varied tactics |
| **Medical Supplies** | High—injuries should feel costly |
| **Rift Energy** | Very High—magic is powerful but limited |
| **Credits** | Moderate—never rich, always striving |
| **Exotic Materials** | Very High—artifacts are rare achievements |

### 13.3 Power Curve

```
EARLY GAME (0-20 hours):
- Struggling for basic survival
- Limited combat options
- Local trade only
- Basic crafting

MID GAME (20-60 hours):
- Stable survival
- Diverse combat builds
- Market participation
- Standard crafting, first artifacts

LATE GAME (60+ hours):
- Survival mastered
- Powerful builds, signature style
- Market influence
- Legendary artifacts
```

---

## 14. Open Questions

### 14.1 Design Questions

1. **Turn-based vs. Hybrid Combat**: Pure turns or real-time with pause option?
2. **Party Companion Limit**: Hard cap at 6, or allow temporary overflow?
3. **Permadeath Option**: Offer hardcore mode where companions also permadeath?
4. **Artifact Trade Economy**: Auction vs. fixed price listings?
5. **Crafter Reputation**: Implement visible crafter ratings?

### 14.2 Content Questions

1. **Starting Region**: Urban ruins, wilderness, or mixed?
2. **Faction Count**: How many factions in prototype vs. full game?
3. **Skill Count**: Current 20 skills—too many for prototype?
4. **Artifact Property Count**: How many unique properties to implement?

### 14.3 Technical Questions

1. **Artifact Generation Seed**: Deterministic from inputs, or true random?
2. **Market Sync Frequency**: How often do prices update across instances?
3. **Material Spawn Distribution**: Static locations or procedural?

---

## Appendix A: Prototype Scope Mapping

For the 3-month prototype, suggested inclusions:

| System | Prototype Scope | Deferred |
|--------|----------------|----------|
| **Character Creation** | 3 origins, full attributes, 10 skills | Full origin list |
| **Companions** | 2-3 fixed companions | Recruitment system |
| **Survival** | Food/water only | Full hazard system |
| **Travel** | On foot only | Vehicles, mounts |
| **Combat** | Turn-based, basic abilities | Full ability trees |
| **Crafting** | 20 recipes, 2 stations | Artifact Forge |
| **Magic** | 5 abilities, 1 arcane skill | Full arcane tree |
| **Technology** | Salvage tier only | Higher tech tiers |
| **Economy** | Local trade only | Dimensional Market |
| **Skills** | 10 skills, simplified progression | Full 20 skills |

---

## Appendix B: Item Templates (Sample)

### Weapons

| Item | Type | Damage | Ammo | Special |
|------|------|--------|------|---------|
| Pipe Pistol | Handgun | 8-12 | Ballistic | Unreliable |
| Hunting Rifle | Rifle | 15-22 | Rifle | Accurate |
| Machete | Melee | 10-15 | — | Bleed chance |
| Shock Baton | Melee | 6-10 | Energy Cell | Stun chance |
| Crossbow | Ranged | 12-18 | Bolts | Silent, recoverable |

### Armor

| Item | Type | Protection | Penalty | Special |
|------|------|------------|---------|---------|
| Leather Jacket | Light | 5 | — | — |
| Scrap Vest | Medium | 10 | — | — |
| Combat Armor | Heavy | 20 | -1 AP | — |
| Hazmat Suit | Light | 3 | — | Rad/Toxic resist |

### Consumables

| Item | Effect | Duration | Craftable |
|------|--------|----------|-----------|
| Stimpak | Heal 30 HP | Instant | Yes |
| Rad-Away | Remove 50 rads | Instant | Yes |
| Combat Stim | +1 AP | 3 turns | Yes |
| Rift Tonic | +20 Rift Energy | Instant | Yes |

---

*End of Document*
