import { Dispatch } from "redux";
import VideosApi, { PathSyncCheckReport } from "../../services/VideosApi";


const requestComposerPathReport = (composerId: string) => ({
    type: 'REQUEST_COMPOSER_PATH_REPORT',
    receivedAt: Date.now(),
    composerId
});

const receiveComposerPathReport = (composerId: string, pathSyncCheckReport: PathSyncCheckReport) => ({
    type: 'RECEIVE_COMPOSER_PATH_REPORT',
    receivedAt: Date.now(),
    pathSyncCheckReport,
    composerId
});

const errorReceivingComposerPathReport = (error: unknown) => {
    return {
        type: 'SHOW_ERROR',
        message: 'Could not check Composer page path',
        error: error,
        receivedAt: Date.now()
    };
};

export function fetchComposerPathReport(composerId: string) {
    return async (dispatch: Dispatch) => {
        dispatch(requestComposerPathReport(composerId));
        try {
            const report = await VideosApi.fetchComposerPathReport(composerId);
            dispatch(receiveComposerPathReport(composerId, report));
        } catch (err) {
            dispatch(errorReceivingComposerPathReport(err));
        }
    };
}