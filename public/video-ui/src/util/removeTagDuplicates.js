export default function removeTagDuplicates(tag, tagValue) {
  return tagValue.filter(value => value.id !== tag.id);
}
