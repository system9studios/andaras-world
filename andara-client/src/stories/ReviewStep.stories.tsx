import type { Meta, StoryObj } from '@storybook/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { ReviewStep } from '../components/character-creation/ReviewStep';
import characterCreationReducer from '../store/slices/characterCreationSlice';
import gameReducer from '../store/slices/gameSlice';
import partyReducer from '../store/slices/partySlice';
import worldReducer from '../store/slices/worldSlice';
import combatReducer from '../store/slices/combatSlice';
import uiReducer from '../store/slices/uiSlice';

const mockStore = configureStore({
  reducer: {
    game: gameReducer,
    party: partyReducer,
    world: worldReducer,
    combat: combatReducer,
    ui: uiReducer,
    characterCreation: characterCreationReducer,
  },
  preloadedState: {
    characterCreation: {
      currentStep: 'review',
      formData: {
        name: 'Test Character',
        origin: 'VAULT_DWELLER',
        attributes: {
          strength: 10,
          agility: 9,
          endurance: 8,
          intellect: 8,
          perception: 8,
          charisma: 8,
        },
        skillFocuses: ['mechanics', 'electronics'],
        appearance: {
          gender: 'NON_BINARY' as const,
          bodyType: 'AVERAGE' as const,
        },
        isProtagonist: true,
      },
      availableOrigins: [],
      availableSkills: [],
      validationErrors: {},
      isSubmitting: false,
      createdCharacterId: null,
    },
  },
});

const meta: Meta<typeof ReviewStep> = {
  title: 'Character Creation/Review',
  component: ReviewStep,
  decorators: [
    (Story) => (
      <Provider store={mockStore}>
        <Story />
      </Provider>
    ),
  ],
  parameters: {
    reactRouter: {
      routePath: '/character-creation',
    },
  },
};

export default meta;
type Story = StoryObj<typeof ReviewStep>;

export const Default: Story = {};

