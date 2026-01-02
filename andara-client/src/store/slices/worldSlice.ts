import { createSlice } from '@reduxjs/toolkit';

interface WorldState {
  currentRegion: unknown | null;
  zones: Record<string, unknown>;
  discoveredPOIs: Record<string, unknown>;
  visibilityMap: Record<string, string>;
}

const initialState: WorldState = {
  currentRegion: null,
  zones: {},
  discoveredPOIs: {},
  visibilityMap: {},
};

const worldSlice = createSlice({
  name: 'world',
  initialState,
  reducers: {
    // Reducers will be added as needed
  },
});

export default worldSlice.reducer;

