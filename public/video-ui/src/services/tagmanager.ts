import { apiRequest } from './apiRequest';
import { getStore } from '../util/storeAccessor';

export default class TagManager {

  static get tagManagerUrl() {
    return getStore().getState().config.tagManagerUrl;
  }

  static getTagsByType(query: string, types: string[]) {
      return apiRequest({
        url: `${TagManager.tagManagerUrl}/hyper/tags?query=${query}&limit=150&type=${types.join(",")}`
      });
    }
}
