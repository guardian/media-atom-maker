import { browserHistory } from 'react-router';
import VideosApi from '../../services/VideosApi';

const BLANK_ASSET = {
  uri: '',
  mimetype: '',
  version: 0
};

function requestAssetCreate() {
  return {
    type:       'ASSET_CREATE_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveAssetCreate(video) {
  browserHistory.push('/video/videos/' + video.id );
  return {
    type: 'ASSET_CREATE_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };

}

function errorAssetCreate(error) {
  return {
    type:       'SHOW_ERROR',
    message:    `Could not create asset. ${error}`,
    error:      error,
    receivedAt: Date.now()
  };
}

export function createAsset(asset, videoId) {
  return dispatch => {
    dispatch(requestAssetCreate());
    return VideosApi.createAsset(asset, videoId)
        .then(res => dispatch(receiveAssetCreate(res)))
        .catch(error => dispatch(errorAssetCreate(error.response)));
  };
}

export function populateEmptyAsset() {
  return {
    type:        'ASSET_POPULATE_BLANK',
    asset:       Object.assign({}, BLANK_ASSET),
    receivedAt:  Date.now()
  };
}
