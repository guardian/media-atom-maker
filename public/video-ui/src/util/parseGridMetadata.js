function parseMimeType(mimeType) {
  //Normalise Mime Types coming from the grid.
  switch (mimeType) {
    case 'jpg':
      return 'image/jpeg';
    case 'png':
      return 'image/png';
  }

  return mimeType;
}

function parseAsset(asset, aspectRatio) {
  console.log('parsing asset with ratio ', aspectRatio);
  console.log('asset is ', asset);
  return {
    file: asset.secureUrl,
    mimeType: parseMimeType(asset.mimeType),
    size: asset.size,
    aspectRatio: aspectRatio,
    dimensions: {
      width: asset.dimensions.width,
      height: asset.dimensions.height,
    }
  };
};

export function parseImageFromGridCrop(cropData, imageData) {
  console.log('**** parsing image from grid crop ****');
  const aspectRatio = cropData.specification.aspectRatio;
  console.log('aspect ratio ', aspectRatio);
  return {
    assets: cropData.assets.map((asset) => parseAsset(asset, aspectRatio)),
    master: parseAsset(cropData.master, aspectRatio),
    mediaId: cropData.specification.uri,
    source: imageData.data.metadata.credit
  };
}

export function parseComposerDataFromImage(image, trail) {
  console.log('parse data from image');
  console.log('image ', image);

  const urlParts = image.mediaId.split('/');
  const mediaId = urlParts[urlParts.length - 1];

  function getComposerAsset(asset) {
    return {
      assetType: 'image',
      mimeType: asset.mimeType,
      url: asset.file,
      fields: {
        width: asset.dimensions.width.toString(),
        height: asset.dimensions.height.toString(),
        aspectRatio: asset.aspectRatio
      }
    };
  }

  function getComposerMasterAsset(asset) {
    const composerAsset = getComposerAsset(asset);
    composerAsset.isMaster = 'true';
    return composerAsset;
  }

  console.log('trail is ', trail.innerHtml);
  return {
    assets: [ getComposerMasterAsset(image.master) ]
    .concat(
      image.assets.map(getComposerAsset)
    ),
    fields: {
      alt: trail ? trail.innerHtml : null,
      imageType: 'Photograph',
      isMandatory: true,
      mediaApiUrl: image.mediaId,
      mediaId: mediaId,
      source: image.source
    }
  };
}
