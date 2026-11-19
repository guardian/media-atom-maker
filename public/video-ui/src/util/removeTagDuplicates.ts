type Tag = { id: string };

// String values (free-text entries) have no id, so they are never treated as duplicates
export function removeTagDuplicates<T extends string | Tag>(
  tag: Tag,
  tagValue: T[]
): T[] {
  return tagValue.filter(
    value => typeof value === 'string' || value.id !== tag.id
  );
}
