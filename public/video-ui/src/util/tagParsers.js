import ContentApi from '../services/capi';
import TagTypes from '../constants/TagTypes';

export function tagsFromStringList(savedTags, tagType) {
  if (!savedTags) {
    Promise.resolve([]);
  }

  return Promise.all(
    savedTags.map(element => {
      if (
        (tagType !== TagTypes.contributor && tagType !== TagTypes.youtube) ||
        element.match('^profile/')
      ) {
        return ContentApi.getLivePage(element)
          .then(({ response }) => response.tag);
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

export function tagsToStringList(addedTags) {
  return addedTags.map(tag => {
    if (typeof tag === 'string') {
      return tag;
    } else return tag.id;
  });
}
