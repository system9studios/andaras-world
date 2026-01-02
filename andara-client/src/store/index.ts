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
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

