import React from 'react';
import { Source } from '../VideoUpload/VideoUpload';

export function VideoEmbed({
  sources,
  posterUrl
}: {
  sources: Source[];
  posterUrl?: string;
}) {
  if (!sources.length) return null;

  const videoProps = {
    className: 'video-player',
    controls: true,
    preload: 'metadata',
    ...(posterUrl && { poster: posterUrl })
  };

  if (sources.length === 1) {
    // to appease Safari
    return <video src={sources[0].src} {...videoProps} />;
  }

  return (
    <video {...videoProps}>
      {sources.map(source => {
        return (
          <source key={source.src} src={source.src} type={source.mimeType} />
        );
      })}
    </video>
  );
}
