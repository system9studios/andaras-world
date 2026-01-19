import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ContentEditor } from '../ContentEditor';

// Mock the useSchema hook
vi.mock('../hooks/useSchemas', () => ({
  useSchema: vi.fn(() => ({
    schema: {
      type: 'object',
      required: ['templateId', 'name', 'category', 'baseValue', 'weight'],
      properties: {
        templateId: { type: 'string' },
        name: { type: 'string' },
        category: {
          type: 'string',
          enum: ['weapon', 'armor', 'consumable', 'resource', 'artifact', 'tool']
        },
        baseValue: { type: 'integer', minimum: 0 },
        weight: { type: 'number', minimum: 0 },
        description: { type: 'string' }
      }
    },
    loading: false,
    error: null
  }))
}));

describe('ContentEditor', () => {
  const mockOnSave = vi.fn();
  const mockOnCancel = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render schema-driven form by default', async () => {
    render(
      <ContentEditor
        contentType="ITEM_TEMPLATE"
        onSave={mockOnSave}
        onCancel={mockOnCancel}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Edit ITEM TEMPLATE/i)).toBeInTheDocument();
    });

    // Should show form mode by default
    expect(screen.getByRole('button', { name: /JSON Mode/i })).toBeInTheDocument();
  });

  it('should toggle between form mode and JSON mode', async () => {
    const user = userEvent.setup();
    
    render(
      <ContentEditor
        contentType="ITEM_TEMPLATE"
        onSave={mockOnSave}
        onCancel={mockOnCancel}
      />
    );

    // Initially in form mode
    const toggleButton = await screen.findByRole('button', { name: /JSON Mode/i });
    
    // Switch to JSON mode
    await user.click(toggleButton);
    
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Form Mode/i })).toBeInTheDocument();
    });

    // Should show textarea in JSON mode
    expect(screen.getByRole('textbox')).toBeInTheDocument();
  });

  it('should initialize with provided initial content', async () => {
    const initialContent = {
      templateId: 'item_test_sword',
      name: 'Test Sword',
      category: 'weapon',
      baseValue: 100,
      weight: 5.0
    };

    render(
      <ContentEditor
        contentType="ITEM_TEMPLATE"
        initialContent={initialContent}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Edit ITEM TEMPLATE/i)).toBeInTheDocument();
    });

    // Switch to JSON mode to verify initial data
    const toggleButton = await screen.findByRole('button', { name: /JSON Mode/i });
    await userEvent.setup().click(toggleButton);

    const textarea = screen.getByRole('textbox') as HTMLTextAreaElement;
    expect(textarea.value).toContain('Test Sword');
    expect(textarea.value).toContain('item_test_sword');
  });

  it('should call onSave with form data when save is clicked', async () => {
    const user = userEvent.setup();
    mockOnSave.mockResolvedValue(undefined);

    // Provide valid initial content so form validation doesn't block submission
    const initialContent = {
      templateId: 'item_test',
      name: 'Test Item',
      category: 'weapon',
      baseValue: 50,
      weight: 3.0
    };

    render(
      <ContentEditor
        contentType="ITEM_TEMPLATE"
        initialContent={initialContent}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Edit ITEM TEMPLATE/i)).toBeInTheDocument();
    });

    // Click save button
    const saveButton = screen.getByRole('button', { name: /^Save$/i });
    await user.click(saveButton);

    await waitFor(() => {
      expect(mockOnSave).toHaveBeenCalledTimes(1);
    });
  });

  it('should call onCancel when cancel is clicked', async () => {
    const user = userEvent.setup();

    render(
      <ContentEditor
        contentType="ITEM_TEMPLATE"
        onSave={mockOnSave}
        onCancel={mockOnCancel}
      />
    );

    const cancelButton = await screen.findByRole('button', { name: /Cancel/i });
    await user.click(cancelButton);

    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it('should display error when save fails', async () => {
    const user = userEvent.setup();
    const errorMessage = 'Failed to save content';
    mockOnSave.mockRejectedValue(new Error(errorMessage));

    // Provide valid initial content so form validation doesn't block submission
    const initialContent = {
      templateId: 'item_test',
      name: 'Test Item',
      category: 'weapon',
      baseValue: 50,
      weight: 3.0
    };

    render(
      <ContentEditor
        contentType="ITEM_TEMPLATE"
        initialContent={initialContent}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Edit ITEM TEMPLATE/i)).toBeInTheDocument();
    });

    const saveButton = screen.getByRole('button', { name: /^Save$/i });
    await user.click(saveButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should handle JSON mode save correctly', async () => {
    const user = userEvent.setup();
    mockOnSave.mockResolvedValue(undefined);

    render(
      <ContentEditor
        contentType="ITEM_TEMPLATE"
        onSave={mockOnSave}
        onCancel={mockOnCancel}
      />
    );

    // Switch to JSON mode
    const toggleButton = await screen.findByRole('button', { name: /JSON Mode/i });
    await user.click(toggleButton);

    // Enter JSON data
    const textarea = screen.getByRole('textbox') as HTMLTextAreaElement;
    const testData = JSON.stringify({
      templateId: 'item_test',
      name: 'Test Item',
      category: 'weapon',
      baseValue: 50,
      weight: 3.0
    });
    
    // Use fireEvent to avoid curly brace interpretation issues
    fireEvent.change(textarea, { target: { value: testData } });

    // Click save
    const saveButton = screen.getByRole('button', { name: /^Save$/i });
    await user.click(saveButton);

    await waitFor(() => {
      expect(mockOnSave).toHaveBeenCalledWith(JSON.parse(testData));
    });
  });

  it('should show error for invalid JSON in JSON mode', async () => {
    const user = userEvent.setup();

    render(
      <ContentEditor
        contentType="ITEM_TEMPLATE"
        onSave={mockOnSave}
        onCancel={mockOnCancel}
      />
    );

    // Switch to JSON mode
    const toggleButton = await screen.findByRole('button', { name: /JSON Mode/i });
    await user.click(toggleButton);

    // Enter invalid JSON
    const textarea = screen.getByRole('textbox') as HTMLTextAreaElement;
    // Use fireEvent to avoid curly brace interpretation issues
    fireEvent.change(textarea, { target: { value: '{invalid json}' } });

    // Try to switch back to form mode
    const formModeButton = screen.getByRole('button', { name: /Form Mode/i });
    await user.click(formModeButton);

    // Should show error (use more specific text match)
    expect(screen.getByText('Invalid JSON. Please fix before switching to form mode.')).toBeInTheDocument();

    // Should still be in JSON mode
    expect(screen.getByRole('button', { name: /Form Mode/i })).toBeInTheDocument();
  });
});
