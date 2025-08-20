import { createSlice, Action } from "@reduxjs/toolkit";

export type AppConfig = {
    permissions: Record<string, boolean>;
    presence?: string;
    youtubeEmbedUrl?: string;
    youtubeThumbnailUrl?: string;
    reauthUrl?: string;
    gridUrl?: string;
    capiProxyUrl?: string;
    liveCapiProxyUrl?: string;
    composerUrl?: string;
    ravenUrl?: string;
    stage?: string;
    viewerUrl?: string;
    minDurationForAds?: string;
    isTrainingMode?: string;
    workflowUrl?: string;
    targetingUrl?: string;
    embeddedMode?: unknown; // TO DO - look at how this is set
}

export type ConfigState = {
    config: AppConfig
};


type ConfigRecievedAction = Action<"config/setConfig"> & {
    payload: AppConfig
}

const initialState: ConfigState = { config: { permissions: {} } };


const config = createSlice({
    name: 'config',
    initialState,
    reducers: {
        setConfig(state: ConfigState, action: ConfigRecievedAction) {
            state.config = action.payload;
        }
    }
});

export default config.reducer;

export const { setConfig } = config.actions;