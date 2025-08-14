import { createSlice } from '@reduxjs/toolkit';
import { Video } from '../services/VideosApi';
import { isAddProjectAction} from '../actions/actions';


const initialState: Video[] = [];

const plutoVideos = createSlice({
  name: 'plutoVideos',
  initialState,
  reducers: {},
  extraReducers(builder) {
    builder.addMatcher(isAddProjectAction, (state, action) => {
      action.video.plutoData.projectId
        ? state.filter(video => video.id !== action.video.id)
        : state.map(v => (v.id === action.video.id ? action.video : v));
    });
  }
});

export default plutoVideos.reducer;
