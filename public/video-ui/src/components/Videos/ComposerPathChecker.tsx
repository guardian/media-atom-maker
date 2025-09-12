import React, { useEffect } from 'react';
import { useDispatch, useStore } from 'react-redux';
import { UsageData } from '../../reducers/usageReducer';
import { fetchComposerPathReport } from '../../slices/composerPagePaths';


function getComposerId(usage: UsageData): string | undefined {
    const [firstContent] = [...usage.data.preview.video, ...usage.data.published.video];
    if (firstContent) {
        return firstContent.fields.internalComposerCode;
    }
    return undefined;
}


export const ComposerPathChecker = () => {
    const store = useStore();
    const dispatch = useDispatch();
    const composerId = getComposerId(store.getState().usage);
    const report = store.getState().composerPagePaths.pathSyncCheckReports[composerId];

    const handleButton = () => {
        if (!composerId) {
            return;
        }
        dispatch(fetchComposerPathReport(composerId));
    };

    useEffect(() => {
        if (!composerId) {
            return;
        }
        dispatch(fetchComposerPathReport(composerId));
    }, [composerId]);

    return (
        <div>
            <button className="btn"
                disabled={!composerId}
                onClick={handleButton}>
                check for path changes
            </button>
            <p>
                path: {report?.previousPath}
            </p>
            <p>
                id: {composerId}
            </p>
        </div>
    );

};