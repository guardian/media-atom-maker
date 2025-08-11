type PresenceConfig = {
    domain: string,
    email: string,
    firstName: string,
    lastName: string
}

type Presence = {
    clientId: {
        connId: string,
        person: {
            browserId: string,
            email: string,
            firstName: string,
            googleId: string,
            lastName: string
        },
        lastAction: string, // ISO 8601 timestamp
        location: string // Always seems to be "document"
    },
}

type PresenceData = {
    subscriptionId?: string,
    currentState?: Presence[],
    subscribedTo: PresenceData[],
    data?: PresenceData
}

type PresenceClient = {
    on: (string: string, f: (data?: PresenceData) => void) => void;
    subscribe: (mediaIds: string[]) => void;
    startConnection: () => void;
}

declare global {
    interface Window { presenceClient?: { (endpoint: string, config: Omit<PresenceConfig, 'domain'>): PresenceClient }; }
}

const safelyStartPresence = (
    callback: { (presenceClient: PresenceClient): void },
    reportPresenceClientError: { (err: unknown): void },
    { domain, firstName, lastName, email }: PresenceConfig
) => {
    const endpoint = `wss://${domain}/socket`;
    if (!window.presenceClient) {
        console.error("Failed to connect to Presence as client was not available in window.");
        return;
    }

    try {
        const presenceClient = window.presenceClient(endpoint, {
            firstName,
            lastName,
            email
        });
        callback(presenceClient);

    } catch (err) {
        console.error('Failed to instantiate Presence client', err);
        reportPresenceClientError(err);
    }
};

export { safelyStartPresence, Presence, PresenceData, PresenceConfig, PresenceClient };