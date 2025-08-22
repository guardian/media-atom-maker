type BaseAction<TypeName extends string> = {
    type: TypeName;
    receivedAt: number;
}

type ShowError = BaseAction<'SHOW_ERROR'> & {
    message: string,
    error: unknown
}


type KnownAction = ShowError

export { KnownAction };
