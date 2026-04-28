// @ts-check
import { findSmallestAssetAboveWidth } from './imageHelpers';

describe('findSmallestAssetAboveWidth', () => {
  it('returns the smallest asset above the default width of 250px', () => {
    const assets = [
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
      { file: 'asset3.jpg', size: 200, dimensions: { width: 400, height: 200 } }
    ];
    const result = findSmallestAssetAboveWidth(assets);
    expect(result).toEqual({
      file: 'asset3.jpg',
      size: 200,
      dimensions: { width: 400, height: 200 }
    });
  });

  it('returns undefined if no assets are above the default width of 250px', () => {
    const assets = [
      {
        file: 'asset1.jpg',
        size: 1000,
        dimensions: { width: 200, height: 100 }
      },
      { file: 'asset2.jpg', size: 500, dimensions: { width: 150, height: 75 } }
    ];
    const result = findSmallestAssetAboveWidth(assets);
    expect(result).toBeUndefined();
  });

  it('returns the smallest asset above a custom width', () => {
    const assets = [
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
      { file: 'asset3.jpg', size: 200, dimensions: { width: 400, height: 200 } }
    ];
    const result = findSmallestAssetAboveWidth(assets, 350);
    expect(result).toEqual({
      file: 'asset3.jpg',
      size: 200,
      dimensions: { width: 400, height: 200 }
    });
  });

  it('returns undefined if no assets are above a custom width', () => {
    const assets = [
      {
        file: 'asset1.jpg',
        size: 1000,
        dimensions: { width: 200, height: 100 }
      },
      {
        file: 'asset2.jpg',
        size: 500,
        dimensions: { width: 300, height: 150 }
      }
    ];
    const result = findSmallestAssetAboveWidth(assets, 350);
    expect(result).toBeUndefined();
  });

  describe('assets with missing dimensions', () => {
    it('returns the smallest of the assets with valid dimensions', () => {
      const assets = [
        {
          file: 'asset1.jpg',
          size: 1000,
          dimensions: { width: 200, height: 100 }
        },
        { file: 'asset2.jpg', size: 500 },
        {
          file: 'asset3.jpg',
          size: 200,
          dimensions: { width: 400, height: 200 }
        }
      ];
      const result = findSmallestAssetAboveWidth(assets);
      expect(result).toEqual({
        file: 'asset3.jpg',
        size: 200,
        dimensions: { width: 400, height: 200 }
      });
    });
    it('returns undefined if no assets have valid dimensions', () => {
      const assets = [
        { file: 'asset1.jpg', size: 1000 },
        { file: 'asset2.jpg', size: 500 }
      ];
      const result = findSmallestAssetAboveWidth(assets);
      expect(result).toBeUndefined();
    });
  });

  describe('assets with missing size', () => {
    it('returns the smallest of the assets with valid size', () => {
      const assets = [
        {
          file: 'asset1.jpg',
          dimensions: { width: 200, height: 100 }
        },
        {
          file: 'asset2.jpg',
          dimensions: { width: 300, height: 150 }
        },
        {
          file: 'asset3.jpg',
          size: 200,
          dimensions: { width: 400, height: 200 }
        }
      ];
      const result = findSmallestAssetAboveWidth(assets);
      expect(result).toEqual({
        file: 'asset3.jpg',
        size: 200,
        dimensions: { width: 400, height: 200 }
      });
    });
    it('returns the first asset if no assets have valid size', () => {
      const assets = [
        {
          file: 'asset1.jpg',
          dimensions: { width: 251, height: 100 }
        },
        {
          file: 'asset2.jpg',
          dimensions: { width: 300, height: 150 }
        }
      ];
      const result = findSmallestAssetAboveWidth(assets);
      expect(result).toEqual({
        file: 'asset1.jpg',
        dimensions: { width: 251, height: 100 }
      });
    });
  });

  it('ignores assets with missing dimensions and size', () => {
    const assets = [
      {
        file: 'asset1.jpg',
        dimensions: { width: 200, height: 100 }
      },
      { file: 'asset2.jpg' },
      { file: 'asset3.jpg', size: 200, dimensions: { width: 400, height: 200 } }
    ];
    const result = findSmallestAssetAboveWidth(assets);
    expect(result).toEqual({
      file: 'asset3.jpg',
      size: 200,
      dimensions: { width: 400, height: 200 }
    });
  });

  it('returns undefined for an empty array', () => {
    const result = findSmallestAssetAboveWidth([]);
    expect(result).toBeUndefined();
  });

  /**
   * This case is describing the existing functionality; as far as I'm aware it's not a normative requirement.
   */
  it('excludes assets with width exactly equal to the minimum width', () => {
    const assets = [
      { file: 'asset1.jpg', size: 100, dimensions: { width: 250, height: 125 } }
    ];
    const result = findSmallestAssetAboveWidth(assets);
    expect(result).toBeUndefined();
  });
});
