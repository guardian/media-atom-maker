import { getGridMediaId } from './getGridMediaId';
import { getTextFromHtml } from './getTextFromHtml';

type GridAssetInput = {
  secureUrl: string;
  mimeType: string;
  size: number;
  dimensions: {
    width: number;
    height: number;
  };
};

type ParsedAsset = {
  file: string;
  mimeType: string;
  size: number;
  aspectRatio: string;
  dimensions: {
    width: number;
    height: number;
  };
};

type GridCropData = {
  assets: GridAssetInput[];
  master: GridAssetInput;
  specification: {
    aspectRatio: string;
    uri: string;
  };
};

type GridImageData = {
  data: {
    metadata: {
      credit: string;
    };
  };
};

type ParsedImage = {
  assets: ParsedAsset[];
  master: ParsedAsset;
  mediaId: string;
  source: string;
};

type ComposerAsset = {
  assetType: 'image';
  mimeType: string;
  url: string;
  fields: {
    width: string;
    height: string;
    aspectRatio: string;
  };
  isMaster?: 'true';
};

type ComposerImageData = {
  assets: ComposerAsset[];
  fields: {
    alt: string;
    imageType: 'Photograph';
    isMandatory: 'true';
    mediaApiUrl: string;
    mediaId: string;
    source: string;
  };
};

function parseMimeType(mimeType: string): string {
  //Normalise Mime Types coming from the grid.
  switch (mimeType) {
    case 'jpg':
      return 'image/jpeg';
    case 'png':
      return 'image/png';
  }

  return mimeType;
}

function parseAsset(asset: GridAssetInput, aspectRatio: string): ParsedAsset {
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
  cropData: GridCropData,
  imageData: GridImageData
): ParsedImage {
  const aspectRatio = cropData.specification.aspectRatio;
  return {
    assets: cropData.assets.map(asset => parseAsset(asset, aspectRatio)),
    master: parseAsset(cropData.master, aspectRatio),
    mediaId: cropData.specification.uri,
    source: imageData.data.metadata.credit
  };
}

export function parseComposerDataFromImage(
  image: ParsedImage,
  trail: string
): ComposerImageData {
  const mediaId = getGridMediaId(image);

  function getComposerAsset(asset: ParsedAsset): ComposerAsset {
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

  function getComposerMasterAsset(asset: ParsedAsset): ComposerAsset {
    const composerAsset = getComposerAsset(asset);
    composerAsset.isMaster = 'true';
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
