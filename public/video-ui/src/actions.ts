import { PlutoCommission, PlutoProject } from "./services/PlutoApi";
import { Video } from "./services/VideosApi";
import { YouTubeChannelWithData, YouTubeVideoCategory } from "./services/YoutubeApi";

type ActionBase<Type extends string> = {
    type: Type,
    receivedAt: number,
}

type ShowError = {
    type: 'SHOW_ERROR',
    message: string,
    receivedAt: number,
    error: unknown
}

type PlutoCommissionsGetRequest = {
    type: 'PLUTO_COMMISSIONS_GET_REQUEST',
    receivedAt: number,
}

type PlutoCommissionsGetReceive = {
    type: 'PLUTO_COMMISSIONS_GET_RECEIVE',
    receivedAt: number,
    commissions: PlutoCommission[]
}

type PlutoProjectGetRequest = {
    type: 'PLUTO_PROJECTS_GET_REQUEST',
    receivedAt: number,
}

type PlutoProjectsGetReceive = {
    type: 'PLUTO_PROJECTS_GET_RECEIVE',
    receivedAt: number,
    projects: PlutoProject[],
}

type AddProjectRequest = {
    type: 'ADD_PROJECT_REQUEST',
    receivedAt: number,
}

type AddProjectReceive = {
    type: 'ADD_PROJECT_RECEIVE',
    receivedAt: number,
    video: Video,
}

type UpdateSearchTerm = {
    type: 'UPDATE_SEARCH_TERM';
    searchTerm: string;
    receivedAt: number;
}

type UpdateShouldUseCreatedDateForSort = {
    type: 'UPDATE_SHOULD_USE_CREATED_DATE_FOR_SORT';
    receivedAt: number;
    shouldUseCreatedDateForSort: boolean;
}

type YtChannelsGetRequest = ActionBase<'YT_CHANNELS_GET_REQUEST'>
type YtChannelsGetReceive = ActionBase<'YT_CHANNELS_GET_RECEIVE'> & {
    channels: YouTubeChannelWithData[];
}

type YtCategoriesGetRequest = ActionBase<"YT_CATEGORIES_GET_REQUEST">
type YtCategoriesGetReceive = ActionBase<"YT_CATEGORIES_GET_RECEIVE"> & {
    categories: YouTubeVideoCategory[]
}

/**
 * A union of the Action types that dispatched from ts tiles.
 * 
 * This is not yet a complete list of all the actions the application handles as
 * some actions dispatched from js files.
 */
type KnownAction =
    ShowError |
    PlutoCommissionsGetRequest |
    PlutoCommissionsGetReceive |
    PlutoProjectGetRequest |
    PlutoProjectsGetReceive |
    AddProjectRequest |
    AddProjectReceive |
    UpdateSearchTerm | 
    UpdateShouldUseCreatedDateForSort | 
    YtChannelsGetRequest | 
    YtChannelsGetReceive | 
    YtCategoriesGetRequest | 
    YtCategoriesGetReceive;

export { KnownAction };
