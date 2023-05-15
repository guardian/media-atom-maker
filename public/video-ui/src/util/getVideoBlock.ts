export type ContentAtom = {
  elementType: 'content-atom';
  fields: {
    id: string;
    atomType: 'media';
    required: 'true' | 'false';
    title: string;
    published: string;
    isMandatory: 'true' | 'false';
    editorialLink: string;
    source: string;
  };
  assets: unknown[];
}

export function getVideoBlock(id: string, title: string, source: string): { elements: ContentAtom[] } {
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
