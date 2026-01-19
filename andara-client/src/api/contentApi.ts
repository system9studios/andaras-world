import apiClient from './client';

export interface ContentVersion {
  versionId: string;
  contentType: string;
  contentId: string;
  versionNumber: number;
  contentData: unknown;
  importedAt: string;
  importedBy: string;
  changeSummary: string;
}

export interface ContentListResponse {
  items: ContentVersion[];
  totalCount: number;
  page: number;
  pageSize: number;
  totalPages: number;
  hasNext: boolean;
}

export interface ContentResponse {
  content: unknown;
  metadata: {
    contentId: string;
    contentType: string;
    versionNumber: number;
    importedAt: string;
    importedBy: string;
    changeSummary: string;
  };
}

export interface ContentImportRequest {
  items: unknown[];
  importedBy?: string;
  changeSummary?: string;
}

export interface ContentImportResponse {
  success: boolean;
  importedCount?: number;
  importedIds?: string[];
  contentId?: string;
  warnings?: string[];
  dryRun?: boolean;
  errors?: string[];
  message?: string;
}

export interface ContentExportResponse {
  success: boolean;
  exportedCount: number;
  outputDirectory: string;
  manifestPath: string;
  errors: string[];
}

export interface VersionHistoryResponse {
  contentId: string;
  contentType: string;
  versions: ContentVersion[];
}

// Admin token configuration - set via environment variable or localStorage
const ADMIN_TOKEN_KEY = 'ANDARA_ADMIN_TOKEN';

/**
 * Get admin token from localStorage or environment.
 */
function getAdminToken(): string | null {
  // Check localStorage first (for browser-based configuration)
  const token = localStorage.getItem(ADMIN_TOKEN_KEY);
  if (token) return token;
  
  // In production, this could come from a secure auth service
  return null;
}

/**
 * Set admin token in localStorage.
 */
export function setAdminToken(token: string) {
  localStorage.setItem(ADMIN_TOKEN_KEY, token);
}

/**
 * Clear admin token from localStorage.
 */
export function clearAdminToken() {
  localStorage.removeItem(ADMIN_TOKEN_KEY);
}

/**
 * Get headers with admin token if available.
 */
function getAdminHeaders(): Record<string, string> {
  const token = getAdminToken();
  if (token) {
    return { 'X-Admin-Token': token };
  }
  return {};
}

/**
 * List all content of a given type with pagination and search.
 */
export async function listContent(
  contentType: string,
  page: number = 0,
  pageSize: number = 20,
  search?: string
): Promise<ContentListResponse> {
  const params = new URLSearchParams({
    page: page.toString(),
    pageSize: pageSize.toString(),
  });
  
  if (search) {
    params.append('search', search);
  }
  
  const response = await apiClient.get<ContentListResponse>(
    `/admin/content/${contentType}?${params.toString()}`,
    { headers: getAdminHeaders() }
  );
  return response.data;
}

/**
 * Get a specific content item by type and ID.
 */
export async function getContent(
  contentType: string,
  contentId: string
): Promise<ContentResponse> {
  const response = await apiClient.get<ContentResponse>(
    `/admin/content/${contentType}/${contentId}`,
    { headers: getAdminHeaders() }
  );
  return response.data;
}

/**
 * Create new content item.
 */
export async function createContent(
  contentType: string,
  content: unknown,
  importedBy: string = 'admin',
  changeSummary: string = 'Created via UI'
): Promise<ContentImportResponse> {
  const response = await apiClient.post<ContentImportResponse>(
    `/admin/content/${contentType}`,
    {
      content,
      importedBy,
      changeSummary,
    },
    { headers: getAdminHeaders() }
  );
  return response.data;
}

/**
 * Update existing content item.
 */
export async function updateContent(
  contentType: string,
  contentId: string,
  content: unknown,
  importedBy: string = 'admin',
  changeSummary: string = 'Updated via UI'
): Promise<ContentImportResponse> {
  const response = await apiClient.put<ContentImportResponse>(
    `/admin/content/${contentType}/${contentId}`,
    {
      content,
      importedBy,
      changeSummary,
    },
    { headers: getAdminHeaders() }
  );
  return response.data;
}

/**
 * Delete content item (deactivates it, preserves version history).
 */
export async function deleteContent(
  contentType: string,
  contentId: string
): Promise<{ success: boolean; message: string }> {
  const response = await apiClient.delete<{ success: boolean; message: string }>(
    `/admin/content/${contentType}/${contentId}`,
    { headers: getAdminHeaders() }
  );
  return response.data;
}

/**
 * Get version history for a content item.
 */
export async function getVersionHistory(
  contentType: string,
  contentId: string
): Promise<VersionHistoryResponse> {
  const response = await apiClient.get<VersionHistoryResponse>(
    `/admin/content/${contentType}/${contentId}/history`,
    { headers: getAdminHeaders() }
  );
  return response.data;
}

/**
 * Import content into the database (batch operation).
 */
export async function importContent(
  contentType: string,
  items: unknown[],
  dryRun: boolean = false,
  importedBy: string = 'admin',
  changeSummary: string = ''
): Promise<ContentImportResponse> {
  const response = await apiClient.post<ContentImportResponse>(
    `/admin/content/import?type=${contentType}&dryRun=${dryRun}`,
    {
      items,
      importedBy,
      changeSummary,
    },
    { headers: getAdminHeaders() }
  );
  return response.data;
}

/**
 * Export content from the database.
 */
export async function exportContent(
  contentType: string,
  outputDir: string = './content-export',
  exportedBy: string = 'admin',
  environment: string = 'dev'
): Promise<ContentExportResponse> {
  const response = await apiClient.get<ContentExportResponse>(
    `/admin/content/export?type=${contentType}&outputDir=${encodeURIComponent(outputDir)}&exportedBy=${encodeURIComponent(exportedBy)}&environment=${encodeURIComponent(environment)}`,
    { headers: getAdminHeaders() }
  );
  return response.data;
}
