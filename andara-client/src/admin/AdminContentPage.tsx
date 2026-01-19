import React, { useState } from 'react';
import { ContentListView } from './ContentListView';
import { ContentDetailView } from './ContentDetailView';
import { ContentEditor } from './ContentEditor';
import { ImportWizard } from './ImportWizard';
import { createContent, updateContent, getContent } from '../api/contentApi';
import './AdminContentPage.css';

type ViewMode = 'list' | 'detail' | 'edit' | 'create' | 'import';

export const AdminContentPage: React.FC = () => {
  const [selectedContentType, setSelectedContentType] = useState<string>('ITEM_TEMPLATE');
  const [view, setView] = useState<ViewMode>('list');
  const [selectedContentId, setSelectedContentId] = useState<string | null>(null);
  const [editingContent, setEditingContent] = useState<unknown>(null);

  const contentTypes = [
    'ITEM_TEMPLATE',
    'SKILL_DEFINITION',
    'ABILITY_DEFINITION',
    'RECIPE',
    'REGION_DEFINITION',
    'ZONE_TEMPLATE',
    'POI_TEMPLATE',
    'NPC_TEMPLATE',
    'FACTION_DEFINITION',
    'ENCOUNTER_TEMPLATE',
    'DIALOGUE_TREE',
  ];

  const handleContentTypeChange = (type: string) => {
    setSelectedContentType(type);
    setView('list');
    setSelectedContentId(null);
    setEditingContent(null);
  };

  const handleSelectContent = (contentId: string) => {
    setSelectedContentId(contentId);
    setView('detail');
  };

  const handleEditContent = async (contentId: string) => {
    try {
      const response = await getContent(selectedContentType, contentId);
      setSelectedContentId(contentId);
      setEditingContent(response.content);
      setView('edit');
    } catch (err) {
      console.error('Failed to load content for editing:', err);
      alert('Failed to load content for editing');
    }
  };

  const handleCreateNew = () => {
    setSelectedContentId(null);
    setEditingContent(null);
    setView('create');
  };

  const handleSaveCreate = async (content: unknown) => {
    try {
      const response = await createContent(
        selectedContentType,
        content,
        'admin',
        'Created via UI'
      );

      if (response.success) {
        alert('Content created successfully!');
        setView('list');
        setEditingContent(null);
      } else {
        throw new Error(response.errors?.join(', ') || 'Failed to create content');
      }
    } catch (err) {
      console.error('Failed to create content:', err);
      throw err; // Re-throw to let ContentEditor handle it
    }
  };

  const handleSaveEdit = async (content: unknown) => {
    if (!selectedContentId) {
      throw new Error('No content ID selected');
    }

    try {
      const response = await updateContent(
        selectedContentType,
        selectedContentId,
        content,
        'admin',
        'Updated via UI'
      );

      if (response.success) {
        alert('Content updated successfully!');
        setView('detail');
        setEditingContent(null);
      } else {
        throw new Error(response.errors?.join(', ') || 'Failed to update content');
      }
    } catch (err) {
      console.error('Failed to update content:', err);
      throw err; // Re-throw to let ContentEditor handle it
    }
  };

  const handleCancelEdit = () => {
    if (selectedContentId) {
      setView('detail');
    } else {
      setView('list');
    }
    setEditingContent(null);
  };

  const handleCloseDetail = () => {
    setView('list');
    setSelectedContentId(null);
  };

  return (
    <div className="admin-content-page">
      <div className="admin-sidebar">
        <h2>Content Management</h2>
        <nav>
          {contentTypes.map((type) => (
            <button
              key={type}
              onClick={() => handleContentTypeChange(type)}
              className={selectedContentType === type ? 'active' : ''}
            >
              {type.replace(/_/g, ' ')}
            </button>
          ))}
        </nav>
        <div className="admin-actions">
          <button 
            onClick={() => setView('import')} 
            className="btn-primary"
          >
            Import Content
          </button>
        </div>
      </div>
      
      <div className="admin-content-area">
        {view === 'list' && (
          <ContentListView 
            contentType={selectedContentType}
            onSelect={handleSelectContent}
            onEdit={handleEditContent}
            onCreate={handleCreateNew}
          />
        )}
        
        {view === 'detail' && selectedContentId && (
          <ContentDetailView
            contentType={selectedContentType}
            contentId={selectedContentId}
            onEdit={() => handleEditContent(selectedContentId)}
            onClose={handleCloseDetail}
          />
        )}
        
        {view === 'create' && (
          <ContentEditor
            contentType={selectedContentType}
            onSave={handleSaveCreate}
            onCancel={handleCancelEdit}
          />
        )}
        
        {view === 'edit' && selectedContentId && (
          <ContentEditor
            contentType={selectedContentType}
            initialContent={editingContent}
            onSave={handleSaveEdit}
            onCancel={handleCancelEdit}
          />
        )}
        
        {view === 'import' && (
          <ImportWizard 
            contentType={selectedContentType}
            onComplete={() => setView('list')}
          />
        )}
      </div>
    </div>
  );
};
