import { blankVideoData } from '../constants/blankVideoData';

export default function video(state = null, action) {
  switch (action.type) {
    case 'PUBLISHED_VIDEO_GET_RECEIVE':
      if (action.publishedVideo) {
        const video = Object.assign({}, blankVideoData, action.publishedVideo);
        /*
        this is necessary to make sure that if the old expiryDate field already exists in the db,
        the new expiry value is initialised to the saved one. This won't be necessary
        once we remove the old field
        */
        const newExpiryDate =
          action.publishedVideo.contentChangeDetails.expiry &&
          action.publishedVideo.contentChangeDetails.expiry.date;

        if (!newExpiryDate && video.expiryDate) {
          return Object.assign({}, video, {
            contentChangeDetails: Object.assign(
              {},
              video.contentChangeDetails,
              {
                expiry: Object.assign({}, video.contentChangeDetails.expiry, {
                  date: video.expiryDate
                })
              }
            )
          });
        } else {
          return video;
        }
      } else {
        return false;
      }

    case 'VIDEO_PUBLISH_RECEIVE':
      return action.publishedVideo
        ? Object.assign({}, blankVideoData, action.publishedVideo)
        : false;

    default:
      return state;
  }
}
