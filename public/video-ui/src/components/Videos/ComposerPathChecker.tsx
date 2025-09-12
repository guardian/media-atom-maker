import React, { useEffect } from 'react';
import { useStore } from 'react-redux';
import { UsageData } from '../../reducers/usageReducer';

interface Props {
    fetchComposerPathReport: { (composerId: string): void }
}


function getComposerId(usage: UsageData): string | undefined {
    const [firstContent] = [...usage.data.preview.video, ...usage.data.published.video];
    if (firstContent) {
        return firstContent.fields.internalComposerCode;
    }
    return undefined;
}


export const ComposerPathChecker = ({ fetchComposerPathReport }: Props) => {

    const store = useStore();
    const composerId = getComposerId(store.getState().usage);
    const report = store.getState().usage.pathSyncCheckReports[composerId];

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