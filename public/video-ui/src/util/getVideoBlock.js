export function getVideoBlock(id, metadata) {
  return {
    elements: [
      {
        elementType: 'content-atom',
        fields: {
          id: id,
          atomType: 'media',
          required: 'true',
          title: metadata.title,
          description: metadata.description,
          published: 'Unable to get published state from atom',
          isMandatory: 'true',
          editorialLink: ''

        },
        assets: []
      }
    ]
  };
}
