import React from 'react';
import { getStore } from '../../util/storeAccessor';

export const getYouTubeEmbedUrl = (id: any) => {
  const embedUrl = getStore().getState().config.youtubeEmbedUrl;
  return `${embedUrl}${id}?showinfo=0&rel=0`;
};
/**
 *
 * @param {{id: string, className?: string, largePreview?: boolean}} param0
 * @returns
 */
export function YouTubeEmbed({
  id,
  className,
  largePreview
}: {
  id: string;
  className: undefined;
  largePreview: boolean;
}) {
  return (
    <iframe
      // @ts-expect-error TS(2322): Type '{ type: string; className: undefined; src: s... Remove this comment to see the full error message
      type="text/html"
      className={className}
      src={getYouTubeEmbedUrl(id)}
      allowFullScreen
      frameBorder="0"
      height={largePreview ? '250px' : undefined}
      width={largePreview ? '400px' : undefined}
    />
  );
}
