import TagTypes from '../constants/TagTypes';

export default function getTagDisplayNames(tags, tagType) {
  return tags.map(tag => {
    if (tagType === TagTypes.keyword) {
      let detailedTitle;

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

      return { id: tag.id, webTitle: detailedTitle };
    }
    return { id: tag.id, webTitle: tag.webTitle };
  });

}
