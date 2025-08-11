import moment from 'moment';

import { getStore } from './storeAccessor';
import PrivacyStates from '../constants/privacyStates';
import { Video } from '../services/VideosApi';
import { YouTubeChannelWithData } from '../services/YoutubeApi';

export default class VideoUtils {
  static hasAssets({ assets }: Video) {
    return assets.length > 0;
  }

  static getActiveAsset({ assets, activeVersion }: Video) {
    if (activeVersion) {
      const active = assets.filter(_ => _.version === activeVersion);
      return active.length === 1 ? active[0] : active;
    }
  }

  static isYoutube(atom: Video) {
    // no assets, could be youtube if we wanted
    if (!VideoUtils.hasAssets(atom)) {
      return true;
    }

    const activeAsset = VideoUtils.getActiveAsset(atom);

    // not possible to have multiple assets w/same version for youtube
    if (Array.isArray(activeAsset)) {
      return false;
    }

    // no active assets, could be youtube if we wanted
    if (!activeAsset) {
      return true;
    }

    return activeAsset.platform === 'Youtube';
  }

  static getYoutubeChannel({ channelId }: Video) {
    if (!channelId) {
      return false;
    }

    const state = getStore().getState();
    const stateChannels = state.youtube.channels as YouTubeChannelWithData[];
    return stateChannels.find(_ => _.id === channelId);
  }

  static hasYoutubeWriteAccess(video: Video) {
    const { privacyStatus } = video;
    const availablePrivacyStates = VideoUtils.getAvailablePrivacyStates(video);

    if (
      !!privacyStatus &&
      availablePrivacyStates &&
      !availablePrivacyStates.includes(privacyStatus as string)
    ) {
      return false;
    }
    return !!VideoUtils.getYoutubeChannel(video);
  }

  static getAvailableChannels(video: Video) {
    const state = getStore().getState();
    const stateChannels = state.youtube.channels as YouTubeChannelWithData[];
    const isCommercialType = VideoUtils.isCommercialType(video);
    return stateChannels.filter(_ => _.isCommercial === isCommercialType);
  }

  static getAvailablePrivacyStates(video: Video) {
    const channel = VideoUtils.getYoutubeChannel(video);
    return channel ? channel.privacyStates : PrivacyStates.defaultStates;
  }

  static isCommercialType({ category }: Video) {
    return (['Hosted', 'Paid'] as unknown[]).includes(category);
  }

  static isLiveStream({ category }: Video) {
    return category === 'Livestream';
  }

  static isHosted({ category }: Video) {
    return category === 'Hosted';
  }

  static isEligibleForAds(atom: Video) {
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

  static isRecentlyModified({ contentChangeDetails }: Video) {
    if (contentChangeDetails && contentChangeDetails.lastModified) {
      const lastModified = moment(contentChangeDetails.lastModified.date);
      const diff = moment().diff(lastModified, 'days');
      return diff < 1;
    }
    return false;
  }

  static canUploadToYouTube({ youtubeCategoryId, channelId, privacyStatus }: Video) {
    return !!youtubeCategoryId && !!channelId && !!privacyStatus;
  }

  static getScheduledLaunch({ contentChangeDetails }: Video) {
    return contentChangeDetails &&
      contentChangeDetails.scheduledLaunch &&
      contentChangeDetails.scheduledLaunch.date;
  }

  static getEmbargo({ contentChangeDetails }:Video) {
    return contentChangeDetails &&
      contentChangeDetails.embargo &&
      contentChangeDetails.embargo.date;
  }

  static getScheduledLaunchAsDate(video:Video) {
    const scheduledLaunch = VideoUtils.getScheduledLaunch(video);
    return scheduledLaunch ? moment(scheduledLaunch) : null;
  }

  static getEmbargoAsDate(video:Video) {
    const embargo = VideoUtils.getEmbargo(video);
    return embargo ? moment(embargo) : null;
  }

  static isPublished({ contentChangeDetails }:Video) {
    return !!contentChangeDetails.published;
  }

  static hasExpired({ contentChangeDetails }: Video) {
    return !!contentChangeDetails.expiry && contentChangeDetails.expiry.date <= Date.now();
  }
}
