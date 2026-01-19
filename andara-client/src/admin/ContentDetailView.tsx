import React, { useState, useEffect } from 'react';
import { getContent, getVersionHistory, ContentVersion } from '../api/contentApi';
import './ContentDetailView.css';

interface ContentDetailViewProps {
  contentType: string;
  contentId: string;
  onEdit?: () => void;
  onClose?: () => void;
}

export const ContentDetailView: React.FC<ContentDetailViewProps> = ({
  contentType,
  contentId,
  onEdit,
  onClose,
}) => {
  const [content, setContent] = useState<unknown>(null);
  const [metadata, setMetadata] = useState<{
    contentId: string;
    contentType: string;
    versionNumber: number;
    importedAt: string;
    importedBy: string;
    changeSummary: string;
  } | null>(null);
  const [versions, setVersions] = useState<ContentVersion[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showHistory, setShowHistory] = useState(false);

  useEffect(() => {
    loadContent();
  }, [contentType, contentId]);

  const loadContent = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await getContent(contentType, contentId);
      setContent(response.content);
      setMetadata(response.metadata);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load content');
      console.error('Error loading content:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadHistory = async () => {
    if (versions.length > 0) {
      setShowHistory(!showHistory);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await getVersionHistory(contentType, contentId);
      setVersions(response.versions);
      setShowHistory(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load version history');
      console.error('Error loading version history:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading && !content) {
    return (
      <div className="content-detail-view">
        <div className="content-detail-loading">Loading...</div>
      </div>
    );
  }

  if (error && !content) {
    return (
      <div className="content-detail-view">
        <div className="content-detail-error">
          <h3>Error</h3>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  if (!content || !metadata) {
    return (
      <div className="content-detail-view">
        <div className="content-detail-error">Content not found</div>
      </div>
    );
  }

  return (
    <div className="content-detail-view">
      <div className="content-detail-header">
        <div>
          <h2>{contentType.replace(/_/g, ' ')}</h2>
          <p className="content-detail-id">{contentId}</p>
        </div>
        <div className="content-detail-actions">
          <button onClick={loadHistory} className="btn-secondary">
            {showHistory ? 'Hide History' : 'Show History'}
          </button>
          {onEdit && (
            <button onClick={onEdit} className="btn-primary">
              Edit
            </button>
          )}
          {onClose && (
            <button onClick={onClose} className="btn-secondary">
              Close
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="content-detail-error">
          {error}
        </div>
      )}

      <div className="content-detail-metadata">
        <div className="metadata-item">
          <span className="metadata-label">Version:</span>
          <span className="metadata-value">v{metadata.versionNumber}</span>
        </div>
        <div className="metadata-item">
          <span className="metadata-label">Last Modified:</span>
          <span className="metadata-value">
            {new Date(metadata.importedAt).toLocaleString()}
          </span>
        </div>
        <div className="metadata-item">
          <span className="metadata-label">Modified By:</span>
          <span className="metadata-value">{metadata.importedBy}</span>
        </div>
        {metadata.changeSummary && (
          <div className="metadata-item">
            <span className="metadata-label">Change Summary:</span>
            <span className="metadata-value">{metadata.changeSummary}</span>
          </div>
        )}
      </div>

      <div className="content-detail-body">
        <h3>Content</h3>
        <pre className="content-detail-json">
          {JSON.stringify(content, null, 2)}
        </pre>
      </div>

      {showHistory && versions.length > 0 && (
        <div className="content-detail-history">
          <h3>Version History</h3>
          <table className="history-table">
            <thead>
              <tr>
                <th>Version</th>
                <th>Date</th>
                <th>Author</th>
                <th>Change Summary</th>
              </tr>
            </thead>
            <tbody>
              {versions.map((version) => (
                <tr key={version.versionId}>
                  <td>v{version.versionNumber}</td>
                  <td>{new Date(version.importedAt).toLocaleString()}</td>
                  <td>{version.importedBy}</td>
                  <td>{version.changeSummary || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
