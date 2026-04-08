import React, {DetailedHTMLProps, VideoHTMLAttributes} from 'react';
import { SelfHostedSource } from "../../slices/s3Upload";

export function VideoEmbed({ sources, posterUrl }: { sources: SelfHostedSource[]; posterUrl?: string }) {
  const props: DetailedHTMLProps<VideoHTMLAttributes<HTMLVideoElement>, HTMLVideoElement> = {
    className: 'video-player',
    controls: true,
    preload: 'metadata',
    crossOrigin: 'anonymous' // need to use CORS to load subtitle track
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

        {sources.filter(source => source.mimeType === "text/vtt").map((source, index) => {
          return (<track
            key={source.src}
            default={index === 0}
            kind="subtitles"
            src={source.src}
          />);
        })}
      </video>
    );
  }
}

/**
* Order is important here - the browser will use the first type it supports.
*/
export const supportedVideoFileTypes = [
  'video/mp4', // MP4 format
  'application/x-mpegURL', // HLS format
  'application/vnd.apple.mpegurl' // Alternative HLS format
] as const;

/**
 * Ensure sources are ordered by the order that MIME types are specified in
 * `supportedVideoFileTypes` and then by size in descending order.
 */

function prepareSources(assets: SelfHostedSource[]) {
  return supportedVideoFileTypes
    .reduce<typeof assets>((acc, type) => {
      const sourcesByType = assets.filter(
        ({ mimeType }) => mimeType === type
      );
      if (sourcesByType.length) {
        const sourcesOrderedByWidthDescending = sourcesByType.sort(
          (a, b) =>
            Number(b.width ?? 0) -
            Number(a.width ?? 0)
        );
        acc.push(...sourcesOrderedByWidthDescending);
      }
      return acc;
    }, [])
}
