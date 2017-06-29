import React from 'react';

export function VideoEmbed({ sources }) {
  return (
    <video className="video-player" controls preload="metadata">
      {sources.map(source => {
        return (
          <source key={source.src} src={source.src} type={source.mimeType} />
        );
      })}
    </video>
  );
}
