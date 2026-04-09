export type Image = {
  mediaId: string,
  source: string,
  size: number,
  dimensions: {
    width: number,
    height: number
  },
  // There are probably more properties
}

type AssetWithDimensions = {
  size: number;
  dimensions: {
    width: number;
    height: number;
  };
};

export function findSmallestAsset<T extends AssetWithDimensions>(assetsArray: T[]): T {
  return assetsArray.reduce((smallestAsset, newAsset) => {
    if (newAsset.size < smallestAsset.size) {
      return newAsset;
    } else {
      return smallestAsset;
    }
  });
}

export function findSmallestAssetAboveWidth<T extends AssetWithDimensions>(assetsArray: T[], minSize = 250): T {
  // Grid provides various versions of a crop
  // their widths are fixed and typically 140, 500, 1000, 2000px
  // use the first one that's above `minSize` in width
  // as the resolution is usually good enough for a simple preview
  const usefulAssets = assetsArray.filter(
    asset => asset.dimensions.width > minSize
  );
  return findSmallestAsset(usefulAssets);
}
