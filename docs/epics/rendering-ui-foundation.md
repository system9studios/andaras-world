# Epic: Rendering & UI Foundation

**Epic Goal**: Establish the client-side rendering architecture and UI framework that displays the game world, handles user interactions, and maintains responsive state updates.

**Technical Context**: This epic implements the presentation layer using React for UI components, Redux for state management, and WebGL (via PixiJS) for game world rendering. The architecture separates game world rendering (canvas) from UI chrome (React components).

---

## Story 1: React Application Bootstrap and Redux Store

**As a** frontend developer  
**I want** the React application scaffolded with Redux Toolkit state management  
**So that** I have a foundation for building UI components and managing game state

### Acceptance Criteria

- [ ] Create React App (or Vite) project initialized with TypeScript
- [ ] Redux Toolkit installed and configured
- [ ] Root Redux store created with slices for:
  - `gameSlice` - session, instance, world time, status
  - `partySlice` - party members, position, inventory
  - `worldSlice` - regions, zones, POIs, visibility
  - `combatSlice` - combat state (initially empty structure)
  - `uiSlice` - UI state (open panels, selections, notifications)

- [ ] Redux DevTools integration configured
- [ ] Store type exports for `RootState` and `AppDispatch`
- [ ] Custom hooks created:
  - `useAppDispatch()` - Typed dispatch hook
  - `useAppSelector()` - Typed selector hook

- [ ] Environment configuration for API endpoints:
  - `VITE_API_URL` or `REACT_APP_API_URL`
  - Development vs production settings

- [ ] Basic routing setup with React Router:
  - `/` - Main menu
  - `/character-creation` - Character creation flow
  - `/game` - Main game view

- [ ] Error boundary component created for graceful error handling

- [ ] Tests verify:
  - Store initializes with correct shape
  - Actions can be dispatched
  - Selectors return expected values
  - Routes navigate correctly

### Technical Notes

```typescript
// store/index.ts
import { configureStore } from '@reduxjs/toolkit';
import gameReducer from './slices/gameSlice';
import partyReducer from './slices/partySlice';
import worldReducer from './slices/worldSlice';
import combatReducer from './slices/combatSlice';
import uiReducer from './slices/uiSlice';

export const store = configureStore({
  reducer: {
    game: gameReducer,
    party: partyReducer,
    world: worldReducer,
    combat: combatReducer,
    ui: uiReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore paths with non-serializable values if needed
        ignoredActions: ['api/webSocketMessage'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

// store/hooks.ts
import { useDispatch, useSelector, TypedUseSelectorHook } from 'react-redux';
import type { RootState, AppDispatch } from './index';

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

// store/slices/gameSlice.ts
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface GameState {
  instanceId: string | null;
  sessionId: string | null;
  status: 'menu' | 'loading' | 'playing' | 'paused' | 'combat';
  worldTime: {
    ticks: number;
    day: number;
    hour: number;
  };
}

const initialState: GameState = {
  instanceId: null,
  sessionId: null,
  status: 'menu',
  worldTime: { ticks: 0, day: 1, hour: 6 },
};

export const gameSlice = createSlice({
  name: 'game',
  initialState,
  reducers: {
    setGameStatus: (state, action: PayloadAction<GameState['status']>) => {
      state.status = action.payload;
    },
    setInstanceId: (state, action: PayloadAction<string>) => {
      state.instanceId = action.payload;
    },
    updateWorldTime: (state, action: PayloadAction<number>) => {
      state.worldTime.ticks = action.payload;
      state.worldTime.day = Math.floor(action.payload / 24);
      state.worldTime.hour = action.payload % 24;
    },
  },
});

export const { setGameStatus, setInstanceId, updateWorldTime } = gameSlice.actions;
export default gameSlice.reducer;
```

**Effort**: 3 points  
**Dependencies**: None  
**Priority**: P0 (blocking)

---

## Story 2: API Client and WebSocket Manager

**As a** frontend developer  
**I want** REST API and WebSocket client services  
**So that** I can communicate with the backend and receive real-time updates

### Acceptance Criteria

- [ ] `ApiClient` service created with methods for all game endpoints:
  - `startGame(characterData): Promise<GameStartResponse>`
  - `loadGame(saveId): Promise<GameLoadResponse>`
  - `saveGame(): Promise<SaveGameResponse>`
  - `moveParty(zoneId): Promise<CommandResponse>`
  - `executeAction(action): Promise<CommandResponse>`
  - Generic `command(cmd): Promise<CommandResponse>`
  - Generic `query(path): Promise<QueryResponse>`

- [ ] Axios (or fetch) configured with:
  - Base URL from environment
  - Request/response interceptors for error handling
  - Authentication headers (future)
  - Retry logic for transient failures (3 attempts, exponential backoff)

- [ ] `WebSocketManager` service created with:
  - Connection management (connect, disconnect, reconnect)
  - Message routing by event type
  - Subscription management for topics
  - Automatic reconnection on disconnect (max 5 attempts)
  - Connection state tracking

- [ ] Redux middleware created for:
  - `apiMiddleware` - Intercepts API actions, calls backend, dispatches results
  - `websocketMiddleware` - Listens for WebSocket messages, dispatches to store

- [ ] Error handling for:
  - Network failures
  - 4xx/5xx responses
  - Timeout errors
  - WebSocket disconnections

- [ ] TypeScript types defined for all API request/response shapes

- [ ] Tests verify:
  - API calls are made with correct payloads
  - Responses update Redux state
  - WebSocket messages dispatch actions
  - Errors are handled gracefully
  - Reconnection logic works

### Technical Notes

```typescript
// api/client.ts
import axios, { AxiosInstance } from 'axios';

class ApiClient {
  private client: AxiosInstance;

  constructor(baseURL: string) {
    this.client = axios.create({
      baseURL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Response interceptor for error handling
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        console.error('API Error:', error);
        // Transform error for Redux
        throw {
          message: error.response?.data?.message || error.message,
          status: error.response?.status,
        };
      }
    );
  }

  async startGame(characterData: CharacterCreationData): Promise<GameStartResponse> {
    const response = await this.client.post('/api/v1/game/start', characterData);
    return response.data;
  }

  async moveParty(partyId: string, targetZoneId: string): Promise<CommandResponse> {
    const response = await this.client.post('/api/v1/party/move', {
      partyId,
      targetZoneId,
    });
    return response.data;
  }

  async getGameState(): Promise<GameStateResponse> {
    const response = await this.client.get('/api/v1/game/state');
    return response.data;
  }
}

export const apiClient = new ApiClient(import.meta.env.VITE_API_URL);

// api/websocket.ts
class WebSocketManager {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private listeners: Map<string, Set<(data: any) => void>> = new Map();

  connect(url: string) {
    this.ws = new WebSocket(url);

    this.ws.onopen = () => {
      console.log('WebSocket connected');
      this.reconnectAttempts = 0;
    };

    this.ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      this.notifyListeners(message.type, message.data);
    };

    this.ws.onclose = () => {
      console.log('WebSocket disconnected');
      this.attemptReconnect(url);
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  }

  private attemptReconnect(url: string) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      setTimeout(() => {
        console.log(`Reconnecting... attempt ${this.reconnectAttempts}`);
        this.connect(url);
      }, this.reconnectDelay * this.reconnectAttempts);
    }
  }

  subscribe(eventType: string, callback: (data: any) => void) {
    if (!this.listeners.has(eventType)) {
      this.listeners.set(eventType, new Set());
    }
    this.listeners.get(eventType)!.add(callback);
  }

  private notifyListeners(eventType: string, data: any) {
    const listeners = this.listeners.get(eventType);
    if (listeners) {
      listeners.forEach((callback) => callback(data));
    }
  }

  send(message: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}

export const wsManager = new WebSocketManager();

// store/middleware/apiMiddleware.ts
import { Middleware } from '@reduxjs/toolkit';
import { apiClient } from '../../api/client';

export const apiMiddleware: Middleware = (store) => (next) => async (action) => {
  // Example: intercept specific action types
  if (action.type === 'party/movePartyAsync') {
    try {
      store.dispatch({ type: 'party/movePartyPending' });
      const result = await apiClient.moveParty(
        action.payload.partyId,
        action.payload.targetZoneId
      );
      store.dispatch({ type: 'party/movePartyFulfilled', payload: result });
    } catch (error) {
      store.dispatch({ type: 'party/movePartyRejected', payload: error });
    }
    return;
  }

  return next(action);
};
```

**Effort**: 5 points  
**Dependencies**: Story 1  
**Priority**: P0 (blocking)

---

## Story 3: WebGL Canvas and PixiJS Initialization

**As a** game developer  
**I want** a WebGL canvas initialized with PixiJS  
**So that** I can render game world graphics efficiently

### Acceptance Criteria

- [ ] PixiJS installed and TypeScript types configured
- [ ] `<GameCanvas>` React component created that:
  - Creates canvas element
  - Initializes PixiJS Application
  - Handles canvas resize on window resize
  - Cleans up resources on unmount

- [ ] PixiJS Application configured with:
  - Resolution set to `window.devicePixelRatio`
  - Antialias enabled
  - Background color from style guide (`#0a0e14`)
  - Resize mode set to maintain aspect ratio

- [ ] Rendering layers created as `PIXI.Container`:
  - `terrainLayer` - Base terrain tiles (z-index: 0)
  - `objectsLayer` - World objects, POIs (z-index: 1)
  - `entitiesLayer` - Characters, NPCs (z-index: 2)
  - `effectsLayer` - Visual effects, animations (z-index: 3)
  - `uiOverlayLayer` - In-world UI elements (z-index: 4)

- [ ] Game loop established using `app.ticker`:
  - Update function called each frame
  - Delta time passed to update systems
  - FPS monitoring in development mode

- [ ] Canvas integrated into main game view component

- [ ] Performance optimizations:
  - Sprite batching enabled
  - Texture atlas support
  - Object pooling structure prepared

- [ ] Tests verify:
  - Canvas creates and destroys cleanly
  - Layers exist in correct z-order
  - Game loop runs
  - Resize handles correctly

### Technical Notes

```typescript
// game/renderer/GameCanvas.tsx
import React, { useEffect, useRef } from 'react';
import * as PIXI from 'pixi.js';
import { WorldRenderer } from './WorldRenderer';

export const GameCanvas: React.FC = () => {
  const canvasRef = useRef<HTMLDivElement>(null);
  const rendererRef = useRef<WorldRenderer | null>(null);

  useEffect(() => {
    if (!canvasRef.current) return;

    // Initialize PixiJS
    const app = new PIXI.Application({
      width: canvasRef.current.clientWidth,
      height: canvasRef.current.clientHeight,
      backgroundColor: 0x0a0e14,
      resolution: window.devicePixelRatio,
      autoDensity: true,
      antialias: true,
    });

    canvasRef.current.appendChild(app.view as HTMLCanvasElement);

    // Initialize renderer
    const renderer = new WorldRenderer(app);
    rendererRef.current = renderer;

    // Handle resize
    const handleResize = () => {
      if (canvasRef.current) {
        app.renderer.resize(
          canvasRef.current.clientWidth,
          canvasRef.current.clientHeight
        );
      }
    };
    window.addEventListener('resize', handleResize);

    // Cleanup
    return () => {
      window.removeEventListener('resize', handleResize);
      renderer.destroy();
      app.destroy(true, { children: true });
    };
  }, []);

  return <div ref={canvasRef} style={{ width: '100%', height: '100%' }} />;
};

// game/renderer/WorldRenderer.ts
export class WorldRenderer {
  private app: PIXI.Application;
  private layers: {
    terrain: PIXI.Container;
    objects: PIXI.Container;
    entities: PIXI.Container;
    effects: PIXI.Container;
    uiOverlay: PIXI.Container;
  };

  constructor(app: PIXI.Application) {
    this.app = app;

    // Create layers
    this.layers = {
      terrain: new PIXI.Container(),
      objects: new PIXI.Container(),
      entities: new PIXI.Container(),
      effects: new PIXI.Container(),
      uiOverlay: new PIXI.Container(),
    };

    // Add layers to stage in order
    this.app.stage.addChild(this.layers.terrain);
    this.app.stage.addChild(this.layers.objects);
    this.app.stage.addChild(this.layers.entities);
    this.app.stage.addChild(this.layers.effects);
    this.app.stage.addChild(this.layers.uiOverlay);

    // Start game loop
    this.app.ticker.add(this.update, this);
  }

  private update(deltaTime: number) {
    // Update systems each frame
    // Will be populated in later stories
  }

  destroy() {
    this.app.ticker.remove(this.update, this);
    // Clean up resources
  }

  getLayer(name: keyof typeof this.layers): PIXI.Container {
    return this.layers[name];
  }
}
```

**Effort**: 5 points  
**Dependencies**: Story 1  
**Priority**: P0 (blocking)

---

## Story 4: Camera and Viewport System

**As a** game developer  
**I want** a camera system that controls the viewport  
**So that** players can pan and zoom the game world

### Acceptance Criteria

- [ ] `Camera` class created with properties:
  - Position (x, y) in world coordinates
  - Zoom level (0.5x to 2.0x range)
  - Viewport bounds (visible area in world coordinates)
  - Follow target (optional entity to follow)

- [ ] Camera operations implemented:
  - `setPosition(x, y)` - Move camera
  - `setZoom(level)` - Set zoom level
  - `followEntity(entity)` - Track entity movement
  - `pan(dx, dy)` - Relative movement
  - `getWorldBounds()` - Calculate visible world area
  - `screenToWorld(screenX, screenY)` - Convert coordinates
  - `worldToScreen(worldX, worldY)` - Convert coordinates

- [ ] Camera smoothing/easing:
  - Lerp to target position over time (smooth follow)
  - Configurable follow speed
  - Zoom transitions smoothed

- [ ] Camera constraints:
  - Min/max zoom levels enforced
  - Optional world bounds clamping
  - Dead zone for follow target (prevents micro-jitters)

- [ ] Input handling for camera:
  - Mouse wheel zoom
  - Middle mouse drag to pan
  - Arrow keys for panning
  - Double-click to recenter on party

- [ ] Camera state integrated with renderer:
  - All layers use camera transform
  - Culling based on camera bounds (don't render off-screen)

- [ ] Tests verify:
  - Coordinate conversions are accurate
  - Zoom affects visible area correctly
  - Follow tracks target smoothly
  - Constraints are enforced

### Technical Notes

```typescript
// game/renderer/Camera.ts
export class Camera {
  private x: number = 0;
  private y: number = 0;
  private zoom: number = 1.0;
  private targetX: number = 0;
  private targetY: number = 0;
  private followSpeed: number = 0.1;
  private viewportWidth: number;
  private viewportHeight: number;

  constructor(viewportWidth: number, viewportHeight: number) {
    this.viewportWidth = viewportWidth;
    this.viewportHeight = viewportHeight;
  }

  update(deltaTime: number) {
    // Smooth camera movement (lerp)
    this.x += (this.targetX - this.x) * this.followSpeed * deltaTime;
    this.y += (this.targetY - this.y) * this.followSpeed * deltaTime;
  }

  setPosition(x: number, y: number) {
    this.targetX = x;
    this.targetY = y;
  }

  setZoom(level: number) {
    this.zoom = Math.max(0.5, Math.min(2.0, level));
  }

  getWorldBounds(): { x: number; y: number; width: number; height: number } {
    const width = this.viewportWidth / this.zoom;
    const height = this.viewportHeight / this.zoom;
    return {
      x: this.x - width / 2,
      y: this.y - height / 2,
      width,
      height,
    };
  }

  screenToWorld(screenX: number, screenY: number): { x: number; y: number } {
    const bounds = this.getWorldBounds();
    return {
      x: bounds.x + (screenX / this.viewportWidth) * bounds.width,
      y: bounds.y + (screenY / this.viewportHeight) * bounds.height,
    };
  }

  worldToScreen(worldX: number, worldY: number): { x: number; y: number } {
    const bounds = this.getWorldBounds();
    return {
      x: ((worldX - bounds.x) / bounds.width) * this.viewportWidth,
      y: ((worldY - bounds.y) / bounds.height) * this.viewportHeight,
    };
  }

  applyToContainer(container: PIXI.Container) {
    container.position.set(this.viewportWidth / 2, this.viewportHeight / 2);
    container.scale.set(this.zoom, this.zoom);
    container.pivot.set(this.x, this.y);
  }
}

// game/input/InputManager.ts
export class InputManager {
  private camera: Camera;
  private canvas: HTMLElement;

  constructor(camera: Camera, canvas: HTMLElement) {
    this.camera = camera;
    this.canvas = canvas;
    this.setupEventListeners();
  }

  private setupEventListeners() {
    // Mouse wheel zoom
    this.canvas.addEventListener('wheel', (e) => {
      e.preventDefault();
      const zoomDelta = e.deltaY > 0 ? 0.9 : 1.1;
      this.camera.setZoom(this.camera.getZoom() * zoomDelta);
    });

    // Middle mouse drag
    let isDragging = false;
    let lastX = 0;
    let lastY = 0;

    this.canvas.addEventListener('mousedown', (e) => {
      if (e.button === 1) { // Middle button
        isDragging = true;
        lastX = e.clientX;
        lastY = e.clientY;
        e.preventDefault();
      }
    });

    this.canvas.addEventListener('mousemove', (e) => {
      if (isDragging) {
        const dx = e.clientX - lastX;
        const dy = e.clientY - lastY;
        this.camera.pan(-dx / this.camera.getZoom(), -dy / this.camera.getZoom());
        lastX = e.clientX;
        lastY = e.clientY;
      }
    });

    this.canvas.addEventListener('mouseup', (e) => {
      if (e.button === 1) {
        isDragging = false;
      }
    });

    // Arrow key panning
    window.addEventListener('keydown', (e) => {
      const panSpeed = 10;
      switch (e.key) {
        case 'ArrowLeft':
          this.camera.pan(-panSpeed, 0);
          break;
        case 'ArrowRight':
          this.camera.pan(panSpeed, 0);
          break;
        case 'ArrowUp':
          this.camera.pan(0, -panSpeed);
          break;
        case 'ArrowDown':
          this.camera.pan(0, panSpeed);
          break;
      }
    });
  }
}
```

**Effort**: 5 points  
**Dependencies**: Story 3  
**Priority**: P0 (blocking)

---

## Story 5: Sprite Asset Loading and Management

**As a** game developer  
**I want** an asset loading system for sprites and textures  
**So that** game assets are loaded efficiently and available for rendering

### Acceptance Criteria

- [ ] `AssetLoader` service created with:
  - `loadTexture(path): Promise<PIXI.Texture>` - Load single texture
  - `loadSpriteSheet(atlasPath): Promise<PIXI.Spritesheet>` - Load texture atlas
  - `loadAll(manifest): Promise<void>` - Batch load from manifest
  - `getTexture(name): PIXI.Texture` - Retrieve loaded texture
  - Progress tracking for loading screen

- [ ] Asset manifest defined (JSON or TypeScript):
  - List of textures to load
  - Sprite sheet definitions
  - Organized by category (terrain, characters, ui, effects)

- [ ] `SpriteManager` class created for:
  - Sprite creation from textures
  - Sprite pooling (reuse sprites for performance)
  - Animated sprite management
  - Sprite disposal

- [ ] Loading screen component created:
  - Shows loading progress
  - Displays percentage and current asset
  - Branded with game aesthetic (rift energy loading bar)

- [ ] Error handling for:
  - Missing assets
  - Failed loads (retry logic)
  - Fallback placeholder sprites

- [ ] Development mode features:
  - Hot reload for assets (when possible)
  - Asset debugging panel showing loaded textures

- [ ] Tests verify:
  - Assets load successfully
  - Progress updates correctly
  - Sprite creation works
  - Pooling reuses sprites
  - Errors handled gracefully

### Technical Notes

```typescript
// game/assets/AssetLoader.ts
export class AssetLoader {
  private textures: Map<string, PIXI.Texture> = new Map();
  private loadingProgress: number = 0;
  private onProgressCallback?: (progress: number) => void;

  async loadAll(manifest: AssetManifest): Promise<void> {
    const totalAssets = manifest.textures.length + manifest.spritesheets.length;
    let loadedAssets = 0;

    // Load individual textures
    for (const texture of manifest.textures) {
      try {
        const loaded = await PIXI.Assets.load(texture.path);
        this.textures.set(texture.name, loaded);
      } catch (error) {
        console.error(`Failed to load texture: ${texture.name}`, error);
        // Use fallback
        this.textures.set(texture.name, this.createFallbackTexture());
      }
      
      loadedAssets++;
      this.updateProgress(loadedAssets / totalAssets);
    }

    // Load sprite sheets
    for (const sheet of manifest.spritesheets) {
      try {
        const loaded = await PIXI.Assets.load(sheet.atlasPath);
        // Store each texture from atlas
        Object.entries(loaded.textures).forEach(([name, texture]) => {
          this.textures.set(`${sheet.name}:${name}`, texture as PIXI.Texture);
        });
      } catch (error) {
        console.error(`Failed to load spritesheet: ${sheet.name}`, error);
      }
      
      loadedAssets++;
      this.updateProgress(loadedAssets / totalAssets);
    }
  }

  getTexture(name: string): PIXI.Texture {
    const texture = this.textures.get(name);
    if (!texture) {
      console.warn(`Texture not found: ${name}`);
      return this.createFallbackTexture();
    }
    return texture;
  }

  private createFallbackTexture(): PIXI.Texture {
    // Create a simple colored rectangle as fallback
    const graphics = new PIXI.Graphics();
    graphics.beginFill(0xff00ff); // Magenta for visibility
    graphics.drawRect(0, 0, 32, 32);
    graphics.endFill();
    return this.app.renderer.generateTexture(graphics);
  }

  onProgress(callback: (progress: number) => void) {
    this.onProgressCallback = callback;
  }

  private updateProgress(progress: number) {
    this.loadingProgress = progress;
    if (this.onProgressCallback) {
      this.onProgressCallback(progress);
    }
  }
}

// game/assets/SpriteManager.ts
export class SpriteManager {
  private assetLoader: AssetLoader;
  private spritePool: Map<string, PIXI.Sprite[]> = new Map();

  constructor(assetLoader: AssetLoader) {
    this.assetLoader = assetLoader;
  }

  createSprite(textureName: string): PIXI.Sprite {
    // Check pool first
    const pooled = this.getFromPool(textureName);
    if (pooled) {
      pooled.visible = true;
      return pooled;
    }

    // Create new sprite
    const texture = this.assetLoader.getTexture(textureName);
    const sprite = new PIXI.Sprite(texture);
    sprite.anchor.set(0.5, 0.5); // Center anchor by default
    return sprite;
  }

  releaseSprite(sprite: PIXI.Sprite, textureName: string) {
    sprite.visible = false;
    sprite.parent?.removeChild(sprite);
    
    // Return to pool
    if (!this.spritePool.has(textureName)) {
      this.spritePool.set(textureName, []);
    }
    this.spritePool.get(textureName)!.push(sprite);
  }

  private getFromPool(textureName: string): PIXI.Sprite | null {
    const pool = this.spritePool.get(textureName);
    return pool?.pop() || null;
  }
}

// assets/manifest.ts
export interface AssetManifest {
  textures: Array<{ name: string; path: string }>;
  spritesheets: Array<{ name: string; atlasPath: string }>;
}

export const assetManifest: AssetManifest = {
  textures: [
    { name: 'terrain_grass', path: '/assets/terrain/grass.png' },
    { name: 'terrain_dirt', path: '/assets/terrain/dirt.png' },
    { name: 'terrain_stone', path: '/assets/terrain/stone.png' },
  ],
  spritesheets: [
    { name: 'characters', atlasPath: '/assets/spritesheets/characters.json' },
    { name: 'ui', atlasPath: '/assets/spritesheets/ui.json' },
  ],
};
```

**Effort**: 5 points  
**Dependencies**: Story 3  
**Priority**: P0 (blocking)

---

## Story 6: Tile-Based World Rendering

**As a** player  
**I want** to see the game world rendered as a tile-based grid  
**So that** I can navigate and understand the environment

### Acceptance Criteria

- [ ] `TileMap` class created with:
  - 2D grid of tile data (type, variant, properties)
  - Grid dimensions (width, height in tiles)
  - Tile size constant (32x32 pixels recommended)
  - Methods to get/set tile at coordinate

- [ ] `TileRenderer` service created that:
  - Renders visible tiles based on camera viewport
  - Uses sprite batching for performance
  - Culls off-screen tiles
  - Supports multiple terrain types (grass, dirt, stone, water, etc.)
  - Handles tile variants for visual variety

- [ ] Tile rendering optimizations:
  - Only render tiles in camera bounds
  - Batch sprites by texture (minimize draw calls)
  - Use sprite pool for tiles
  - Static tiles rendered to RenderTexture (cache layer)

- [ ] Grid overlay feature (toggle-able in dev mode):
  - Shows tile boundaries
  - Shows grid coordinates
  - Rift energy color (#4da6ff, low opacity)

- [ ] Multi-layer tile support:
  - Base terrain layer
  - Objects/decorations layer
  - Optional overlay layer (hazards, effects)

- [ ] Integration with Redux world state:
  - Reads zone data from `worldSlice`
  - Updates when zone changes
  - Handles fog of war (unexplored tiles grayed)

- [ ] Tests verify:
  - Tiles render at correct positions
  - Culling works (tiles outside viewport not rendered)
  - Tile changes update visually
  - Performance acceptable (60 FPS with 100x100 tiles visible)

### Technical Notes

```typescript
// game/renderer/TileMap.ts
export enum TerrainType {
  GRASS = 'grass',
  DIRT = 'dirt',
  STONE = 'stone',
  WATER = 'water',
  RUBBLE = 'rubble',
}

export interface Tile {
  terrain: TerrainType;
  variant: number; // 0-3 for visual variety
  discovered: boolean;
}

export class TileMap {
  private tiles: Tile[][];
  readonly width: number;
  readonly height: number;
  static readonly TILE_SIZE = 32;

  constructor(width: number, height: number) {
    this.width = width;
    this.height = height;
    this.tiles = this.initializeTiles(width, height);
  }

  private initializeTiles(width: number, height: number): Tile[][] {
    const tiles: Tile[][] = [];
    for (let y = 0; y < height; y++) {
      tiles[y] = [];
      for (let x = 0; x < width; x++) {
        tiles[y][x] = {
          terrain: TerrainType.GRASS,
          variant: Math.floor(Math.random() * 4),
          discovered: false,
        };
      }
    }
    return tiles;
  }

  getTile(x: number, y: number): Tile | null {
    if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
      return null;
    }
    return this.tiles[y][x];
  }

  setTile(x: number, y: number, tile: Tile): void {
    if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
      this.tiles[y][x] = tile;
    }
  }
}

// game/renderer/TileRenderer.ts
export class TileRenderer {
  private tileMap: TileMap;
  private spriteManager: SpriteManager;
  private container: PIXI.Container;
  private tileSprites: Map<string, PIXI.Sprite> = new Map();

  constructor(
    tileMap: TileMap,
    spriteManager: SpriteManager,
    container: PIXI.Container
  ) {
    this.tileMap = tileMap;
    this.spriteManager = spriteManager;
    this.container = container;
  }

  render(cameraBounds: { x: number; y: number; width: number; height: number }) {
    // Calculate visible tile range
    const startX = Math.floor(cameraBounds.x / TileMap.TILE_SIZE);
    const startY = Math.floor(cameraBounds.y / TileMap.TILE_SIZE);
    const endX = Math.ceil((cameraBounds.x + cameraBounds.width) / TileMap.TILE_SIZE);
    const endY = Math.ceil((cameraBounds.y + cameraBounds.height) / TileMap.TILE_SIZE);

    // Track which tiles should be visible
    const visibleTiles = new Set<string>();

    // Render visible tiles
    for (let y = startY; y <= endY; y++) {
      for (let x = startX; x <= endX; x++) {
        const tile = this.tileMap.getTile(x, y);
        if (!tile) continue;

        const key = `${x},${y}`;
        visibleTiles.add(key);

        // Get or create sprite for this tile
        let sprite = this.tileSprites.get(key);
        if (!sprite) {
          const textureName = `terrain_${tile.terrain}_${tile.variant}`;
          sprite = this.spriteManager.createSprite(textureName);
          sprite.position.set(
            x * TileMap.TILE_SIZE,
            y * TileMap.TILE_SIZE
          );
          this.container.addChild(sprite);
          this.tileSprites.set(key, sprite);
        }

        // Apply fog of war
        if (!tile.discovered) {
          sprite.tint = 0x333333; // Darkened
          sprite.alpha = 0.5;
        } else {
          sprite.tint = 0xffffff;
          sprite.alpha = 1.0;
        }

        sprite.visible = true;
      }
    }

    // Hide sprites outside viewport
    for (const [key, sprite] of this.tileSprites) {
      if (!visibleTiles.has(key)) {
        sprite.visible = false;
      }
    }
  }

  updateFromZoneData(zoneData: ZoneData) {
    // Update tile map from backend zone data
    for (let y = 0; y < zoneData.height; y++) {
      for (let x = 0; x < zoneData.width; x++) {
        const tile = zoneData.tiles[y][x];
        this.tileMap.setTile(x, y, {
          terrain: tile.terrain,
          variant: tile.variant,
          discovered: tile.discovered,
        });
      }
    }
    // Force re-render on next frame
  }
}
```

**Effort**: 8 points  
**Dependencies**: Stories 3, 4, 5  
**Priority**: P0 (blocking)

---

## Story 7: UI Component Library Foundation

**As a** UI developer  
**I want** a library of styled components following the style guide  
**So that** I can build consistent interfaces efficiently

### Acceptance Criteria

- [ ] Style system configured with design tokens:
  - Colors from style guide as CSS custom properties
  - Typography scale defined
  - Spacing scale (8px base unit)
  - Breakpoints for responsive design

- [ ] Core components created following style guide:
  - `Button` (primary, danger, ghost variants)
  - `Input` (text, number, with error states)
  - `Panel` (container with header/body/footer)
  - `ProgressBar` (resource bars, loading)
  - `Tooltip` (hover info)
  - `Modal` (dialog overlay)
  - `Tabs` (navigation within panels)
  - `Badge` (counts, status indicators)

- [ ] Each component includes:
  - TypeScript type definitions
  - Props for variants/states
  - Accessibility attributes (ARIA)
  - Focus states with rift glow
  - Hover/active states
  - Storybook stories (if using Storybook)

- [ ] Effects library created:
  - Rift glow CSS mixins
  - Scan line effect
  - Corner bracket decorations
  - Pulse animations

- [ ] Icon system established:
  - SVG icon components
  - Consistent sizing (24x24 base)
  - Stroke-based style (2px weight)
  - Rift glow on interactive icons

- [ ] Layout components:
  - `Grid` - 12 column grid system
  - `Stack` - Vertical/horizontal spacing
  - `Container` - Max width content container

- [ ] Tests verify:
  - Components render correctly
  - Variants apply correct styles
  - Accessibility features work
  - Events fire correctly

### Technical Notes

```typescript
// components/ui/Button.tsx
import React from 'react';
import styled from 'styled-components';

interface ButtonProps {
  variant?: 'primary' | 'danger' | 'ghost';
  onClick?: () => void;
  disabled?: boolean;
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  onClick,
  disabled = false,
  children,
}) => {
  return (
    <StyledButton
      variant={variant}
      onClick={onClick}
      disabled={disabled}
      aria-disabled={disabled}
    >
      {children}
    </StyledButton>
  );
};

const StyledButton = styled.button<{ variant: string }>`
  background: linear-gradient(135deg, #2d3542 0%, #1a1f28 100%);
  border: 1px solid #555d6d;
  color: #e3e8ef;
  padding: 12px 24px;
  border-radius: 2px;
  font-family: 'IBM Plex Mono', monospace;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.05);

  &:hover:not(:disabled) {
    border-color: var(--color-rift-stable);
    box-shadow: 0 0 12px rgba(77, 166, 255, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.05);
  }

  &:active:not(:disabled) {
    background: linear-gradient(135deg, #1a1f28 0%, #0a0e14 100%);
    border-color: var(--color-rift-active);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    border-color: #3d4451;
    color: #555d6d;
  }

  ${(props) =>
    props.variant === 'danger' &&
    `
    border-color: var(--color-rift-volatile);
    
    &:hover:not(:disabled) {
      box-shadow: 0 0 12px rgba(255, 107, 107, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.05);
    }
  `}

  ${(props) =>
    props.variant === 'ghost' &&
    `
    background: transparent;
    border-color: #3d4451;
    color: #9ba3b4;
    
    &:hover:not(:disabled) {
      border-color: var(--color-rift-stable);
      color: #e3e8ef;
    }
  `}
`;

// styles/tokens.css
:root {
  /* Colors */
  --color-void: #0a0e14;
  --color-concrete: #1a1f28;
  --color-steel: #2d3542;
  --color-rust: #3d4451;
  --color-ash: #555d6d;
  --color-dust: #7a8291;
  --color-smoke: #9ba3b4;
  --color-paper: #c5cdd8;
  --color-bleached: #e3e8ef;

  --color-rift-stable: #4da6ff;
  --color-rift-resonant: #66b3ff;
  --color-rift-active: #80c0ff;
  --color-rift-volatile: #ff6b6b;
  --color-rift-arcane: #d946ff;
  --color-rift-healing: #4dffb8;
  --color-rift-temporal: #ffdb4d;

  /* Spacing */
  --space-xs: 4px;
  --space-s: 8px;
  --space-m: 16px;
  --space-l: 24px;
  --space-xl: 32px;
  --space-xxl: 48px;

  /* Typography */
  --font-primary: 'IBM Plex Mono', monospace;
  --font-display: 'Rajdhani', sans-serif;

  /* Effects */
  --glow-rift: 0 0 12px rgba(77, 166, 255, 0.3);
  --shadow-panel: 0 4px 12px rgba(0, 0, 0, 0.5);
}

// components/ui/Panel.tsx
export const Panel = styled.div`
  background: rgba(26, 31, 40, 0.95);
  border: 1px solid var(--color-rust);
  backdrop-filter: blur(8px);
  box-shadow: var(--shadow-panel);
`;

export const PanelHeader = styled.div`
  background: var(--color-concrete);
  border-bottom: 1px solid var(--color-rift-stable);
  padding: var(--space-m) var(--space-l);
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 600;
  color: var(--color-bleached);
`;

export const PanelBody = styled.div`
  padding: var(--space-l);
`;
```

**Effort**: 8 points  
**Dependencies**: Story 1  
**Priority**: P0 (blocking)

---

## Story 8: Main Game Layout and HUD

**As a** player  
**I want** a clear game layout with HUD elements  
**So that** I can access game information and controls

### Acceptance Criteria

- [ ] Main game view layout created with:
  - Full-screen canvas for game world (left/center)
  - Right sidebar for party/character info (300px width, collapsible)
  - Bottom bar for notifications and messages (60px height)
  - Top bar for resources and time (optional, 40px height)

- [ ] Layout is responsive:
  - Adjusts to different screen sizes
  - Panels can be collapsed/expanded
  - Minimum resolution supported: 1280x720

- [ ] HUD components created:
  - `PartyPanel` - Shows party members, health bars, status
  - `NotificationBar` - Scrolling log of events
  - `ResourceBar` - Credits, time of day
  - `ActionBar` - Context-sensitive actions (bottom-right)

- [ ] Party panel features:
  - Character portraits (placeholder for now)
  - Health/stamina bars with rift energy styling
  - Status effect icons
  - Click character to view details
  - Highlights selected character

- [ ] Notification system:
  - Messages slide in from bottom
  - Auto-dismiss after 5 seconds
  - Different colors for different event types
  - Click to expand/dismiss
  - Persists recent 20 messages

- [ ] Action bar features:
  - Shows available actions based on context
  - Buttons with keyboard shortcuts
  - Tooltips on hover
  - Disables unavailable actions

- [ ] Layout persists across routes (stays visible during gameplay)

- [ ] Tests verify:
  - Layout renders correctly
  - Panels can collapse/expand
  - Character selection works
  - Notifications appear and dismiss

### Technical Notes

```typescript
// components/game/GameView.tsx
export const GameView: React.FC = () => {
  const [rightPanelOpen, setRightPanelOpen] = useState(true);

  return (
    <GameLayout>
      <TopBar>
        <ResourceBar />
      </TopBar>

      <MainContent>
        <GameCanvasContainer>
          <GameCanvas />
          <ActionBar />
        </GameCanvasContainer>

        {rightPanelOpen && (
          <RightSidebar>
            <PartyPanel />
          </RightSidebar>
        )}
      </MainContent>

      <BottomBar>
        <NotificationBar />
      </BottomBar>

      <PanelToggle onClick={() => setRightPanelOpen(!rightPanelOpen)}>
        {rightPanelOpen ? '»' : '«'}
      </PanelToggle>
    </GameLayout>
  );
};

const GameLayout = styled.div`
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  background: var(--color-void);
  overflow: hidden;
`;

const TopBar = styled.div`
  height: 40px;
  border-bottom: 1px solid var(--color-rust);
  display: flex;
  align-items: center;
  padding: 0 var(--space-m);
`;

const MainContent = styled.div`
  flex: 1;
  display: flex;
  overflow: hidden;
`;

const GameCanvasContainer = styled.div`
  flex: 1;
  position: relative;
`;

const RightSidebar = styled.div`
  width: 300px;
  border-left: 1px solid var(--color-rust);
  background: rgba(26, 31, 40, 0.95);
  backdrop-filter: blur(8px);
  overflow-y: auto;
`;

const BottomBar = styled.div`
  height: 60px;
  border-top: 1px solid var(--color-rust);
  background: rgba(26, 31, 40, 0.95);
`;

// components/game/PartyPanel.tsx
export const PartyPanel: React.FC = () => {
  const party = useAppSelector((state) => state.party);
  const selectedCharacterId = useAppSelector((state) => state.ui.selectedCharacter);
  const dispatch = useAppDispatch();

  return (
    <Panel>
      <PanelHeader>Party</PanelHeader>
      <PanelBody>
        {party.members.map((character) => (
          <CharacterCard
            key={character.id}
            character={character}
            selected={character.id === selectedCharacterId}
            onClick={() => dispatch(selectCharacter(character.id))}
          />
        ))}
      </PanelBody>
    </Panel>
  );
};

const CharacterCard = styled.div<{ selected: boolean }>`
  padding: var(--space-m);
  margin-bottom: var(--space-s);
  border: 1px solid var(--color-rust);
  cursor: pointer;
  transition: all 0.2s ease;

  ${(props) =>
    props.selected &&
    `
    border-color: var(--color-rift-stable);
    background: rgba(77, 166, 255, 0.1);
    box-shadow: var(--glow-rift);
  `}

  &:hover {
    border-color: var(--color-rift-stable);
  }
`;
```

**Effort**: 8 points  
**Dependencies**: Stories 1, 7  
**Priority**: P0 (blocking)

---

## Story 9: Redux-to-Renderer State Sync

**As a** developer  
**I want** Redux state changes to automatically update the renderer  
**So that** the visual representation stays in sync with game state

### Acceptance Criteria

- [ ] `useGameRenderer` custom hook created that:
  - Initializes renderer on mount
  - Subscribes to Redux state changes
  - Updates renderer when relevant state changes
  - Cleans up on unmount

- [ ] State sync implemented for:
  - World/zone changes → Update tile map
  - Party movement → Update party position sprite
  - Character status → Update character sprites (health, effects)
  - Combat state → Update combat entities and UI

- [ ] Render update strategy:
  - Selective updates (only changed entities)
  - Debounced updates for frequent changes
  - Frame-rate independent updates

- [ ] Entity position interpolation:
  - Smooth movement between positions
  - Configurable animation speed
  - Easing functions applied

- [ ] State mapping utilities:
  - `mapZoneDataToTileMap` - Convert backend zone to tiles
  - `mapCharacterToSprite` - Create sprite from character data
  - `updateSpriteFromCharacter` - Sync sprite with character state

- [ ] Performance optimizations:
  - Don't re-render unchanged entities
  - Batch updates when possible
  - Use object pooling for temporary entities

- [ ] Tests verify:
  - State changes trigger renders
  - Entities update correctly
  - Performance is acceptable (60 FPS maintained)
  - No memory leaks on state changes

### Technical Notes

```typescript
// hooks/useGameRenderer.ts
export const useGameRenderer = (
  canvasRef: React.RefObject<HTMLDivElement>,
  worldRenderer: WorldRenderer | null
) => {
  const party = useAppSelector((state) => state.party);
  const world = useAppSelector((state) => state.world);
  const combat = useAppSelector((state) => state.combat);

  // Update party position when it changes
  useEffect(() => {
    if (!worldRenderer || !party.position) return;

    worldRenderer.updatePartyPosition(party.position);
  }, [worldRenderer, party.position]);

  // Update zone tiles when zone changes
  useEffect(() => {
    if (!worldRenderer || !world.currentZone) return;

    const tileMap = mapZoneDataToTileMap(world.currentZone);
    worldRenderer.updateTileMap(tileMap);
  }, [worldRenderer, world.currentZone]);

  // Update character sprites when characters change
  useEffect(() => {
    if (!worldRenderer || !party.members) return;

    party.members.forEach((character) => {
      worldRenderer.updateCharacterSprite(character);
    });
  }, [worldRenderer, party.members]);

  // Update combat entities
  useEffect(() => {
    if (!worldRenderer || combat.status !== 'active') return;

    worldRenderer.updateCombatState(combat);
  }, [worldRenderer, combat]);
};

// game/renderer/WorldRenderer.ts (additions)
export class WorldRenderer {
  // ... existing code

  private partySprite: PIXI.Sprite | null = null;
  private characterSprites: Map<string, PIXI.Sprite> = new Map();
  private animationQueue: Array<{ sprite: PIXI.Sprite; target: Position }> = [];

  updatePartyPosition(position: WorldPosition) {
    if (!this.partySprite) {
      this.partySprite = this.spriteManager.createSprite('party_icon');
      this.layers.entities.addChild(this.partySprite);
    }

    // Animate to new position
    const targetX = position.gridX * TileMap.TILE_SIZE;
    const targetY = position.gridY * TileMap.TILE_SIZE;
    
    this.animationQueue.push({
      sprite: this.partySprite,
      target: { x: targetX, y: targetY },
    });
  }

  updateCharacterSprite(character: Character) {
    let sprite = this.characterSprites.get(character.id);
    
    if (!sprite) {
      // Create new sprite for character
      sprite = this.spriteManager.createSprite(`character_${character.appearance}`);
      this.layers.entities.addChild(sprite);
      this.characterSprites.set(character.id, sprite);
    }

    // Update sprite properties based on character state
    sprite.tint = character.status.dead 
      ? 0x666666 
      : character.status.incapacitated 
      ? 0xaa6666 
      : 0xffffff;

    // Position based on character position or party formation
    // ...
  }

  private update(deltaTime: number) {
    // Process animation queue
    this.animationQueue = this.animationQueue.filter((anim) => {
      const dx = anim.target.x - anim.sprite.x;
      const dy = anim.target.y - anim.sprite.y;
      const distance = Math.sqrt(dx * dx + dy * dy);

      if (distance < 1) {
        anim.sprite.position.set(anim.target.x, anim.target.y);
        return false; // Remove from queue
      }

      // Interpolate position
      const speed = 200; // pixels per second
      const step = (speed * deltaTime) / 60;
      const ratio = Math.min(step / distance, 1);
      
      anim.sprite.x += dx * ratio;
      anim.sprite.y += dy * ratio;

      return true; // Keep in queue
    });

    // Apply camera transform
    this.camera.update(deltaTime);
    this.camera.applyToContainer(this.app.stage);
  }
}

// utils/mappers.ts
export function mapZoneDataToTileMap(zoneData: ZoneData): TileMap {
  const tileMap = new TileMap(zoneData.width, zoneData.height);
  
  for (let y = 0; y < zoneData.height; y++) {
    for (let x = 0; x < zoneData.width; x++) {
      const backendTile = zoneData.tiles[y][x];
      tileMap.setTile(x, y, {
        terrain: backendTile.terrain as TerrainType,
        variant: backendTile.variant,
        discovered: backendTile.discovered,
      });
    }
  }
  
  return tileMap;
}
```

**Effort**: 5 points  
**Dependencies**: Stories 1, 3, 6  
**Priority**: P0 (blocking)

---

## Epic Summary

### Total Effort: 52 points

### Critical Path (P0 - MVP Blocking):
All 9 stories are P0 for MVP - you cannot show the game without rendering.

### Story Dependency Graph:
```
Story 1 (Redux Setup)
   ├─► Story 2 (API Client)
   ├─► Story 3 (PixiJS Init)
   │      ├─► Story 4 (Camera)
   │      ├─► Story 5 (Asset Loading)
   │      │      └─► Story 6 (Tile Rendering)
   │      │              └─► Story 9 (State Sync)
   │      └─► Story 9 (State Sync)
   └─► Story 7 (UI Components)
          └─► Story 8 (Layout/HUD)
```

---

## Implementation Order Recommendation

**Sprint 1** (Foundation - 8 points):
- Story 1: Redux Setup (3 pts)
- Story 2: API Client (5 pts)

**Sprint 2** (Rendering Engine - 15 points):
- Story 3: PixiJS Init (5 pts)
- Story 4: Camera (5 pts)
- Story 5: Asset Loading (5 pts)

**Sprint 3** (World Rendering - 13 points):
- Story 6: Tile Rendering (8 pts)
- Story 9: State Sync (5 pts)

**Sprint 4** (UI - 16 points):
- Story 7: Component Library (8 pts)
- Story 8: Layout/HUD (8 pts)

---

## Testing Strategy

1. **Visual Regression Tests**: Screenshot comparisons for UI components
2. **Performance Tests**: FPS monitoring, render time measurements
3. **Integration Tests**: Redux → Renderer pipeline end-to-end
4. **Manual Testing**: Visual inspection of animations, effects

## Performance Targets

| Metric | Target |
|--------|--------|
| FPS (60 Hz display) | 60 FPS sustained |
| Frame time | < 16.67ms |
| Initial load time | < 3 seconds |
| Asset load time | < 5 seconds |
| State update → Visual | < 100ms |
| Memory usage | < 500MB |

## Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| PixiJS performance issues with many sprites | Medium | High | Sprite batching, culling, pooling (Story 6) |
| Redux state updates cause stuttering | Medium | High | Selective updates, debouncing (Story 9) |
| Asset loading too slow | Low | Medium | Sprite sheets, progressive loading (Story 5) |
| Browser compatibility issues | Low | Medium | Target modern browsers, test on Chrome/Firefox/Safari |
| Memory leaks from sprite management | Medium | High | Proper cleanup, pooling, profiling |
