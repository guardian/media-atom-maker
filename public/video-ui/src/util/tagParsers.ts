import ContentApi from '../services/capi';
import TagTypes from '../constants/TagTypes';
import {
  ParsedTag,
  CapiTagResponse,
  CapiTagNotFoundResponse
} from '../types/tags';

export type TagsFromStringListResult = {
  tags: ParsedTag[];
  missingTagIds: string[];
};

const isCapiTagResponse = (value: unknown): value is CapiTagResponse => {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const response = (value as CapiTagResponse).response;
  return Boolean(response?.tag && typeof response.tag.id === 'string');
};

const isCapiTagNotFoundResponse = (
  value: unknown
): value is CapiTagNotFoundResponse => {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const response = (value as CapiTagNotFoundResponse).response;
  return Boolean(
    response?.status === 'error' &&
      response?.message?.includes('could not be found')
  );
};

const fallbackTag = (id: string) => ({ id, webTitle: id });

export function tagsFromStringList(
  savedTags: string[],
  tagType: string
): Promise<TagsFromStringListResult> {
  if (!savedTags) {
    return Promise.resolve({
      tags: [],
      missingTagIds: []
    });
  }

  return Promise.all(
    savedTags.map((element): Promise<{ tag: ParsedTag; sourceId?: string }> => {
      if (
        (tagType !== TagTypes.contributor && tagType !== TagTypes.youtube) ||
        element.match('^profile/')
      ) {
        return ContentApi.getLivePage(element).then(response => {
          if (isCapiTagResponse(response) && response.response?.tag) {
            return {
              tag: response.response.tag
            };
          }

          if (isCapiTagNotFoundResponse(response)) {
            return {
              tag: fallbackTag(element),
              sourceId: element
            };
          }

          return {
            tag: fallbackTag(element)
          };
        });
      }

      if (tagType === TagTypes.youtube) {
        return Promise.resolve({
          tag: fallbackTag(element)
        });
      }

      return Promise.resolve({
        tag: element
      });
    })
  ).then(parsedTags => {
    const missingTagIds = parsedTags
      .map(tag => tag.sourceId)
      .filter((id): id is string => Boolean(id));

    return {
      tags: parsedTags.map(tag => tag.tag),
      missingTagIds
    };
  });
}

export function tagsToStringList(addedTags: ParsedTag[]): string[] {
  return addedTags.map(tag => {
    if (typeof tag === 'string') {
      return tag;
    }

    return tag.id;
  });
}
