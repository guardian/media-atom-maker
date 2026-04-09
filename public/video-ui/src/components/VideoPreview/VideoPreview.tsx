import React from 'react';
import { VideoEmbed } from '../utils/VideoEmbed';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';

import {
  findSmallestAssetAboveWidth,
  type Image
} from '../../util/imageHelpers';

type VideoAsset = {
  version?: number;
  platform?: string;
  id: string;
  mimeType?: string;
};

type PosterAsset = Image & {
  file: string;
};

type VideoPreviewProps = {
  video: {
    activeVersion?: number;
    assets?: VideoAsset[];
    posterImage?: {
      assets: PosterAsset[];
    };
    keywords?: string[];
  };
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
      return <YouTubeEmbed id={active[0].id} largePreview={false} />;
    }

    const sources = active.map(asset => {
      return { src: asset.id, mimeType: asset.mimeType ?? 'video/mp4' };
    });

    if (this.props.video.posterImage && this.props.video.posterImage.assets.length > 0) {
      const poster = findSmallestAssetAboveWidth(
        this.props.video.posterImage.assets
      ) as PosterAsset;

      return <VideoEmbed sources={sources} posterUrl={poster.file}/>;
    }

    return <VideoEmbed sources={sources}/>;
  }

  render() {
    return (
      <div className={"sixteen-by-nine"}>
        {this.renderPreview()}
      </div>
    );
  }
}

