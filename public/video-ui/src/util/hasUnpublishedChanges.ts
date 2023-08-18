import _ from 'lodash';
import {appUpdatedFields} from '../constants/appUpdatedFields';
import {imageFields} from '../constants/imageFields';
import {Video} from "../services/VideosApi";

export function hasUnpublishedChanges(
  previewVideo: Video,
  publishedVideo: Video
) {
  if (!previewVideo) {
    return false;
  }

  if (!publishedVideo) {
    return true;
  }

  return previewVideo.contentChangeDetails.revision > publishedVideo.contentChangeDetails.revision;
}
