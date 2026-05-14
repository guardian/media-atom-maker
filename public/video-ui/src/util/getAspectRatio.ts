import { Video, Image } from "../services/VideosApi";

export const DEFAULT_VIDEO_RATIO  = "9:16";

export const getAspectRatioFromVideo = (video: Video): string | undefined => {
    if(!video.activeVersion) return undefined;
    const asset = video.assets.find(v => v.version === video.activeVersion && v.assetType === "Video" && v.aspectRatio);
    if(!asset) return undefined;
    return asset.aspectRatio;
};

export const getAspectRatioFromImage = (image: Image): string | undefined => {
    if(!image.master) return undefined;
    if(!image.master.aspectRatio) return undefined;
    return image.master.aspectRatio;
};

export const isImageCropOutOfSync = (video: Video): boolean => {
    if(!video.posterImage) return false;
    const imageAspectRatio = getAspectRatioFromImage(video.posterImage);
    if(!imageAspectRatio) return false;
    const videoAspectRatio = getAspectRatioFromVideo(video) ?? DEFAULT_VIDEO_RATIO;
    return videoAspectRatio !== imageAspectRatio;

};