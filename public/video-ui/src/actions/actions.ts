import { PlutoCommission, PlutoProject } from "../services/PlutoApi";
import { Video } from "../services/VideosApi";

type BaseAction<TypeName extends string> = {
    type: TypeName;
    receivedAt: number;
}

type ShowError = BaseAction<'SHOW_ERROR'> & {
    message: string,
    error: unknown
}

type AddProjectRequest = BaseAction<'ADD_PROJECT_REQUEST'> & {
}

type AddProjectReceive = BaseAction<'ADD_PROJECT_RECEIVE'> & {
    video: Video,
}

/**
 * A union of the Action types that dispatched from ts files.
 *
 * This is not yet a complete list of all the actions the application handles as
 * some actions dispatched from js files.
 */
type KnownAction =
    ShowError |
    AddProjectRequest |
    AddProjectReceive;

export { KnownAction };
