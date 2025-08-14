// import {createSlice} from '@reduxjs/toolkit'
//
// const initialState: { categories: unknown[], channels: unknown[] } = { categories: [], channels: [] }
//
// const youtubeSlice = createSlice({
//   name: 'youtube',
//   initialState,
//   reducers: {
//     categoriesReceived: (state, action) => {
//       state.categories = action.payload;
//     },
//     channelsReceived: (state, action) => {
//       state.channels = action.payload;
//     },
//   },
// })
//
// export const { categoriesReceived, channelsReceived } = youtubeSlice.actions
//
// export default youtubeSlice.reducer


import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import {YouTubeChannelWithData, YouTubeVideoCategory} from "../services/YoutubeApi";

export const youtubeApi = createApi({
  baseQuery: fetchBaseQuery({
    // Fill in your own server starting URL here
    baseUrl: '/api/youtube',
  }),
  endpoints: (build) => ({
    getYoutubeCategories: build.query<YouTubeVideoCategory[], void>({
      query: () => '/categories',
    }),
    getYoutubeChannels: build.query<YouTubeChannelWithData[], void>({
      query: () => '/channels',
    }),
  }),
})

export const { useGetYoutubeCategoriesQuery, useGetYoutubeChannelsQuery } = youtubeApi
