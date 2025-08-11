import { PlutoCommission, PlutoProject } from "./services/PlutoApi";
import { Video } from "./services/VideosApi";
import { YouTubeChannelWithData, YouTubeVideoCategory } from "./services/YoutubeApi";

type ActionBase<Type extends string> = {
    type: Type,
    receivedAt: number,
}

type ShowError = ActionBase<'SHOW_ERROR'> & {
    message: string,
    error: unknown
}

type PlutoCommissionsGetRequest = ActionBase<'PLUTO_COMMISSIONS_GET_REQUEST'>

type PlutoCommissionsGetReceive = ActionBase<'PLUTO_COMMISSIONS_GET_RECEIVE'> & {
    commissions: PlutoCommission[]
}

type PlutoProjectGetRequest = ActionBase<'PLUTO_PROJECTS_GET_REQUEST'>

type PlutoProjectsGetReceive = ActionBase<'PLUTO_PROJECTS_GET_RECEIVE'> & {
    projects: PlutoProject[],
}

type AddProjectRequest = ActionBase<'ADD_PROJECT_REQUEST'> & {
}

type AddProjectReceive = ActionBase<'ADD_PROJECT_RECEIVE'> & {
    video: Video,
}

type UpdateSearchTerm = ActionBase<'UPDATE_SEARCH_TERM'> & {
    searchTerm: string;
}

type UpdateShouldUseCreatedDateForSort = ActionBase<'UPDATE_SHOULD_USE_CREATED_DATE_FOR_SORT'> & {
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
