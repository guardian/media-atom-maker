import { Dispatch } from "redux";

const getPresenceClientError = (error: unknown) => ({
    type: 'SHOW_ERROR',
    receivedAt: Date.now(),
    message: 'Failed to create start the Presence Client',
    error: error
});


export function reportPresenceClientError(presenceClientError: unknown) {
    return (dispatch: Dispatch) => {
        dispatch(getPresenceClientError(presenceClientError));
    };
}
