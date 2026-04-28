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

  it('throws an error if no assets are above the default width of 250px', () => {
    const assets = [
      {
        file: 'asset1.jpg',
        size: 1000,
        dimensions: { width: 200, height: 100 }
      },
      { file: 'asset2.jpg', size: 500, dimensions: { width: 150, height: 75 } }
    ];
    expect(() => findSmallestAssetAboveWidth(assets)).toThrow();
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

  it('throws an error if no assets are above a custom width', () => {
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
    expect(() => findSmallestAssetAboveWidth(assets, 350)).toThrow();
  });

  describe('assets with missing dimensions', () => {
    it('throws an error if any of the assets lack valid dimensions', () => {
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
      expect(() => findSmallestAssetAboveWidth(assets)).toThrow();
    });
  });

  describe('assets with missing size', () => {
    it('returns the smallest of the assets with valid size, if the first asset above the minimum width has a valid size', () => {
      const assets = [
        {
          file: 'asset1.jpg',
          dimensions: { width: 200, height: 100 }
        },
        {
          file: 'asset3.jpg',
          size: 200,
          dimensions: { width: 400, height: 200 }
        },
        {
          file: 'asset2.jpg',
          dimensions: { width: 300, height: 150 }
        },
        {
          file: 'asset4.jpg',
          size: 100,
          dimensions: { width: 400, height: 200 }
        }
      ];
      const result = findSmallestAssetAboveWidth(assets);
      expect(result).toEqual({
        file: 'asset4.jpg',
        size: 100,
        dimensions: { width: 400, height: 200 }
      });
    });

    it('but returns an image without a valid size if it is the *first* asset above the minimum width, in the list', () => {
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
        },
        {
          file: 'asset4.jpg',
          size: 100,
          dimensions: { width: 400, height: 200 }
        }
      ];
      const result = findSmallestAssetAboveWidth(assets);
      expect(result).toEqual({
        file: 'asset2.jpg',
        dimensions: { width: 300, height: 150 }
      });
    });
  });

  it('throws if it finds assets with missing dimensions and size', () => {
    const assets = [
      {
        file: 'asset1.jpg',
        dimensions: { width: 200, height: 100 }
      },
      { file: 'asset2.jpg' },
      { file: 'asset3.jpg', size: 200, dimensions: { width: 400, height: 200 } }
    ];
    expect(() => findSmallestAssetAboveWidth(assets)).toThrow();
  });

  it('throws an error for an empty array', () => {
    expect(() => findSmallestAssetAboveWidth([])).toThrow();
  });

  /**
   * This case is describing the existing functionality; as far as I'm aware it's not a normative requirement.
   */
  it('excludes assets with width exactly equal to the minimum width (and throws)', () => {
    const assets = [
      { file: 'asset1.jpg', size: 100, dimensions: { width: 250, height: 125 } }
    ];
    expect(() => findSmallestAssetAboveWidth(assets)).toThrow();
  });
});
