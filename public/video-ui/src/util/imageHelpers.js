export function findSmallestAsset(assetsArray) {
  return assetsArray.reduce((smallestAsset, newAsset) => {
    if (newAsset.size < smallestAsset.size) {
      return newAsset;
    } else {
      return smallestAsset;
    }
  });
}

export function findSmallestAssetAboveWidth(assetsArray, minSize = 250) {
  // Grid provides various versions of a crop
  // their widths are fixed and typically 140, 500, 1000, 2000px
  // use the first one that's above `minSize` in width
  // as the resolution is usually good enough for a simple preview
  const usefulAssets = assetsArray.filter((asset) => asset.dimensions.width > minSize);
  return findSmallestAsset(usefulAssets);
}
