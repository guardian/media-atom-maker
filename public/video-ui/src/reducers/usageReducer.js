import { blankUsageData } from '../constants/blankUsageData';

export default function usage(state = blankUsageData, action) {
  switch (action.type) {
    case 'VIDEO_USAGE_GET_RECEIVE': {
      return action.usages || {};
    }
    case 'VIDEO_PAGE_CREATE_POST_RECEIVE': {
      // TODO avoid mutation... but how on such a deep property?!
      // usages are sorted creation date DESC, new usage goes to the top
      state.data.preview.video = [action.newPage, ...state.data.preview.video];

      return Object.assign({}, state, {
        totalUsages: state.totalUsages + 1,
        totalVideoPages: state.totalVideoPages + 1
      });
    }
    case 'VIDEO_PAGE_UPDATE_POST_RECEIVE': {
      const usages = state.data;

      return Object.keys(usages).reduce(
        (all, publishState) => {
          const updated = usages[publishState].video.map(usage => {
            return Object.assign({}, usage, {
              webTitle: action.newTitle
            });
          });

          // TODO avoid mutation... but how on such a deep property?!
          all.data[publishState] = {
            video: updated,
            other: usages[publishState].other
          };

          return all;
        },
        Object.assign({}, blankUsageData, {
          totalUsages: state.totalUsages,
          totalVideoPages: state.totalVideoPages
        })
      );
    }
    default: {
      return state;
    }
  }
}
