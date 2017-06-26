import React from 'react';

export function VideoEmbed({ sources }) {
  return (
    <video controls>
      {sources.map(source => {
        return (
          <source key={source.src} src={source.src} type={source.mimeType} />
        );
      })}
    </video>
  );
}
