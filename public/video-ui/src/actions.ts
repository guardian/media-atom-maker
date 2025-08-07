import { PlutoCommission, PlutoProject } from "./services/PlutoApi";
import { Video } from "./services/VideosApi";


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

type KnownAction =
    ShowError |
    PlutoCommissionsGetRequest |
    PlutoCommissionsGetReceive |
    PlutoProjectGetRequest |
    PlutoProjectsGetReceive |
    AddProjectRequest |
    AddProjectReceive;

export { KnownAction };