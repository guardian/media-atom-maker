export function getComposerPages(usages) {
  if (!usages) {
    return false;
  }
  return usages.filter(value => value.type === 'video');
}
