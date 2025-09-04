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
    const {videoSources, subtitleTrack} = prepareSourcesAndSubtitles(sources);

    if (subtitleTrack) {
      // need to use CORS to load subtitle track
      props.crossOrigin = "anonymous";
    }

    return (
      <video {...props}>
        {videoSources.map(source => {
          return (
            <source key={source.src} src={source.src} type={source.mimeType}/>
          );
        })}
        {subtitleTrack}
      </video>
    );
  }
}

function prepareSourcesAndSubtitles(sources) {
  // if m3u8 and mp4 are both present, put m3u8 first
  const m3u8 = sources.find(source => source.mimeType === "application/vnd.apple.mpegurl");
  const mp4 = sources.find(source => source.mimeType === "video/mp4");
  const videoSources =
    (m3u8 && mp4) ? [m3u8].concat(sources.filter(source => source !== m3u8)) : sources;

  // add subtitle track for browsers that don't support m3u8
  const subtitles = sources.find(source => source.assetType === "subtitles");
  const subtitleTrack =
    (m3u8 && mp4 && subtitles) ? <track default kind="subtitles" src={subtitles.src}/> : undefined;
  return {videoSources, subtitleTrack};
}
