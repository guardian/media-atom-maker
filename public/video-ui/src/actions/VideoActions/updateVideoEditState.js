export function updateVideoEditState(editState) {
  return {
    type: 'VIDEO_EDIT_STATE_REQUEST',
    editState: editState,
    receivedAt: Date.now()
  };
}
