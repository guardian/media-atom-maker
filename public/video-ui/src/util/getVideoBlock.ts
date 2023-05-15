export function getVideoBlock(id, title, source) {
  return {
    elements: [
      {
        elementType: 'content-atom',
        fields: {
          id: id,
          atomType: 'media',
          required: 'true',
          title: title,
          published: 'Unable to get published state from atom',
          isMandatory: 'true',
          editorialLink: '',
          source: source
        },
        assets: []
      }
    ]
  };
}
