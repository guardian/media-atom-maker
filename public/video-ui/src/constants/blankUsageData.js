import ContentApi from '../services/capi';

export const blankUsageData = {
  data: {
    [ContentApi.preview]: {
      video: [],
      other: []
    },
    [ContentApi.published]: {
      video: [],
      other: []
    }
  },

  totalUsages: 0,
  totalVideoPages: 0
};
