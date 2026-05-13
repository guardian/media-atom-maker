// @ts-check
import { ImageAsset } from '../services/VideosApi';
import { findAssetToUseAsThumbnail } from './imageHelpers';

describe('findAssetToUseAsThumbnail', () => {
  it('returns the smallest asset above or equal to250px if one exists', () => {
    const image = {
      mediaId: 'image1',
      assets: [
        {
          file: 'asset1.jpg',
          size: 1000,
          dimensions: { width: 200, height: 100 }
        },
        {
          file: 'asset2.jpg',
          size: 500,
          dimensions: { width: 300, height: 150 }
        },
        {
          file: 'asset3.jpg',
          size: 200,
          dimensions: { width: 400, height: 200 }
        }
      ]
    };
    const result = findAssetToUseAsThumbnail(image);
    expect(result).toEqual({
      file: 'asset3.jpg',
      size: 200,
      dimensions: { width: 400, height: 200 }
    });
  });
  it('returns the largest asset if all assets are below 250px', () => {
    const image = {
      mediaId: 'image1',
      assets: [
        {
          file: 'asset1.jpg',
          size: 1000,
          dimensions: { width: 200, height: 100 }
        },
        {
          file: 'asset2.jpg',
          size: 500,
          dimensions: { width: 150, height: 75 }
        }
      ]
    };
    const result = findAssetToUseAsThumbnail(image);
    expect(result).toEqual({
      file: 'asset1.jpg',
      size: 1000,
      dimensions: { width: 200, height: 100 }
    });
  });

  it('returns the master image if there are no assets', () => {
    const image = {
      mediaId: 'image1',
      master: {
        file: 'master.jpg'
      },
      assets: [] as ImageAsset[]
    };
    const result = findAssetToUseAsThumbnail(image);
    expect(result).toEqual({
      file: 'master.jpg'
    });
  });

  it('returns undefined if there are no assets and no master image', () => {
    const image = {
      mediaId: 'image1',
      assets: [] as ImageAsset[]
    };
    const result = findAssetToUseAsThumbnail(image);
    expect(result).toBeUndefined();
  });
});
