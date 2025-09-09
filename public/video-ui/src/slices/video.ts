import {Video} from "../services/VideosApi";
import {blankVideoData} from "../constants/blankVideoData";
import {createSlice, Action} from "@reduxjs/toolkit";

const initialState: null | boolean | Video = null;

interface VideoAction extends Action {
  video?: Video;
}

const isVideoAction = (action: Action): action is VideoAction => {
  return ["VIDEO_GET_RECEIVE", "VIDEO_CREATE_RECEIVE", "VIDEO_UPDATE_REQUEST", "VIDEO_SAVE_REQUEST", "VIDEO_SAVE_RECEIVE", "ASSET_DELETE_RECEIVE"].includes(action.type);
}

const video = createSlice({
  name: 'video',
  initialState,
  reducers: {},
  extraReducers: builder => {
    builder
      .addCase('VIDEO_POPULATE_BLANK', _ => {
        return {...blankVideoData, type: "media"}
      })
      .addMatcher(isVideoAction, (_, { video }) => {
        if(video){
          return {...blankVideoData, ...video}
        }
        else {
          return false;
        }

      })
  }
});

export default video.reducer;
