declare global {
  interface Window { guardian: {csrf: {token: string }}; }
}
export {};
