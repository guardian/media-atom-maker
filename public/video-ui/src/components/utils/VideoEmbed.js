import React from 'react';

export function VideoEmbed({ sources }) {
  const props = {
    className: 'video-player',
    controls: 'controls',
    preload: 'metadata'
  };

  if (sources.length === 1) {
    // to appease Safari
    return <video src={sources[0].src} {...props} />;
  } else {
    return (
      <video {...props}>
        {sources.map(source => {
          return (
            <source key={source.src} src={source.src} type={source.mimeType} />
          );
        })}
      </video>
    );
  }
}
