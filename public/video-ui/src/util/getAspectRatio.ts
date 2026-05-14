import { Video } from "../services/VideosApi";

export const getAspectRatioFromVideo = (video: Video): string | undefined => {
    if(!video.activeVersion) return undefined;
    const asset = video.assets.find(v => v.version === video.activeVersion && v.assetType === "Video" && v.aspectRatio);
    if(!asset) return undefined;
    return asset.aspectRatio;
};
