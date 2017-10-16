const editorialCategories = [
  'News',
  'Documentary',
  'Explainer',
  'Feature'
].map(cat => ({ id: cat, title: cat }));

// id matches the thrift enum
// title is for ui
// see https://github.com/guardian/content-atom/blob/master/thrift/src/main/thrift/atoms/media.thrift#L22-L29
const labsCategories = [
  {id: 'Hosted', title: 'GLabs - Hosted by'},
  {id: 'Paid', title: 'GLabs - Paid for'}
];

export const videoCategories = [
  ...editorialCategories,
  ...labsCategories
];
