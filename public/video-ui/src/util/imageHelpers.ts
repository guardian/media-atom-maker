import type { ImageAsset } from '../services/VideosApi';

function findSmallestAsset(assetsArray: ImageAsset[]): ImageAsset | undefined {
  /**
   * Array.prototype.sort mutates the original array, so we create a copy of the array before sorting to avoid side effects.
   * `toSorted` is non-mutating but is only available in more recent versions of JS.
   * Given that this is running in the browser and we aren't polyfilling (afaik), it makes sense to
   * copy and mutate instead of using `toSorted` and potentially breaking older browsers.
   */
  const assetsBySize = [...assetsArray].sort((a, b) => {
    const aSize = a.size ?? Number.POSITIVE_INFINITY;
    const bSize = b.size ?? Number.POSITIVE_INFINITY;
    return aSize - bSize;
  });

  if (assetsBySize.length === 0) {
    return undefined;
  }

  return assetsBySize[0];
}

export function findSmallestAssetAboveWidth(
  assetsArray: ImageAsset[],
  minSize = 250
) {
  // Grid provides various versions of a crop
  // their widths are fixed and typically 140, 500, 1000, 2000px
  // use the first one that's above `minSize` in width
  // as the resolution is usually good enough for a simple preview
  const usefulAssets = assetsArray.filter(
    asset => asset.dimensions?.width && asset.dimensions.width > minSize
  );
  return findSmallestAsset(usefulAssets);
}
