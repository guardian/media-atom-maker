export default function removeTagDuplicates(tag: { id: any }, tagValue: any) {
  return tagValue.filter((value: { id: any }) => value.id !== tag.id);
}
