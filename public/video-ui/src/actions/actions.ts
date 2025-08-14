import { PlutoCommission, PlutoProject } from "../services/PlutoApi";
import { Video } from "../services/VideosApi";
import { Action } from "@reduxjs/toolkit";

type BaseAction<TypeName extends string> = {
    type: TypeName;
    receivedAt: number;
}

type ShowError = BaseAction<'SHOW_ERROR'> & {
    message: string,
    error: unknown
}

type PlutoCommissionsGetRequest = BaseAction<'PLUTO_COMMISSIONS_GET_REQUEST'>

type PlutoCommissionsGetReceive = BaseAction<'PLUTO_COMMISSIONS_GET_RECEIVE'> & {
    commissions: PlutoCommission[]
}

type PlutoProjectGetRequest = BaseAction<'PLUTO_PROJECTS_GET_REQUEST'>

type PlutoProjectsGetReceive = BaseAction<'PLUTO_PROJECTS_GET_RECEIVE'> & {
    projects: PlutoProject[],
}

type AddProjectRequest = BaseAction<'ADD_PROJECT_REQUEST'> & {
}

type AddProjectReceive = BaseAction<'ADD_PROJECT_RECEIVE'> & {
    video: Video,
}

export const isAddProjectAction = (action: Action): action is AddProjectReceive => {
  return action.type === 'ADD_PROJECT_RECEIVE';
}


/**
 * A union of the Action types that dispatched from ts files.
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
    AddProjectReceive;

export { KnownAction };
