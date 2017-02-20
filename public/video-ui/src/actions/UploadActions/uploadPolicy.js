import UploadsApi from '../../services/UploadsApi';

function requestUploadPolicy() {
  return {
    type: 'UPLOAD_POLICY_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveUploadPolicy(policy, file) {
  return {
    type: 'UPLOAD_POLICY_RECEIVE',
    receivedAt: Date.now(),
    policy: policy,
    file: file
  };
}

function clearUploadPolicyAction() {
  return {
    type: 'UPLOAD_POLICY_CLEAR',
    receivedAt: Date.now()
  };
}

export function getUploadPolicy(atomId, file) {
  return dispatch => {
    dispatch(requestUploadPolicy());

    UploadsApi.getUploadPolicy(atomId).then((policy) => {
      dispatch(receiveUploadPolicy(policy, file));
    });
  };
}

export function clearUploadPolicy() {
  return dispatch => {
    dispatch(clearUploadPolicyAction());
  };
}
