import moment from 'moment';

import { getStore } from './storeAccessor';
import PrivacyStates from '../constants/privacyStates';

export default class VideoUtils {
  static hasAssets({ assets }) {
    return assets.length > 0;
  }

  static getActiveAsset({ assets, activeVersion }) {
    if (activeVersion) {
      const active = assets.filter(_ => _.version === activeVersion);
      return active.length === 1 ? active[0] : active;
    }
  }

  static isYoutube(atom) {
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

  static getYoutubeChannel({ channelId }) {
    if (!channelId) {
      return false;
    }

    const state = getStore().getState();
    const stateChannels = state.youtube.channels;
    return stateChannels.find(_ => _.id === channelId);
  }

  static hasYoutubeWriteAccess({ channelId, privacyStatus }) {
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

  static getAvailableChannels({ category }) {
    const state = getStore().getState();
    const stateChannels = state.youtube.channels;
    const isCommercialType = VideoUtils.isCommercialType({ category });
    return stateChannels.filter(_ => _.isCommercial === isCommercialType);
  }

  static getAvailablePrivacyStates({ channelId }) {
    const channel = VideoUtils.getYoutubeChannel({ channelId });
    return channel ? channel.privacyStates : PrivacyStates.defaultStates;
  }

  static isCommercialType({ category }) {
    return ['Hosted', 'Paid'].includes(category);
  }

  static isEligibleForAds(atom) {
    if (VideoUtils.isCommercialType(atom)) {
      return false;
    }

    if (!VideoUtils.hasAssets(atom)) {
      return true;
    }

    const minDurationForAds = getStore().getState().config.minDurationForAds;
    return atom.duration > 0 && atom.duration > minDurationForAds;
  }

  static isRecentlyModified({ contentChangeDetails }) {
    if (contentChangeDetails && contentChangeDetails.lastModified) {
      const lastModified = moment(contentChangeDetails.lastModified.date);
      const diff = moment().diff(lastModified, 'days');
      return diff < 1;
    }
    return false;
  }

  static getScheduledLaunch({ contentChangeDetails }) {
    const scheduledLaunch = contentChangeDetails.scheduledLaunch;
    return scheduledLaunch ? moment(scheduledLaunch.date) : null;
  }

  static canUploadToYouTube({ youtubeCategoryId, channelId, privacyStatus }) {
    return !!youtubeCategoryId && !!channelId && !!privacyStatus;
  }

  static getEmbargo({ contentChangeDetails }) {
    const embargo = contentChangeDetails.embargo;
    return embargo ? moment(embargo.date) : null;
  }

  static isPublished({contentChangeDetails}) {
    return !!contentChangeDetails.published;
  }

  static hasExpired({contentChangeDetails}) {
    return !!contentChangeDetails.expiry && contentChangeDetails.expiry.date <= Date.now();
  }
}
