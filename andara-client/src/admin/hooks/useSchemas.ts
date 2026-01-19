import { useState, useEffect, useCallback } from 'react';
import { JSONSchema7 } from 'json-schema';

// Mapping of content types to schema file names
const CONTENT_TYPE_TO_SCHEMA: Record<string, string> = {
  'ITEM_TEMPLATE': 'item-template.json',
  'SKILL_DEFINITION': 'skill-definition.json',
  'ABILITY_DEFINITION': 'ability-definition.json',
  'RECIPE': 'recipe.json',
  'REGION_DEFINITION': 'region-definition.json',
  'ZONE_TEMPLATE': 'zone-template.json',
  'POI_TEMPLATE': 'poi-template.json',
  'NPC_TEMPLATE': 'npc-template.json',
  'FACTION_DEFINITION': 'faction-definition.json',
  'ENCOUNTER_TEMPLATE': 'encounter-template.json',
  'DIALOGUE_TREE': 'dialogue-tree.json',
};

// In-memory cache for loaded schemas
const schemaCache: Map<string, JSONSchema7> = new Map();

/**
 * Hook to load and cache JSON schemas for content types.
 * 
 * @param contentType Content type to load schema for
 * @returns Schema, loading state, and error
 */
export function useSchema(contentType: string) {
  const [schema, setSchema] = useState<JSONSchema7 | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadSchema = async () => {
      // Check cache first
      if (schemaCache.has(contentType)) {
        setSchema(schemaCache.get(contentType)!);
        return;
      }

      const schemaFile = CONTENT_TYPE_TO_SCHEMA[contentType];
      if (!schemaFile) {
        setError(`Unknown content type: ${contentType}`);
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const response = await fetch(`/schemas/${schemaFile}`);
        if (!response.ok) {
          throw new Error(`Failed to load schema: ${response.statusText}`);
        }

        const schemaData = await response.json() as JSONSchema7;
        schemaCache.set(contentType, schemaData);
        setSchema(schemaData);
      } catch (err) {
        const errorMsg = err instanceof Error ? err.message : 'Failed to load schema';
        setError(errorMsg);
        console.error('Error loading schema:', err);
      } finally {
        setLoading(false);
      }
    };

    loadSchema();
  }, [contentType]);

  return { schema, loading, error };
}

/**
 * Hook to preload all schemas into cache.
 * Useful for admin panel initialization.
 */
export function usePreloadSchemas() {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [loaded, setLoaded] = useState<boolean>(false);

  const preload = useCallback(async () => {
    if (loaded) return;

    setLoading(true);
    setError(null);

    try {
      const schemaPromises = Object.entries(CONTENT_TYPE_TO_SCHEMA).map(
        async ([contentType, schemaFile]) => {
          if (schemaCache.has(contentType)) return;

          const response = await fetch(`/schemas/${schemaFile}`);
          if (!response.ok) {
            throw new Error(`Failed to load ${schemaFile}: ${response.statusText}`);
          }

          const schemaData = await response.json() as JSONSchema7;
          schemaCache.set(contentType, schemaData);
        }
      );

      await Promise.all(schemaPromises);
      setLoaded(true);
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to preload schemas';
      setError(errorMsg);
      console.error('Error preloading schemas:', err);
    } finally {
      setLoading(false);
    }
  }, [loaded]);

  return { preload, loading, error, loaded };
}

/**
 * Get schema synchronously from cache (if already loaded).
 * 
 * @param contentType Content type
 * @returns Schema or null if not in cache
 */
export function getSchemaFromCache(contentType: string): JSONSchema7 | null {
  return schemaCache.get(contentType) || null;
}

/**
 * Clear the schema cache (useful for testing or forced reload).
 */
export function clearSchemaCache() {
  schemaCache.clear();
}
