import React from 'react';
import { VideoEmbed } from '../utils/VideoEmbed';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';

import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';

type PreviewAsset = {
  version?: number;
  id: string;
  platform?: string;
  mimeType?: string;
  dimensions?: {
    height?: number;
    width?: number;
  };
};

export type PosterAsset = {
  file: string;
  size: number;
  dimensions: {
    width: number;
    height: number;
  };
};

type VideoPreviewData = {
  activeVersion?: number;
  assets?: PreviewAsset[];
  posterImage?: {
    assets: PosterAsset[];
  };
  keywords?: string[];
};

type VideoPreviewProps = {
  video: VideoPreviewData;
};

export default class VideoPreview extends React.Component<VideoPreviewProps> {

  renderPreview() {
    const activeVersion = this.props.video.activeVersion;
    const assets = this.props.video.assets || [];
    const active = assets.filter(asset => asset.version === activeVersion);

    if (active.length === 0) {
      return <div className="baseline-margin">No Active Video</div>;
    }

    if (active.length === 1 && active[0].platform === 'Youtube') {
      return <YouTubeEmbed id={active[0].id} largePreview={false}/>;
    }

    const sources = active.map(asset => {
      return {
        src: asset.id,
        mimeType: asset.mimeType ?? '',
        height: asset.dimensions?.height ?? 0,
        width: asset.dimensions?.width ?? 0
      };
    });

    if (this.props.video.posterImage && this.props.video.posterImage.assets.length > 0) {
      const poster = findSmallestAssetAboveWidth(
        this.props.video.posterImage.assets
      );

      return <VideoEmbed sources={sources} posterUrl={poster.file}/>;
    }

    return <VideoEmbed sources={sources}/>;
  }

  hasVerticalVideoTag() {
    const tags = this.props.video.keywords || [];
    return tags.includes('tone/vertical-video');
  }

  render() {
    return (
      <div className={this.hasVerticalVideoTag() ? "nine-by-sixteen" : "sixteen-by-nine"}>
        {this.renderPreview()}
      </div>
    );
  }
}

