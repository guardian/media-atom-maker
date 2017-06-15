import ContentApi from '../services/capi';

export function tagsFromStringList(savedTags, tagType) {
  if (!savedTags) {
    Promise.resolve([]);
  }

  let regex;

  if (tagType === 'contributor') {
    regex = '^profile';
  } else if (tagType === 'tracking') {
    regex = '^tracking';
  } else if (tagType === 'keyword') {
    regex = '^.*/.*/g';
  }

  return Promise.all(
    savedTags.map(element => {
      if (element.match(regex)) {
        return ContentApi.getLivePage(element).then(capiResponse => {
          const tag = capiResponse.response.tag;
          return {
            id: tag.id,
            webTitle: tag.webTitle
          };
        });
      } else {
        return Promise.resolve(element);
      }
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
