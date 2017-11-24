import { blankVideoData } from '../constants/blankVideoData';

export default function video(state = null, action) {
  switch (action.type) {
    case 'VIDEO_GET_RECEIVE':
      if (action.video) {
        let video = Object.assign({}, blankVideoData, action.video);
        /*
        this is necessary to make sure that if the old expiryDate field already exists in the db,
        the new expiry value is initialised to the saved one. This won't be necessary
        once we remove the old field
        */
        const newExpiryDate =
          video.contentChangeDetails.expiry &&
          video.contentChangeDetails.expiry.date;
        if (!newExpiryDate && video.expiryDate) {
          video = Object.assign({}, video, {
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
        }
        return video;
      } else {
        return false;
      }

    case 'VIDEO_CREATE_RECEIVE':
      return action.video
        ? Object.assign({}, blankVideoData, action.video)
        : false;

    case 'VIDEO_UPDATE_REQUEST':
      return Object.assign({}, blankVideoData, action.video);

    case 'VIDEO_SAVE_REQUEST': {
      const video = Object.assign({}, blankVideoData, action.video);
      const newExpiryDate = video.contentChangeDetails.expiry.date;
      //keeps old and new expiry fields synced. This is temporary, until we remove the old expiryDate field
      if (newExpiryDate && video.expiryDate !== newExpiryDate) {
        video.expiryDate = newExpiryDate;
      }
      return video;
    }

    case 'VIDEO_POPULATE_BLANK':
      return Object.assign({}, blankVideoData, {
        type: 'media'
      });

    case 'ASSET_REVERT_REQUEST':
      return Object.assign({}, state, {
        activeVersion: action.assetVersion
      });

    case 'ASSET_CREATE_RECEIVE':
      return Object.assign({}, state, {
        assets: action.video.assets
      });

    default:
      return state;
  }
}
