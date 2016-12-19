// TODO add `Hosted` category.
// Although `Hosted` is a valid category, the APIs driving the React frontend perform authenticated calls to YT.
// These only work with content that we own. `Hosted` can have third-party assets so the API calls will fail.
// Add `Hosted` once the UI is smarter and removes features when category is `Hosted`.

export const videoCategories = [
  'News',
  'Documentary',
  'Explainer',
  'Feature',
  // 'Hosted'
].map(cat => { return { id: cat, title: cat } });
