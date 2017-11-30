import _ from 'lodash';
import { appUpdatedFields } from '../constants/appUpdatedFields';
import { imageFields } from '../constants/imageFields';

export function hasUnpublishedChanges(
  previewVideo,
  publishedVideo,
  editableFields
) {
  if (!previewVideo) {
    return false;
  }

  if (!publishedVideo) {
    return true;
  }

  const allFields = [...editableFields, ...appUpdatedFields, ...imageFields];

  return !allFields.every(key => {
    return _.isEqual(previewVideo[key], publishedVideo[key]);
  });
}
