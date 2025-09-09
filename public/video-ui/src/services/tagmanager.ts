import { apiRequest } from './apiRequest';
import { getStore } from '../util/storeAccessor';

export function getTagsByType(tagManagerUrl: string, query: string, types: string[]) {
  return apiRequest({
    url: `${tagManagerUrl}/hyper/tags?query=${query}&limit=150&type=${types.join(",")}`
  });
}

