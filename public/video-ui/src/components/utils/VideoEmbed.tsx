import React, {DetailedHTMLProps, VideoHTMLAttributes} from 'react';
import { SelfHostedSource } from "../../slices/s3Upload";

export function VideoEmbed({ sources, posterUrl }: { sources: SelfHostedSource[]; posterUrl?: string }) {
  const props: DetailedHTMLProps<VideoHTMLAttributes<HTMLVideoElement>, HTMLVideoElement> = {
    className: 'video-player',
    controls: true,
    preload: 'metadata'
  };

  if (posterUrl) {
    props.poster = posterUrl;
  }

  if (sources.length === 1) {
    // to appease Safari
    return <video src={sources[0].src} {...props} />;
  } else {
    const videoSources = prepareSources(sources);

    return (
      <video {...props}>
        {videoSources.map(source => {
          return (
            <source key={source.src} src={source.src} type={source.mimeType}/>
          );
        })}
      </video>
    );
  }
}

function prepareSources(sources: SelfHostedSource[]) {
  const videoSources = sources.filter(source => source.mimeType !== "text/vtt");

  // if m3u8 and mp4 are both present, put mp4 first
  const m3u8 = videoSources.find(source => source.mimeType === "application/vnd.apple.mpegurl");
  const mp4 = videoSources.find(source => source.mimeType === "video/mp4");

  return (m3u8 && mp4) ? [mp4].concat(videoSources.filter(source => source !== mp4)) : videoSources;
}
