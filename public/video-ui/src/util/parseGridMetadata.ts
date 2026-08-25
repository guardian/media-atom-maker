import { getGridMediaId } from './getGridMediaId';
import { getTextFromHtml } from './getTextFromHtml';
import type { Image, ImageAsset } from '../services/VideosApi';

type GridAssetInput = {
  secureUrl: string;
  mimeType: string;
  size: number;
  dimensions: {
    width: number;
    height: number;
  };
};

type ParsedAsset = ImageAsset & {
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

type ParsedImage = Image & {
  assets: ParsedAsset[];
  master: ParsedAsset;
  source: string;
};

type ComposerAsset = {
  assetType: 'image';
  mimeType: string;
  url: string;
  fields: {
    width?: string;
    height?: string;
    aspectRatio?: string;
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
    source?: string;
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
  image: Image,
  trail: string
): ComposerImageData {
  const mediaId = getGridMediaId(image);

  if (!mediaId) {
    throw new Error('Could not derive Grid media ID from parsed image');
  }

  function getComposerAsset(asset: ImageAsset): ComposerAsset | undefined {
    if (asset.mimeType) {
      return {
        assetType: 'image',
        mimeType: asset.mimeType,
        url: asset.file,
        fields: {
          width: asset.dimensions?.width.toString(),
          height: asset.dimensions?.height.toString(),
          aspectRatio: asset.aspectRatio
        }
      };
    } else {
      return undefined;
    }
  }

  function getComposerMasterAsset(
    asset: ImageAsset
  ): ComposerAsset | undefined {
    const composerAsset = getComposerAsset(asset);
    if (composerAsset) {
      return {
        ...composerAsset,
        isMaster: 'true'
      };
    } else {
      return undefined;
    }
  }

  const alt = getTextFromHtml(trail);

  return {
    assets: [image.master ? getComposerMasterAsset(image.master) : undefined]
      .concat(image.assets.map(getComposerAsset))
      .filter((asset): asset is ComposerAsset => asset !== undefined),
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
