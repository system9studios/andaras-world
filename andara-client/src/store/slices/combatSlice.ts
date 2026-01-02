import { createSlice } from '@reduxjs/toolkit';

interface CombatState {
  encounterId: string | null;
  status: 'idle' | 'active' | 'resolved';
  turnNumber: number;
  currentTurn: string | null;
  combatants: Record<string, unknown>;
  battlefield: unknown;
  availableActions: unknown[];
  actionHistory: unknown[];
}

const initialState: CombatState = {
  encounterId: null,
  status: 'idle',
  turnNumber: 0,
  currentTurn: null,
  combatants: {},
  battlefield: null,
  availableActions: [],
  actionHistory: [],
};

const combatSlice = createSlice({
  name: 'combat',
  initialState,
  reducers: {
    // Reducers will be added as needed
  },
});

export default combatSlice.reducer;

