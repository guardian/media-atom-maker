import {
  getAspectRatioFromImage,
  getAspectRatioFromVideo,
  isImageCropOutOfSync
} from '../util/getAspectRatio';

import {
  audioAsset1,
  emptyImage,
  emptyVideo,
  imageWithAspectRatio5by3,
  imageWithAspectRatio5by4,
  imageWithDefaultVideoAspectRatio,
  videoAsset1,
  videoAsset2,
  videoWithActiveAsset5by4
} from './models';

describe('getAspectRatioFromVideo', () => {
  it('should return undefined if no activeVersion is present', () => {
    expect(getAspectRatioFromVideo(emptyVideo)).toEqual(undefined);
  });
  it('should return undefined if there are no video assets', () => {
    const video = {
      ...emptyVideo,
      activeVersion: 1,
      assets: [audioAsset1]
    };
    expect(getAspectRatioFromVideo(video)).toEqual(undefined);
  });
  it('should return undefined if there is a not an aspectRatio on the active video asset', () => {
    const video = {
      ...emptyVideo,
      activeVersion: 1,
      assets: [videoAsset1, videoAsset2]
    };
    expect(getAspectRatioFromVideo(video)).toEqual(undefined);
  });
  it('should return the aspect ratio of the asset with the version that matches the active version', () => {
    const video = {
      ...emptyVideo,
      activeVersion: 2,
      assets: [
        {
          ...videoAsset1,
          aspectRatio: '5:4'
        },
        { ...videoAsset2, aspectRatio: '7:3' }
      ]
    };
    expect(getAspectRatioFromVideo(video)).toEqual('7:3');
  });
  it('should return the aspect ratio of the first asset if multiple video assets match the active version', () => {
    const video = {
      ...emptyVideo,
      activeVersion: 1,
      assets: [
        {
          ...videoAsset1,
          aspectRatio: '5:4'
        },
        { ...videoAsset1, aspectRatio: '7:3' }
      ]
    };
    expect(getAspectRatioFromVideo(video)).toEqual('5:4');
  });
});

describe('getAspectRatioFromImage', () => {
  it('should return undefined if there is no master image', () => {
    expect(getAspectRatioFromImage(emptyImage)).toEqual(undefined);
  });
  it('should the aspect ratio of the master image', () => {
    expect(getAspectRatioFromImage(imageWithAspectRatio5by3)).toEqual('5:3');
  });
});

describe('isImageCropOutOfSync', () => {
  it('should return false if there is no master image set', () => {
    expect(isImageCropOutOfSync(videoWithActiveAsset5by4)).toBe(false);
  });
  it('should return false if there is no master image set on poster image', () => {
    expect(
      isImageCropOutOfSync({
        ...videoWithActiveAsset5by4,
        posterImage: emptyImage
      })
    ).toBe(false);
  });
  it('should return false if no video active image is set and the image is set to the default video value', () => {
    expect(
      isImageCropOutOfSync({
        ...emptyVideo,
        posterImage: imageWithDefaultVideoAspectRatio
      })
    ).toBe(false);
  });
  it('should return true if no video active image is set and the image is not set to the default video value', () => {
    expect(
      isImageCropOutOfSync({
        ...emptyVideo,
        posterImage: imageWithAspectRatio5by3
      })
    ).toBe(true);
  });
  it('should return false if a video active image is set and the image is set to the same value', () => {
    expect(
      isImageCropOutOfSync({
        ...videoWithActiveAsset5by4,
        posterImage: imageWithAspectRatio5by4
      })
    ).toBe(false);
  });
  it('should return true if a video active image is set and the image is not set to the same value', () => {
    expect(
      isImageCropOutOfSync({
        ...videoWithActiveAsset5by4,
        posterImage: imageWithAspectRatio5by3
      })
    ).toBe(true);
  });
});
