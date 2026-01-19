import React, { useState, useEffect } from 'react';
import { listContent, deleteContent, ContentVersion } from '../api/contentApi';
import './ContentListView.css';

interface ContentListViewProps {
  contentType: string;
  onSelect?: (contentId: string) => void;
  onCreate?: () => void;
  onEdit?: (contentId: string) => void;
}

export const ContentListView: React.FC<ContentListViewProps> = ({ 
  contentType, 
  onSelect,
  onCreate,
  onEdit
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [items, setItems] = useState<ContentVersion[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalCount, setTotalCount] = useState(0);

  const loadContent = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await listContent(
        contentType,
        page,
        20,
        searchTerm || undefined
      );
      
      setItems(response.items);
      setTotalPages(response.totalPages);
      setTotalCount(response.totalCount);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load content');
      console.error('Error loading content:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContent();
  }, [contentType, page, searchTerm]);

  const handleDelete = async (contentId: string) => {
    if (!confirm(`Are you sure you want to delete ${contentId}?`)) {
      return;
    }

    try {
      await deleteContent(contentType, contentId);
      // Reload the list
      loadContent();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete content');
      console.error('Error deleting content:', err);
    }
  };

  const handleSearch = (term: string) => {
    setSearchTerm(term);
    setPage(0); // Reset to first page on search
  };

  const extractName = (item: ContentVersion): string => {
    const data = item.contentData as Record<string, unknown>;
    return (data?.name as string) || item.contentId;
  };

  return (
    <div className="content-list-view">
      <div className="content-list-header">
        <h2>{contentType.replace(/_/g, ' ')} ({totalCount})</h2>
        <div className="content-list-actions">
          <input
            type="text"
            placeholder="Search..."
            value={searchTerm}
            onChange={(e) => handleSearch(e.target.value)}
            className="content-search"
          />
          <button className="btn-primary" onClick={onCreate}>
            Create New
          </button>
        </div>
      </div>

      {error && (
        <div className="content-list-error">
          {error}
        </div>
      )}

      {loading ? (
        <div className="content-list-loading">Loading...</div>
      ) : (
        <>
          <div className="content-list-table">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Version</th>
                  <th>Last Modified</th>
                  <th>Modified By</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {items.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="content-list-empty">
                      No content items found. {onCreate && 'Click "Create New" to add one.'}
                    </td>
                  </tr>
                ) : (
                  items.map((item) => (
                    <tr key={item.contentId}>
                      <td className="content-id">{item.contentId}</td>
                      <td>{extractName(item)}</td>
                      <td>v{item.versionNumber}</td>
                      <td>{new Date(item.importedAt).toLocaleString()}</td>
                      <td>{item.importedBy}</td>
                      <td className="content-actions">
                        <button 
                          onClick={() => onSelect?.(item.contentId)}
                          className="btn-small btn-view"
                        >
                          View
                        </button>
                        <button 
                          onClick={() => onEdit?.(item.contentId)}
                          className="btn-small btn-edit"
                        >
                          Edit
                        </button>
                        <button 
                          onClick={() => handleDelete(item.contentId)}
                          className="btn-small btn-delete"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="content-list-pagination">
              <button 
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="btn-pagination"
              >
                Previous
              </button>
              <span className="pagination-info">
                Page {page + 1} of {totalPages}
              </span>
              <button 
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
                className="btn-pagination"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};
