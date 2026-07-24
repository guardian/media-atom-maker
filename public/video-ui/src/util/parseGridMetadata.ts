import { getGridMediaId } from './getGridMediaId';
import { getTextFromHtml } from './getTextFromHtml';

function parseMimeType(mimeType: any) {
  //Normalise Mime Types coming from the grid.
  switch (mimeType) {
    case 'jpg':
      return 'image/jpeg';
    case 'png':
      return 'image/png';
  }

  return mimeType;
}

function parseAsset(
  asset: {
    secureUrl: any;
    mimeType: any;
    size: any;
    dimensions: { width: any; height: any };
  },
  aspectRatio: any
) {
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

export function parseImageFromGridCrop(
  cropData: {
    specification: { aspectRatio: any; uri: any };
    assets: any[];
    master: any;
  },
  imageData: { data: { metadata: { credit: any } } }
) {
  const aspectRatio = cropData.specification.aspectRatio;
  return {
    assets: cropData.assets.map(asset => parseAsset(asset, aspectRatio)),
    master: parseAsset(cropData.master, aspectRatio),
    mediaId: cropData.specification.uri,
    source: imageData.data.metadata.credit
  };
}

export function parseComposerDataFromImage(
  image: { master: any; assets: any; mediaId: any; source: any },
  trail: string
) {
  const mediaId = getGridMediaId(image);

  function getComposerAsset(asset: {
    mimeType: any;
    file: any;
    dimensions: {
      width: { toString: () => any };
      height: { toString: () => any };
    };
    aspectRatio: any;
  }) {
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

  function getComposerMasterAsset(asset: any) {
    const composerAsset = getComposerAsset(asset);
    (composerAsset as any).isMaster = 'true';
    return composerAsset;
  }

  const alt = getTextFromHtml(trail);

  return {
    assets: [getComposerMasterAsset(image.master)].concat(
      image.assets.map(getComposerAsset)
    ),
    fields: {
      alt: alt,
      imageType: 'Photograph',
      isMandatory: 'true',
      mediaApiUrl: image.mediaId,
      mediaId: mediaId,
      source: image.source
    }
  };
}
