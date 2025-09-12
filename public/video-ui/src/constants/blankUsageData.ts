import type { UsageData } from "../reducers/usageReducer";


export const blankUsageData: UsageData = {
  data: {
    published: {
      video: [],
      other: []
    },
    preview: {
      video: [],
      other: []
    }
  },

  totalUsages: 0,
  totalVideoPages: 0
};
