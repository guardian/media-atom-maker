import { Action, createSlice } from '@reduxjs/toolkit';
import { ConfigState, getAppConfig } from '../util/config';

type ConfigRecievedAction = Action<'config/setConfig'> & {
  payload: Partial<ConfigState>;
};

const initialState: ConfigState = getAppConfig();

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
