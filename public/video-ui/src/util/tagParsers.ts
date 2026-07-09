import ContentApi from '../services/capi';
import TagTypes from '../constants/TagTypes';
import { DisplayTag, ParsedTag } from '../types/tags';

type CapiTagResponse = {
  response?: {
    tag?: DisplayTag;
  };
};

const isCapiTagResponse = (value: unknown): value is CapiTagResponse => {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const response = (value as CapiTagResponse).response;
  return !response || !response.tag || typeof response.tag.id === 'string';
};

export function tagsFromStringList(
  savedTags: string[],
  tagType: string
): Promise<ParsedTag[]> {
  if (!savedTags) {
    return Promise.resolve([]);
  }

  return Promise.all(
    savedTags.map((element): Promise<ParsedTag> => {
      if (
        (tagType !== TagTypes.contributor && tagType !== TagTypes.youtube) ||
        element.match('^profile/')
      ) {
        return ContentApi.getLivePage(element).then(response => {
          if (isCapiTagResponse(response) && response.response?.tag) {
            return response.response.tag;
          }

          return {
            id: element,
            webTitle: element
          };
        });
      }

      if (tagType === TagTypes.youtube) {
        return Promise.resolve({
          id: element,
          webTitle: element
        });
      }

      return Promise.resolve(element);
    })
  );
}

export function tagsToStringList(addedTags: ParsedTag[]): string[] {
  return addedTags.map(tag => {
    if (typeof tag === 'string') {
      return tag;
    }

    return tag.id;
  });
}
