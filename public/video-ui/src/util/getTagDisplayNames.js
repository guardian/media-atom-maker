import TagTypes from '../constants/TagTypes';

export default function getTagDisplayNames(tags) {
  return tags.map(tag => {
    if (typeof tag === 'string') {
      return tag;
    }

    const tagType = tag.type;
    const tagForDisplay = { id: tag.id, webTitle: tag.webTitle };

    if (tagType === TagTypes.keyword) {
      //Some webtitles on keyword tags are too unspecific and we need to add
      //the section name to them to know what tags they are referring to
      tagForDisplay.detailedTitle = tag.webTitle !== tag.sectionName
        ? `${tag.webTitle} (${tag.sectionName})`
        : tag.webTitle;
    } else {
      const appendTagTypes = [
        TagTypes.series,
        TagTypes.tone,
        TagTypes.commercial
      ];

      tagForDisplay.detailedTitle = appendTagTypes.includes(tagType)
        ? `${tag.webTitle} (${tagType})`
        : tag.webTitle;
    }

    return tagForDisplay;
  });
}
