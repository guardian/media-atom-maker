declare module "panda-session" {
    const reEstablishSession:{(loginUrl:string, maxWait:number):Promise<void>};
    export {reEstablishSession};
}
