import { PlutoCommission } from "./services/PlutoApi";


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

type KnownAction = ShowError | PlutoCommissionsGetRequest | PlutoCommissionsGetReceive

export { KnownAction };