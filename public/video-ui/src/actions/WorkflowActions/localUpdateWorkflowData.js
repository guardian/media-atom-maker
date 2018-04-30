//NOTE: THIS DOESN'T SAVE THE VIDEO, ONLY UPDATES THE CLIENT STATE. USE saveVideo TO SAVE
export function localUpdateWorkflowData(status) {
  return {
    type: 'WORKFLOW_VIDEO_UPDATE_REQUEST',
    receivedAt: Date.now(),
    status: status
  };
}
