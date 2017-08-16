import TagTypes from '../constants/TagTypes';

export default function getTagDisplayNames(tags) {

  return tags.map(tag => {
    if (typeof tag === 'string') {
      return tag;
    }

    const tagType = tag.type;
    let detailedTitle;

    if (tagType === TagTypes.keyword) {

      //Some webtitles on keyword tags are too unspecific and we need to add
      //the section name to them to know what tags they are referring to

      if (
        tag.webTitle !== tag.sectionName &&
        tag.webTitle.split(' ').length <= 2
      ) {
        detailedTitle = tag.webTitle + ' (' + tag.sectionName + ')';
      } else {
        detailedTitle = tag.webTitle;
      }
    }

    else if (tagType === TagTypes.series) {
      detailedTitle = tag.webTitle + ' (series)';
    }

    else if (tagType === TagTypes.tone) {
      detailedTitle = tag.webTitle + ' (tone)';
    }

    else detailedTitle = tag.webTitle;

    return {
      id: tag.id,
      webTitle: tag.webTitle,
      detailedTitle: detailedTitle
    };
  });

}
