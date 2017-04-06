import VideosApi from '../../services/VideosApi';

const BLANK_ASSET = {
  uri: ''
};

function requestAssetCreate(video) {
  return {
    type:       'ASSET_CREATE_REQUEST',
    video:      video,
    receivedAt: Date.now()
  };
}

function receiveAssetCreate(video) {
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

export function createAsset(asset, video) {
  return dispatch => {
    dispatch(requestAssetCreate(video));
    return VideosApi.createAsset(asset, video.id)
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
