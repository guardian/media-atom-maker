import { apiRequest } from './apiRequest';
import { getStore } from '../util/storeAccessor';

export type Section = {
  id: number;
  name: string;
};

export type Tag = {
  id: number;
  path: string;
  type: string;
  internalName: string;
  externalName: string;
  deprecated: boolean;
  section: Section;
  subType?: string;
};

export type TagManagerItem = {
  data: Tag;
};

export type TagManagerApiResponse = {
  data: TagManagerItem[];
};

export function getTagsByType(
  tagManagerUrl: string,
  query: string,
  types: string[],
  subType?: string
): Promise<TagManagerApiResponse> {
  const subTypeQuery = subType ? `&subType=${subType}` : '';
  return apiRequest<TagManagerApiResponse>({
    url: `${tagManagerUrl}/hyper/tags?query=${query}&limit=150&type=${types.join(',')}${subTypeQuery}`
  });
}

export function getTagByPath(
  tagManagerUrl: string,
  path: string
): Promise<Tag | undefined> {
  const apiResponse = apiRequest<TagManagerApiResponse>({
    url: `${tagManagerUrl}/hyper/tags?query=${path}&limit=150&searchField=path`
  });
  return apiResponse.then(
    response => response.data.find(item => item.data.path === path)?.data
  );
}
