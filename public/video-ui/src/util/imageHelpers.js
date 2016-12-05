export function findSmallestAsset(assetsArray) {
  return assetsArray.reduce((smallestAsset, newAsset) => {
    if (newAsset.size < smallestAsset.size) {
      return newAsset
    } else {
      return smallestAsset
    }
  });
}

export function findSmallestAssetAboveHeight(assetsArray, minSize) {
  const usefulAssets = assetsArray.filter((asset) => asset.dimensions.height >= minSize)
  return findSmallestAsset(usefulAssets);
}
