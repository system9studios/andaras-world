# Andara Content CLI

Command-line tool for managing game content in Andara's World.

## Installation

The CLI is built with the main server project. Build it using Gradle:

```bash
cd andara-server
./gradlew :andara-content-cli:build
```

## Configuration

### Admin Token

Most commands require admin authentication. Set your admin token via environment variable:

```bash
export ANDARA_ADMIN_TOKEN=your-secret-token
```

Or pass it directly with the `--token` option:

```bash
andara-content import ITEM_TEMPLATE --source ./items --token your-secret-token
```

### Server URL

By default, commands connect to `http://localhost:8080`. Override with `--server`:

```bash
andara-content import ITEM_TEMPLATE --source ./items --server https://staging.andara.example.com
```

## Commands

### create

Generate a new content file from schema template.

```bash
andara-content create <content-type> --output <file>
```

**Examples:**

```bash
# Create an item template
andara-content create ITEM_TEMPLATE --output items/new-sword.json

# Create a skill definition
andara-content create SKILL_DEFINITION --output skills/new-skill.json

# Create without comments
andara-content create RECIPE --output recipes/new-recipe.json --with-comments=false
```

**Content Types:**
- `ITEM_TEMPLATE`
- `SKILL_DEFINITION`
- `ABILITY_DEFINITION`
- `RECIPE`
- `REGION_DEFINITION`
- `ZONE_TEMPLATE`
- `POI_TEMPLATE`
- `NPC_TEMPLATE`
- `FACTION_DEFINITION`
- `ENCOUNTER_TEMPLATE`
- `DIALOGUE_TREE`

### validate

Validate content files against JSON schemas.

```bash
andara-content validate <file-or-directory> [--type <content-type>] [--all]
```

**Examples:**

```bash
# Validate a single file (infers type from filename)
andara-content validate items/rusty-sword.json

# Validate with explicit type
andara-content validate items/custom-item.json --type ITEM_TEMPLATE

# Validate all files in a directory
andara-content validate ./content --all

# Validate all files in current directory only
andara-content validate ./items
```

**Exit Codes:**
- `0` - All files valid
- `1` - Validation errors found

### import

Import content from files into the database via REST API.

```bash
andara-content import <content-type> --source <file-or-directory> [options]
```

**Options:**
- `--source` - Source file or directory (required)
- `--server` - Server URL (default: http://localhost:8080)
- `--env` - Environment name (default: dev)
- `--dry-run` - Validate only, don't import (default: false)
- `--token` - Admin API token (or use ANDARA_ADMIN_TOKEN env var)

**Examples:**

```bash
# Import all items from directory
andara-content import ITEM_TEMPLATE --source ./items

# Import with dry-run (validation only)
andara-content import SKILL_DEFINITION --source ./skills --dry-run

# Import to staging server
andara-content import RECIPE --source recipes.json --server https://staging.example.com --env staging

# Import with explicit token
andara-content import ITEM_TEMPLATE --source ./items --token abc123def456
```

### export

Export content from database to files via REST API.

```bash
andara-content export <content-type> [options]
```

**Options:**
- `--output` - Output directory (default: ./content-export)
- `--server` - Server URL (default: http://localhost:8080)
- `--env` - Environment name (default: dev)
- `--token` - Admin API token (or use ANDARA_ADMIN_TOKEN env var)

**Examples:**

```bash
# Export all items
andara-content export ITEM_TEMPLATE

# Export to specific directory
andara-content export SKILL_DEFINITION --output ./exported-skills

# Export from production server
andara-content export RECIPE --server https://prod.example.com --env prod
```

## Workflow Examples

### Creating New Content

```bash
# 1. Create template
andara-content create ITEM_TEMPLATE --output items/magic-sword.json

# 2. Edit the file (fill in required fields)
# Edit items/magic-sword.json in your favorite editor

# 3. Validate
andara-content validate items/magic-sword.json

# 4. Import to database
andara-content import ITEM_TEMPLATE --source items/magic-sword.json
```

### Batch Import

```bash
# 1. Create directory of content files
mkdir -p content/items
# Create multiple .json files in content/items/

# 2. Validate all files
andara-content validate content/items --all

# 3. Import all at once
andara-content import ITEM_TEMPLATE --source content/items
```

### Backup and Restore

```bash
# Export from production
export ANDARA_ADMIN_TOKEN=prod-token
andara-content export ITEM_TEMPLATE --server https://prod.example.com --output ./backup/items

# Import to development
export ANDARA_ADMIN_TOKEN=dev-token
andara-content import ITEM_TEMPLATE --source ./backup/items --server http://localhost:8080
```

## Troubleshooting

### Authentication Errors

If you see "Authentication failed" or 401 errors:

1. Ensure `ANDARA_ADMIN_TOKEN` environment variable is set:
   ```bash
   echo $ANDARA_ADMIN_TOKEN
   ```

2. Or pass token explicitly with `--token` option

3. Verify the server has authentication configured in `application.yml`:
   ```yaml
   andara:
     admin:
       token: ${ANDARA_ADMIN_TOKEN:}
   ```

### Connection Errors

If you see "Failed to connect to server":

1. Check the server is running:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. Verify the correct server URL with `--server` option

3. Check firewall/network settings

### Validation Errors

If validation fails:

1. Check the error messages for specific issues
2. Verify required fields are present
3. Check field types match schema (string vs integer, etc.)
4. Ensure enum values are valid
5. Use `--type` to explicitly specify content type if inference fails

### Schema Not Found

If you see "Schema not found":

1. Ensure you're running from the project root or
2. Schemas should be in `docs/content-schemas/` directory
3. Or packaged in the JAR at `/docs/content-schemas/`

## Building and Running

### Build

```bash
cd andara-server
./gradlew :andara-content-cli:build
```

### Run

```bash
# Using Gradle
./gradlew :andara-content-cli:run --args="validate items/test-item.json"

# Using JAR directly (after build)
java -jar andara-content-cli/build/libs/andara-content-cli.jar validate items/test-item.json

# Create alias for convenience
alias andara-content="java -jar $(pwd)/andara-content-cli/build/libs/andara-content-cli.jar"
```

## Development

### Adding New Commands

1. Create a new command class in `commands/` package
2. Implement `Callable<Integer>` interface
3. Add `@CommandLine.Command` annotation
4. Register in `ContentCLI.java` subcommands

### Testing

Run unit tests:

```bash
./gradlew :andara-content-cli:test
```

## Support

For issues or questions:
- Check the main project README
- Review schema definitions in `docs/content-schemas/`
- Check server logs for detailed error messages
