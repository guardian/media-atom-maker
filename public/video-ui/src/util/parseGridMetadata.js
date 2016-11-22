function parseMimeType(mimeType) {

  //Normalise Mime Types coming from the grid.
  switch(mimeType) {
    case "jpg":
      return "image/jpeg";
    case "png":
      return "image/png";
  }

  return mimeType;
}


function parseAsset(asset) {
  return {
    file: asset.secureUrl,
    mimeType: parseMimeType(asset.mimeType),
    size: asset.size,
    dimensions: {
      width: asset.dimensions.width,
      height: asset.dimensions.height
    }
  }
}

export function parseImageFromGridCrop(cropData) {
  return {
    assets: cropData.assets.map(parseAsset),
    master: parseAsset(cropData.master),
    mediaId: cropData.specification.uri
  }
}
