import React from 'react';
import { ContentVersion } from '../api/contentApi';
import './AuditLog.css';

interface AuditLogProps {
  versions: ContentVersion[];
  loading?: boolean;
  error?: string | null;
}

/**
 * Displays audit log (version history) for content items.
 * Shows chronological list of all changes with metadata.
 */
export const AuditLog: React.FC<AuditLogProps> = ({ versions, loading, error }) => {
  if (loading) {
    return (
      <div className="audit-log">
        <div className="audit-log-loading">Loading version history...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="audit-log">
        <div className="audit-log-error">{error}</div>
      </div>
    );
  }

  if (versions.length === 0) {
    return (
      <div className="audit-log">
        <div className="audit-log-empty">No version history available</div>
      </div>
    );
  }

  return (
    <div className="audit-log">
      <h3>Version History</h3>
      <div className="audit-log-table">
        <table>
          <thead>
            <tr>
              <th>Version</th>
              <th>Date & Time</th>
              <th>Author</th>
              <th>Change Summary</th>
            </tr>
          </thead>
          <tbody>
            {versions.map((version) => (
              <tr key={version.versionId}>
                <td className="version-number">
                  <span className="version-badge">v{version.versionNumber}</span>
                </td>
                <td className="version-date">
                  {new Date(version.importedAt).toLocaleString()}
                </td>
                <td className="version-author">{version.importedBy}</td>
                <td className="version-summary">
                  {version.changeSummary || <em>No summary provided</em>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
