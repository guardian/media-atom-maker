import moment from 'moment';

import { getStore } from './storeAccessor';
import PrivacyStates from '../constants/privacyStates';

export default class VideoUtils {
  static hasAssets({ assets }: any) {
    return assets.length > 0;
  }

  static getActiveAsset({ assets, activeVersion }: any) {
    if (activeVersion) {
      const active = assets.filter(
        (_: { version: any }) => _.version === activeVersion
      );
      return active.length === 1 ? active[0] : active;
    }
  }

  static getYoutubeChannel({ channelId }: any) {
    if (!channelId) {
      return false;
    }

    const state = getStore().getState();
    const stateChannels = state.youtube.channels;
    return stateChannels.find(_ => _.id === channelId);
  }

  static hasYoutubeWriteAccess({ channelId, privacyStatus }: any) {
    const availablePrivacyStates = VideoUtils.getAvailablePrivacyStates({
      channelId
    });

    if (
      !!privacyStatus &&
      availablePrivacyStates &&
      !availablePrivacyStates.includes(privacyStatus)
    ) {
      return false;
    }
    return !!VideoUtils.getYoutubeChannel({ channelId });
  }

  static getAvailableChannels({ category }: any) {
    const state = getStore().getState();
    const stateChannels = state.youtube.channels;
    const isCommercialType = VideoUtils.isCommercialType({ category });
    return stateChannels.filter(_ => _.isCommercial === isCommercialType);
  }

  static getAvailablePrivacyStates({ channelId }: any) {
    const channel = VideoUtils.getYoutubeChannel({ channelId });
    return channel ? channel.privacyStates : PrivacyStates.defaultStates;
  }

  static isCommercialType({ category }: any) {
    return ['Hosted', 'Paid'].includes(category);
  }

  static isLiveStream({ category }: any) {
    return category === 'Livestream';
  }

  static isHosted({ category }: any) {
    return category === 'Hosted';
  }

  static isEligibleForAds(atom: any) {
    if (!VideoUtils.hasAssets(atom)) {
      return true;
    }

    if (VideoUtils.isCommercialType(atom)) {
      return true;
    }

    if (VideoUtils.isLiveStream(atom)) {
      return true;
    }

    const minDurationForAds = getStore().getState().config.minDurationForAds;
    return atom.duration > 0 && atom.duration >= minDurationForAds;
  }

  static canUploadToYouTube({
    youtubeCategoryId,
    channelId,
    privacyStatus
  }: any) {
    return !!youtubeCategoryId && !!channelId && !!privacyStatus;
  }

  static getScheduledLaunch({ contentChangeDetails }: any) {
    return (
      contentChangeDetails &&
      contentChangeDetails.scheduledLaunch &&
      contentChangeDetails.scheduledLaunch.date
    );
  }

  static getEmbargo({ contentChangeDetails }: any) {
    return (
      contentChangeDetails &&
      contentChangeDetails.embargo &&
      contentChangeDetails.embargo.date
    );
  }

  static getScheduledLaunchAsDate(video: any) {
    const scheduledLaunch = VideoUtils.getScheduledLaunch(video);
    return scheduledLaunch ? moment(scheduledLaunch) : null;
  }

  static getEmbargoAsDate(video: any) {
    const embargo = VideoUtils.getEmbargo(video);
    return embargo ? moment(embargo) : null;
  }

  static isPublished({ contentChangeDetails }: any) {
    return !!contentChangeDetails.published;
  }

  static hasExpired({ contentChangeDetails }: any) {
    return (
      !!contentChangeDetails.expiry &&
      contentChangeDetails.expiry.date <= Date.now()
    );
  }

  // @ts-expect-error TS(2304): Cannot find name 'ContentChangeDetails'.
  static getPlatformFromAtom(atom: {
    id?: string;
    type?: string;
    labels?: string[];
    contentChangeDetails?: ContentChangeDetails;
    assets?: Asset[];
    activeVersion?: number;
    title?: string;
    category?: unknown;
    plutoData?: PlutoData;
    iconikData?: IconikData;
    duration?: number;
    source?: string;
    description?: string;
    trailText?: string;
    posterImage?: Image;
    trailImage?: unknown;
    youtubeOverrideImage?: unknown;
    tags?: string[];
    byline?: string[];
    commissioningDesks?: string[];
    atomTagIds?: string[];
    keywords?: string[];
    youtubeCategoryId?: string;
    license?: string;
    channelId?: string;
    legallySensitive?: boolean;
    sensitive?: boolean;
    privacyStatus?: unknown;
    expiryDate?: number;
    youtubeTitle?: string;
    youtubeDescription?: string;
    blockAds?: boolean;
    composerCommentsEnabled?: boolean;
    optimisedForWeb?: boolean;
    suppressRelatedContent?: boolean;
    videoPlayerFormat?: VideoPlayerFormat;
    platform: any;
  }) {
    return atom?.platform?.toLowerCase() || null;
  }

  static getPlatformFromSummary(atomSummary: { platform: string }) {
    return atomSummary?.platform?.toLowerCase() || null;
  }

  static canHaveComposerPage(atom: any) {
    return (
      atom.videoPlayerFormat !== 'Cinemagraph' &&
      atom.videoPlayerFormat !== 'Loop'
    );
  }

  static mustHaveTags(atom: any) {
    return atom.videoPlayerFormat === 'Default';
  }
}
