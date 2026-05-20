import { z } from 'zod';
import { QUERY_PARAM_supportTagging } from '../constants/queryParams';

const PresenceSchema = z.object({
  domain: z.string(),
  firstName: z.string(),
  lastName: z.string(),
  email: z.string()
});

const PermissionsSchema = z.object({
  deleteAtom: z.boolean().default(false),
  setVideosOnAllChannelsPublic: z.boolean().default(false),
  pinboard: z.boolean().default(false)
});

export const ClientConfigSchema = z.object({
  presence: PresenceSchema.optional(),
  youtubeEmbedUrl: z.string(),
  youtubeThumbnailUrl: z.string(),
  reauthUrl: z.string(),
  gridUrl: z.string(),
  capiProxyUrl: z.string(),
  liveCapiProxyUrl: z.string(),
  composerUrl: z.string(),
  ravenUrl: z.string(),
  stage: z.string(),
  viewerUrl: z.string(),
  permissions: PermissionsSchema,
  minDurationForAds: z.number(),
  isTrainingMode: z.boolean(),
  workflowUrl: z.string(),
  targetingUrl: z.string(),
  tagManagerUrl: z.string()
});

export type ClientConfig = z.infer<typeof ClientConfigSchema>;

export function extractConfigFromPage(): ClientConfig {
  const configEl = document.getElementById('config');

  if (!configEl) {
    throw new Error('Config element not found in page');
  }

  return ClientConfigSchema.parse(JSON.parse(configEl.innerHTML));
}

export function extractEmbeddedModeFromUrl(): boolean {
  const maybeEmbeddedMode = new URLSearchParams(location.search).get(
    'embeddedMode'
  );
  if (!maybeEmbeddedMode || maybeEmbeddedMode === 'false') {
    return false;
  }
  return true;
}

export type ConfigState = ClientConfig & { embeddedMode: boolean };

export function getAppConfig(): ConfigState {
  const isTest = !!process.env.JEST_WORKER_ID;

  if (isTest) {
    return {
      youtubeEmbedUrl: '',
      youtubeThumbnailUrl: '',
      reauthUrl: '',
      gridUrl: '',
      capiProxyUrl: '',
      liveCapiProxyUrl: '',
      composerUrl: '',
      ravenUrl: '',
      stage: 'TEST',
      viewerUrl: '',
      permissions: {
        deleteAtom: false,
        setVideosOnAllChannelsPublic: false,
        pinboard: false
      },
      minDurationForAds: 0,
      isTrainingMode: false,
      workflowUrl: '',
      targetingUrl: '',
      tagManagerUrl: '',
      embeddedMode: false
    };
  }

  const config = extractConfigFromPage();
  const embeddedMode = extractEmbeddedModeFromUrl();
  return { ...config, embeddedMode };
}

export function getUserTelemetryClient(stage: string): string {
  switch (stage) {
    case 'CODE':
      return 'https://user-telemetry.code.dev-gutools.co.uk';
    case 'PROD':
      return 'https://user-telemetry.gutools.co.uk';
    default:
      return 'https://user-telemetry.local.dev-gutools.co.uk';
  }
}

export function isTaggingSupported(): boolean {
  const url = new URL(window.location.href);
  return url.searchParams.has(QUERY_PARAM_supportTagging);
}
