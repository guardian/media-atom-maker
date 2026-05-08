import type { Image, ImageAsset } from '../services/VideosApi';

export function findAssetToUseAsThumbnail(image: Image) {
  const { assets, master } = image;
  /**
   * The partition function treats missing widths as below the threshold.
   * In practice, we aren't expecting to have assets without widths, but
   * it feels reasonable to put them in the "below threshold" bucket
   * because we're giving the 'smallest asset above or equal to 250px'
   * higher priority than the 'largest asset below 250px', and we probably
   * wouldn't want to prioritise an asset with an unknown width.
   */
  const { aboveOrEqualToThreshold, belowThreshold } = partitionByWidth(
    assets,
    250
  );

  if (aboveOrEqualToThreshold.length > 0) {
    return findSmallestAsset(aboveOrEqualToThreshold);
  }
  if (belowThreshold.length > 0) {
    return findLargestAsset(belowThreshold);
  }
  return master;
}

function sortAssetsAscendingBySize(assetsArray: ImageAsset[]): ImageAsset[] {
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
  return assetsBySize;
}

function findSmallestAsset(assetsArray: ImageAsset[]): ImageAsset | undefined {
  const assetsBySize = sortAssetsAscendingBySize(assetsArray);

  if (assetsBySize.length === 0) {
    return undefined;
  }

  return assetsBySize[0];
}

function findLargestAsset(assetsArray: ImageAsset[]): ImageAsset | undefined {
  const assetsBySize = sortAssetsAscendingBySize(assetsArray);

  if (assetsBySize.length === 0) {
    return undefined;
  }

  return assetsBySize[assetsBySize.length - 1];
}

function partitionByWidth(assetsArray: ImageAsset[], widthThreshold: number) {
  const aboveOrEqualToThreshold: ImageAsset[] = [];
  const belowThreshold: ImageAsset[] = [];

  for (const asset of assetsArray) {
    if (asset.dimensions?.width && asset.dimensions.width >= widthThreshold) {
      aboveOrEqualToThreshold.push(asset);
    } else {
      belowThreshold.push(asset);
    }
  }

  return { aboveOrEqualToThreshold, belowThreshold };
}
