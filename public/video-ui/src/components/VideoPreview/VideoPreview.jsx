// @ts-check
import React from 'react';
import { VideoEmbed } from '../utils/VideoEmbed';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';

import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';
import { isSelfHostedSource } from '../../slices/s3Upload';


/**
 * @param {{video: import('../../services/VideosApi').Video}} param0  
 */
export const VideoPreview = ({ video }) => {
  const renderPreview = () => {
    const activeVersion = video.activeVersion;
    const assets = video.assets || [];
    const active = assets.filter(asset => asset.version === activeVersion);

    if (active.length === 0) {
      return <div className="baseline-margin">No Active Video</div>;
    }

    if (active.length === 1 && active[0].platform === 'Youtube') {
      return <YouTubeEmbed id={active[0].id} />;
    }

    const sources = active.map(asset => {
      return {
        src: asset.id,
        mimeType: asset.mimeType,
        height: asset.dimensions?.height ?? 0,
        width: asset.dimensions?.width ?? 0
      };
    }).filter(s => isSelfHostedSource(s));

    const maybeSmallestPosterImage = findSmallestAssetAboveWidth(
      video.posterImage?.assets ?? []
    );

    return (
      <VideoEmbed sources={sources} posterUrl={maybeSmallestPosterImage?.file} />
    );
  };

  const hasVerticalVideoTag = video.keywords?.includes('tone/vertical-video');

  return (
    <div
      className={hasVerticalVideoTag ? 'nine-by-sixteen' : 'sixteen-by-nine'}
    >
      {renderPreview()}
    </div>
  );
};

export default VideoPreview;
