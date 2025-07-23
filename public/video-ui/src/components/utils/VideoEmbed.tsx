import React from 'react';

export function VideoEmbed({ sources, posterUrl }) {
  const videoProps = {
    className: 'video-player',
    controls: 'controls',
    preload: 'metadata',
    ...(posterUrl && { poster: posterUrl}),
  };


  if (sources.length === 1) {
    // to appease Safari
    return <video src={sources[0].src} {...videoProps} />;
  } else {
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
}
