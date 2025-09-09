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
  const videoSources = sources.filter(source => source.mimeType !== "text/vtt");

  // if m3u8 and mp4 are both present, put m3u8 first
  const m3u8 = videoSources.find(source => source.mimeType === "application/vnd.apple.mpegurl");
  const mp4 = videoSources.find(source => source.mimeType === "video/mp4");

  const sortedSources =
    (m3u8 && mp4) ? [m3u8].concat(videoSources.filter(source => source !== m3u8)) : videoSources;

  // add subtitle track for browsers that don't support m3u8
  const vtt = sources.find(source => source.mimeType === "text/vtt");
  const subtitleTrack =
    (m3u8 && mp4 && vtt) ? <track default kind="subtitles" src={vtt.src}/> : undefined;
  return {videoSources: sortedSources, subtitleTrack};
}
