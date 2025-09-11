import React, { useEffect } from 'react';
import { useStore } from 'react-redux';
import { PathSyncCheckReport } from '../../services/VideosApi';

interface Props {
    fetchComposerPathReport: { (composerId: string): void }
}


export function getComposerId(usage: any) {
    try {
        const usages = usage?.data;
        const videoPages = [...usages.preview.video, ...usages.published.video];
        if (videoPages.length !== 0) {
            return videoPages[0].fields.internalComposerCode;
        }
        return undefined;
    } catch (err) {
        return undefined;
    }
}


export const ComposerPathChecker = ({ fetchComposerPathReport }: Props) => {

    const store = useStore();
    const composerId = getComposerId(store.getState().usage);
    const report = store.getState().usage.pathSyncCheckReport as undefined | PathSyncCheckReport;

    const handleButton = () => {
        if (!composerId) {
            return;
        }
        fetchComposerPathReport(composerId);
    };

    useEffect(() => {
        if (!composerId) {
            return;
        }
        fetchComposerPathReport(composerId);
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