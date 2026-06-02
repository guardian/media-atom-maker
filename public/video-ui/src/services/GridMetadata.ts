import { apiRequest } from './apiRequest';

export type CropOption = {
    key: string;
    ratio: string;
    ratioString: string
}

export function getCropOptions() {
  return apiRequest<CropOption[]>({
    url: '/api/grid/crop-variations'
  });
}