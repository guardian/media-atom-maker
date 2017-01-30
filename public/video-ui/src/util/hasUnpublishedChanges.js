import _ from 'lodash';

export function hasUnpublishedChanges(previewVideo, publishedVideo) {
  if (!previewVideo) {
    return false;
  }

  if (!publishedVideo) {
    return true;
  }

  const propertiesToCheck = ['title', 'category', 'expiryDate', 'legallySensitive', 'posterImage', 'youtubeCategoryId', 'privacyStatus', 'tags', 'activeVersion'];

  return !propertiesToCheck.every(key => {
    return _.isEqual(previewVideo[key], publishedVideo[key]);
  });
}
