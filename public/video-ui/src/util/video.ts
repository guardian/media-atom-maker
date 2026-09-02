import moment from 'moment';

import { getStore } from './storeAccessor';
import PrivacyStates from '../constants/privacyStates';
import { VideoPlayerFormat } from '../constants/videoCreateOptions';
import { Asset, MediaAtomSummary, Video } from '../services/VideosApi';

export default class VideoUtils {
  static hasAssets({ assets }: Pick<Video, 'assets'>) {
    return assets.length > 0;
  }

  static getActiveAsset({
    assets,
    activeVersion
  }: Pick<Video, 'assets' | 'activeVersion'>) {
    if (activeVersion) {
      const active = assets.filter((_: Asset) => _.version === activeVersion);
      return active.length === 1 ? active[0] : active;
    }
  }

  static getYoutubeChannel({ channelId }: Pick<Video, 'channelId'>) {
    if (!channelId) {
      return false;
    }

    const state = getStore().getState();
    const stateChannels = state.youtube.channels;
    return stateChannels.find(_ => _.id === channelId);
  }

  static hasYoutubeWriteAccess({
    channelId,
    privacyStatus
  }: Pick<Video, 'channelId' | 'privacyStatus'>) {
    const availablePrivacyStates = VideoUtils.getAvailablePrivacyStates({
      channelId
    });

    if (
      !!privacyStatus &&
      availablePrivacyStates &&
      !availablePrivacyStates.includes(privacyStatus as string)
    ) {
      return false;
    }
    return !!VideoUtils.getYoutubeChannel({ channelId });
  }

  static getAvailableChannels({ category }: Pick<Video, 'category'>) {
    const state = getStore().getState();
    const stateChannels = state.youtube.channels;
    const isCommercialType = VideoUtils.isCommercialType({ category });
    return stateChannels.filter(_ => _.isCommercial === isCommercialType);
  }

  static getAvailablePrivacyStates({ channelId }: Pick<Video, 'channelId'>) {
    const channel = VideoUtils.getYoutubeChannel({ channelId });
    return channel ? channel.privacyStates : PrivacyStates.defaultStates;
  }

  static isCommercialType({ category }: Pick<Video, 'category'>) {
    return ['Hosted', 'Paid'].includes(category as string);
  }

  static isLiveStream({ category }: Pick<Video, 'category'>) {
    return category === 'Livestream';
  }

  static isHosted({ category }: Pick<Video, 'category'>) {
    return category === 'Hosted';
  }

  static isEligibleForAds(
    atom: Pick<Video, 'assets' | 'category' | 'duration'>
  ) {
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
    return (
      atom.duration != null &&
      atom.duration > 0 &&
      atom.duration >= minDurationForAds
    );
  }

  static canUploadToYouTube({
    youtubeCategoryId,
    channelId,
    privacyStatus
  }: Pick<Video, 'youtubeCategoryId' | 'channelId' | 'privacyStatus'>) {
    return !!youtubeCategoryId && !!channelId && !!privacyStatus;
  }

  static getScheduledLaunch({
    contentChangeDetails
  }: Pick<Video, 'contentChangeDetails'>) {
    return (
      contentChangeDetails &&
      contentChangeDetails.scheduledLaunch &&
      contentChangeDetails.scheduledLaunch.date
    );
  }

  static getEmbargo({
    contentChangeDetails
  }: Pick<Video, 'contentChangeDetails'>) {
    return (
      contentChangeDetails &&
      contentChangeDetails.embargo &&
      contentChangeDetails.embargo.date
    );
  }

  static getScheduledLaunchAsDate(video: Pick<Video, 'contentChangeDetails'>) {
    const scheduledLaunch = VideoUtils.getScheduledLaunch(video);
    return scheduledLaunch ? moment(scheduledLaunch) : null;
  }

  static getEmbargoAsDate(video: Pick<Video, 'contentChangeDetails'>) {
    const embargo = VideoUtils.getEmbargo(video);
    return embargo ? moment(embargo) : null;
  }

  static isPublished({
    contentChangeDetails
  }: Pick<Video, 'contentChangeDetails'>) {
    return !!contentChangeDetails.published;
  }

  static hasExpired({
    contentChangeDetails
  }: Pick<Video, 'contentChangeDetails'>) {
    return (
      !!contentChangeDetails.expiry &&
      contentChangeDetails.expiry.date <= Date.now()
    );
  }

  static getPlatformFromAtom(atom: Pick<Video, 'platform'>) {
    return atom?.platform?.toLowerCase() || null;
  }

  static getPlatformFromSummary(atomSummary: MediaAtomSummary) {
    return atomSummary?.platform?.toLowerCase() || null;
  }

  static canHaveComposerPage(atom: Pick<Video, 'videoPlayerFormat'>) {
    return (
      atom.videoPlayerFormat !== 'Cinemagraph' &&
      atom.videoPlayerFormat !== 'Loop'
    );
  }

  static mustHaveTags(atom: Pick<Video, 'videoPlayerFormat'>) {
    return atom.videoPlayerFormat === 'Default';
  }
}
