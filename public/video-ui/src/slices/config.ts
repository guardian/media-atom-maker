import { Action, createSlice } from '@reduxjs/toolkit';

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
  embeddedMode?: string;
  tagManagerUrl?: string;
};

export type ConfigState = AppConfig;

type ConfigRecievedAction = Action<'config/setConfig'> & {
  payload: AppConfig;
};

const initialState: ConfigState = { permissions: {} };

const config = createSlice({
  name: 'config',
  initialState,
  reducers: {
    setConfig(state: ConfigState, action: ConfigRecievedAction) {
      Object.assign(state, action.payload);
    }
  }
});

export default config.reducer;

export const { setConfig } = config.actions;
