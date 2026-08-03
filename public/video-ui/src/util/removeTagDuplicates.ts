type Tag = { id: string };

export function removeTagDuplicates<T extends Tag>(
  tag: Tag,
  tagValue: T[]
): T[] {
  return tagValue.filter(value => value.id !== tag.id);
}
