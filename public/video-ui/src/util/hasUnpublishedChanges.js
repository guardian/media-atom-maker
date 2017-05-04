import _ from 'lodash';
import { appUpdatedFields } from '../constants/appUpdatedFields';

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

  const allFields = editableFields.concat(appUpdatedFields);

  return !allFields.every(key => {
    return _.isEqual(previewVideo[key], publishedVideo[key]);
  });
}
