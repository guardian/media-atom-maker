import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { UsageData } from '../../reducers/usageReducer';
import { CapiContent } from '../../services/capi';
import { ComposerPathPathState, fetchComposerPathReport } from '../../slices/composerPagePaths';
import { RootState } from '../../util/setupStore';


function getCapiContent({ usage }: { usage: UsageData }): CapiContent | undefined {
    const [firstContent] = [...usage.data.preview.video, ...usage.data.published.video];
    return firstContent;
}


export const ComposerPathChecker = () => {
    const dispatch = useDispatch();
    const maybeCapiContent = useSelector<RootState, CapiContent>(getCapiContent);
    const composerId = maybeCapiContent?.fields.internalComposerCode;

    const composerPathPathState = useSelector<RootState, ComposerPathPathState>(({ composerPagePaths }) => composerPagePaths);
    const {
        updateRequired = false,
        previousPath: composerPath = undefined
    } = composerPathPathState.pathSyncCheckReports[composerId] ?? {};
    const isPending = composerPathPathState.pendingChecks.includes(composerId);

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
        if (!composerId || isPending) {
            return;
        }
        dispatch(fetchComposerPathReport(composerId));
    };

    const buttonClassName = isPending ? "btn btn--loading" : "btn btn--space-for-loading";

    return (
        <div>
            <div className="details-list">
                <p className="details-list__title">Composer Path:</p>
                <p className="details-list__field">{composerPath}</p>
            </div>
            <div className="flex-container path-status">
                <button className={buttonClassName}
                    disabled={!composerId || isPending}
                    onClick={handleButton}>
                    check for changes
                </button>

                {updateRequired && (
                    <>
                        <div className="path-status__indicator path-status__indicator--out-of-sync">!</div>
                        <div className="path-status__warning">
                            <span>The Composer page for this video needs its path updating.</span>
                        </div>
                    </>
                )}
            </div>
        </div>
    );

};