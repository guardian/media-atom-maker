import React from 'react';

export function VideoEmbed({ sources, posterUrl }) {
  const props = {
    className: 'video-player',
    controls: 'controls',
    preload: 'metadata'
  };

  if (posterUrl) {
    props.poster = posterUrl;
  }

  if (sources.length === 1) {
    // to appease Safari
    return <video src={sources[0].src} {...props} />;
  } else {
    return (
      <video {...props} crossOrigin="anonymous">
        {sources.map(source => {
          if (source.mimeType == "application/vnd.apple.mpegurl") {
            const src = "https://uploads.guimcode.co.uk/2025/08/22/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-1captions_00001.vtt";
            return (
              <track default kind="subtitles" src={src}/>
            );
          } else {
            return (
              // <source key={source.src} src={source.src} type={source.mimeType}/>
              <source src="https://uploads.guimcode.co.uk/2025/08/22/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-1.mp4" type="video/mp4" />
            );
          }
        })}
      </video>
    );
  }
}
