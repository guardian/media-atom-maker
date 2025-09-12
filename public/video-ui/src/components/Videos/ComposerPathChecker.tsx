import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { UsageData } from '../../reducers/usageReducer';
import { ComposerPathPathState, fetchComposerPathReport } from '../../slices/composerPagePaths';
import { RootState } from '../../util/setupStore';
import { CapiContent } from '../../services/capi';


function getCapiContent(usage: UsageData): CapiContent | undefined {
    const [firstContent] = [...usage.data.preview.video, ...usage.data.published.video];
    return firstContent;
}


export const ComposerPathChecker = () => {
    const dispatch = useDispatch();
    const maybeCapiContent = useSelector<RootState, CapiContent>(({ usage }) => getCapiContent(usage));
    const composerPathPathState = useSelector<RootState, ComposerPathPathState>(({ composerPagePaths }) => composerPagePaths);
    const composerId = maybeCapiContent?.fields.internalComposerCode;
    const {
        updateRequired = false,
        previousPath = undefined,
        proposedPath = undefined
    } = composerPathPathState.pathSyncCheckReports[composerId] ?? {};

    useEffect(() => {
        if (!composerId) {
            return;
        }
        dispatch(fetchComposerPathReport(composerId));
    }, [composerId]);

    if (!composerId) {
        return null;
    }

    const handleButton = () => {
        if (!composerId) {
            return;
        }
        dispatch(fetchComposerPathReport(composerId));
    };

    const indicatorClassName = updateRequired ? 'topbar__path-sync-indicator topbar__path-sync-indicator--out-of-sync' : 'topbar__path-sync-indicator';

    return (
        <div className="flex-container topbar__path-sync" >
            <button className="btn"
                title={previousPath && `current path: ${previousPath}`}
                disabled={!composerId}
                onClick={handleButton}>
                check for path changes
            </button>

            <div className={indicatorClassName}
                title={updateRequired ? `update to: ${proposedPath}` : undefined}
            >
                {updateRequired ? 'out of sync' : 'path ok'}
            </div>
        </div>
    );

};