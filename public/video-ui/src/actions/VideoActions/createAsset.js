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

function recieveAssetCreate(asset, videoId) {
  browserHistory.push('/video/videos/' + video.id + '/asset');
  return {
    type: 'ASSET_CREATE_RECEIVE',
    asset: asset,
    receivedAt: Date.now()
  };

}

function errorAssetCreate(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not create asset',
    error:      error,
    receivedAt: Date.now()
  };
}

export function createAsset(asset) {
  return dispatch => {
    dispatch(requestAssetCreate());
    return VideosApi.createAsset(asset)
        .then(res => dispatch(recieveAssetCreate(res)))
        .fail(error => dispatch(errorAssetCreate(error)));
  };
}

export function populateEmptyAsset() {
  return {
    type:        'ASSET_POPULATE_BLANK',
    asset:       Object.assign({}, BLANK_ASSET),
    receivedAt:  Date.now()
  };
}
