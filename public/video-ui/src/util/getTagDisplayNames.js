import TagTypes from '../constants/TagTypes';

// Logic from before CAPI returned internal names for tags
function getLegacyDetailedTitle(tag) {
  const tagType = tag.type;

  if (tagType === TagTypes.keyword) {
    //Some webtitles on keyword tags are too unspecific and we need to add
    //the section name to them to know what tags they are referring to
    return tag.webTitle !== tag.sectionName
      ? `${tag.webTitle} (${tag.sectionName})`
      : tag.webTitle;
  } else {
    const appendTagTypes = [
      TagTypes.series,
      TagTypes.tone,
      TagTypes.commercial
    ];

    return appendTagTypes.includes(tagType)
      ? `${tag.webTitle} (${tagType})`
      : tag.webTitle;
  }
}

export default function getTagDisplayNames(tags) {
  return tags.map(tag => {
    if (typeof tag === 'string') {
      return tag;
    }

    return {
      id: tag.id,
      webTitle: tag.webTitle,
      detailedTitle: tag.internalName ? tag.internalName : getLegacyDetailedTitle(tag)
    };
  });
}
