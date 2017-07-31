import { getGridMediaId } from './getGridMediaId';

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
  return {
    file: asset.secureUrl,
    mimeType: parseMimeType(asset.mimeType),
    size: asset.size,
    aspectRatio: aspectRatio,
    dimensions: {
      width: asset.dimensions.width,
      height: asset.dimensions.height
    }
  };
}

export function parseImageFromGridCrop(cropData, imageData) {
  const aspectRatio = cropData.specification.aspectRatio;
  return {
    assets: cropData.assets.map(asset => parseAsset(asset, aspectRatio)),
    master: parseAsset(cropData.master, aspectRatio),
    mediaId: cropData.specification.uri,
    source: imageData.data.metadata.credit
  };
}

export function parseComposerDataFromImage(image, trail) {
  const mediaId = getGridMediaId(image);

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

  const tempDiv = document.createElement('div');
  tempDiv.innerHTML = trail;
  const alt = tempDiv.innerText;

  return {
    assets: [getComposerMasterAsset(image.master)].concat(
      image.assets.map(getComposerAsset)
    ),
    fields: {
      alt: alt,
      imageType: 'Photograph',
      isMandatory: true,
      mediaApiUrl: image.mediaId,
      mediaId: mediaId,
      source: image.source
    }
  };
}
