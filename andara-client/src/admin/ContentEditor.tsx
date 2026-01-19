import React, { useState, useEffect } from 'react';
import Form from '@rjsf/core';
import { RJSFSchema } from '@rjsf/utils';
import validator from '@rjsf/validator-ajv8';
import { useSchema } from './hooks/useSchemas';
import './ContentEditor.css';

interface ContentEditorProps {
  contentType: string;
  initialContent?: unknown;
  onSave: (content: unknown) => Promise<void>;
  onCancel?: () => void;
}

export const ContentEditor: React.FC<ContentEditorProps> = ({
  contentType,
  initialContent,
  onSave,
  onCancel,
}) => {
  const { schema, loading: schemaLoading, error: schemaError } = useSchema(contentType);
  const [formData, setFormData] = useState<unknown>(initialContent || {});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [jsonMode, setJsonMode] = useState(false);
  const [jsonText, setJsonText] = useState('');

  useEffect(() => {
    if (initialContent) {
      setFormData(initialContent);
      setJsonText(JSON.stringify(initialContent, null, 2));
    }
  }, [initialContent]);

  const handleFormSubmit = async (data: { formData?: unknown }) => {
    try {
      setSaving(true);
      setError(null);
      
      const contentToSave = jsonMode ? JSON.parse(jsonText) : data.formData;
      await onSave(contentToSave);
      
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save');
    } finally {
      setSaving(false);
    }
  };

  const toggleJsonMode = () => {
    if (!jsonMode) {
      // Switching to JSON mode - serialize current form data
      setJsonText(JSON.stringify(formData, null, 2));
    } else {
      // Switching to form mode - parse JSON
      try {
        const parsed = JSON.parse(jsonText);
        setFormData(parsed);
      } catch (e) {
        setError('Invalid JSON. Please fix before switching to form mode.');
        return;
      }
    }
    setJsonMode(!jsonMode);
  };

  const handleJsonSave = async () => {
    try {
      setSaving(true);
      setError(null);
      
      const parsed = JSON.parse(jsonText);
      await onSave(parsed);
      
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save');
    } finally {
      setSaving(false);
    }
  };

  if (schemaLoading) {
    return (
      <div className="content-editor">
        <div className="content-editor-loading">Loading schema...</div>
      </div>
    );
  }

  if (schemaError) {
    return (
      <div className="content-editor">
        <div className="content-editor-error">
          <h3>Schema Error</h3>
          <p>{schemaError}</p>
        </div>
      </div>
    );
  }

  if (!schema) {
    return (
      <div className="content-editor">
        <div className="content-editor-error">No schema available for {contentType}</div>
      </div>
    );
  }

  return (
    <div className="content-editor">
      <div className="content-editor-header">
        <h2>Edit {contentType.replace(/_/g, ' ')}</h2>
        <div className="content-editor-actions">
          <button 
            onClick={toggleJsonMode} 
            className="btn-secondary"
            type="button"
          >
            {jsonMode ? 'Form Mode' : 'JSON Mode'}
          </button>
          {onCancel && (
            <button onClick={onCancel} className="btn-secondary" type="button">
              Cancel
            </button>
          )}
          {jsonMode ? (
            <button 
              onClick={handleJsonSave} 
              disabled={saving} 
              className="btn-primary"
              type="button"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          ) : null}
        </div>
      </div>
      
      {error && (
        <div className="content-editor-error">
          {error}
        </div>
      )}
      
      <div className="content-editor-body">
        {jsonMode ? (
          <div className="content-editor-json">
            <textarea
              value={jsonText}
              onChange={(e) => setJsonText(e.target.value)}
              className="content-editor-textarea"
              spellCheck={false}
            />
          </div>
        ) : (
          <div className="content-editor-form">
            <Form
              schema={schema as RJSFSchema}
              formData={formData}
              validator={validator}
              onChange={(e) => setFormData(e.formData)}
              onSubmit={handleFormSubmit}
              disabled={saving}
            >
              <div className="content-editor-form-actions">
                <button 
                  type="submit" 
                  disabled={saving} 
                  className="btn-primary"
                >
                  {saving ? 'Saving...' : 'Save'}
                </button>
              </div>
            </Form>
          </div>
        )}
      </div>
    </div>
  );
};
