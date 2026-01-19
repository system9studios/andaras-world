# Andara Admin Content Management UI

Web-based content management interface for Andara's World.

## Accessing the Admin Panel

Navigate to `/admin/content` in your browser:

```
http://localhost:5173/admin/content
```

## Features

### Content Type Navigation

The left sidebar displays all available content types:
- Item Templates
- Skill Definitions
- Ability Definitions
- Recipes
- Region Definitions
- Zone Templates
- POI Templates
- NPC Templates
- Faction Definitions
- Encounter Templates
- Dialogue Trees

Click any content type to view and manage items of that type.

### List View

The main content area shows a paginated list of all items for the selected content type.

**Features:**
- **Search**: Filter items by ID or content using the search box
- **Pagination**: Navigate through pages (20 items per page)
- **Actions**: View, Edit, or Delete individual items
- **Create New**: Button to create a new item

**Columns:**
- ID - Unique identifier for the content item
- Name - Display name extracted from content
- Version - Current version number
- Last Modified - Date and time of last modification
- Modified By - User who made the last modification
- Actions - Quick action buttons

### Detail View

Click "View" on any item to see detailed information.

**Sections:**
- **Header**: Content type and ID
- **Metadata**: Version, modification date, author, change summary
- **Content**: Full JSON representation of the content
- **Version History**: Chronological list of all versions with change summaries

**Actions:**
- **Edit**: Opens the editor with current content
- **Show/Hide History**: Toggle version history display
- **Close**: Return to list view

### Content Editor

Create new or edit existing content using the schema-driven editor.

**Two Modes:**

1. **Form Mode** (Default)
   - Automatically generated forms based on JSON schema
   - Field validation
   - Type-appropriate inputs (text, numbers, dropdowns, etc.)
   - Required fields marked clearly

2. **JSON Mode**
   - Direct JSON editing in textarea
   - Syntax highlighting
   - Useful for advanced users or bulk edits

**Features:**
- **Mode Toggle**: Switch between Form and JSON mode
- **Validation**: Client-side validation before submission
- **Save**: Create or update content in database
- **Cancel**: Return to previous view without saving

### Import Wizard

Bulk import content from JSON files.

**Steps:**
1. Select content type from sidebar
2. Click "Import Content" button
3. Choose one or more `.json` files
4. Enable/disable "Dry Run" (validation only)
5. Click "Import"

**Dry Run Mode:**
- Validates files without importing
- Useful for testing before actual import
- Shows validation errors and warnings

### Version History & Audit Log

Every content modification is versioned and tracked.

**Information Tracked:**
- Version number
- Date and time of change
- Author (user who made the change)
- Change summary (optional description)
- Full content snapshot for each version

**Benefits:**
- Audit trail for compliance
- Rollback capability (view previous versions)
- Change tracking and accountability

## Configuration

### Admin Token

If the server requires authentication, set the admin token in your browser:

1. Open browser console (F12)
2. Run:
   ```javascript
   localStorage.setItem('ANDARA_ADMIN_TOKEN', 'your-secret-token');
   ```
3. Refresh the page

The token is stored in localStorage and automatically included in all API requests.

**To clear the token:**
```javascript
localStorage.removeItem('ANDARA_ADMIN_TOKEN');
```

### Server URL

By default, the admin panel connects to `/api/admin/content` relative to the current origin.

For development with separate frontend/backend:
- Ensure API client is configured correctly in `src/api/client.ts`
- CORS must be enabled on the backend for cross-origin requests

## Workflows

### Creating New Content

1. **Select Content Type** from sidebar (e.g., Item Templates)
2. Click **"Create New"** button
3. Fill in required fields in the form
   - Or switch to JSON mode for manual entry
4. Click **"Save"**
5. View newly created item in detail view

### Editing Existing Content

1. **Navigate** to content type
2. **Search** or browse to find the item
3. Click **"Edit"** button
4. Modify fields as needed
5. Add change summary (optional but recommended)
6. Click **"Save"**
7. New version is created and activated

### Reviewing Changes

1. **Navigate** to item detail view
2. Click **"Show History"**
3. Review version list with:
   - Version numbers
   - Dates
   - Authors
   - Change summaries
4. Compare versions to understand what changed

### Bulk Import

1. Prepare JSON files conforming to schema
2. Click **"Import Content"** button
3. Select content type
4. Upload files
5. Test with **dry-run** first
6. Import for real after validation passes

### Searching Content

1. Select content type
2. Enter search term in search box
3. Search looks in:
   - Content IDs
   - Content data (names, descriptions, etc.)
4. Press Enter or wait for auto-search
5. Clear search to see all items

### Deleting Content

1. Navigate to content item
2. Click **"Delete"** button
3. Confirm deletion
4. Item is **deactivated** (not permanently deleted)
5. Version history is preserved
6. Item removed from active content list

**Note:** Deletion is soft - version history remains in the database for audit purposes.

## Tips & Best Practices

### Version Control

- Always provide meaningful change summaries
- Use version history to track who changed what
- Review history before making major changes

### Content Validation

- Use Form Mode for guided creation
- JSON Mode for power users and bulk edits
- Validate with Dry Run before bulk imports
- Check validation errors carefully

### Search Tips

- Search is case-insensitive
- Searches both IDs and content data
- Use unique identifiers for precise searches
- Use names or keywords for broader searches

### Performance

- Pagination limits to 20 items per page for performance
- Large content sets load quickly
- Search filters results efficiently

## Keyboard Shortcuts

- **Tab**: Navigate between form fields
- **Ctrl/Cmd + Enter**: Save (in form mode)
- **Esc**: Cancel editing

## Troubleshooting

### "Authentication Failed" Error

**Solution:**
1. Ensure admin token is set in localStorage
2. Check token is correct
3. Verify server authentication is configured
4. Check browser console for detailed errors

### "Failed to Load Content" Error

**Possible Causes:**
1. Server is not running
2. Network connectivity issues
3. CORS not configured for development
4. Invalid content type

**Solutions:**
1. Check server status
2. Verify API URL in browser network tab
3. Enable CORS in development mode
4. Refresh the page

### Form Fields Not Showing

**Cause:** Schema not loaded

**Solution:**
1. Check browser console for errors
2. Ensure schemas are in `/public/schemas/` directory
3. Verify schema files are valid JSON
4. Clear browser cache and reload

### Changes Not Appearing

**Solution:**
1. Refresh the list view
2. Check for validation errors
3. Verify save operation succeeded
4. Check server logs for errors

## Development

### Running Locally

```bash
cd andara-client
npm install
npm run dev
```

Admin panel will be available at `http://localhost:5173/admin/content`

### Adding New Content Types

1. Add schema to `public/schemas/`
2. Update `CONTENT_TYPE_TO_SCHEMA` in `useSchemas.ts`
3. Add to content type list in `AdminContentPage.tsx`
4. Update backend `ContentType` enum

### Customizing Forms

Forms are auto-generated from JSON schemas. To customize:

1. Use `uiSchema` prop in ContentEditor for field ordering
2. Add custom widgets for special field types
3. Modify `ContentEditor.tsx` for global form styling

### Testing

Run tests:

```bash
npm run test
```

Run specific test file:

```bash
npm run test -- ContentEditor.test.tsx
```

## Support

For issues or feature requests:
- Check schema definitions in `public/schemas/`
- Review API documentation
- Check browser console for errors
- Check server logs for backend issues
