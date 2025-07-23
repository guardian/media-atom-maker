import React from 'react';
import { getStore } from '../../util/storeAccessor';


export const getYouTubeEmbedUrl = (id: string) => {
  const embedUrl = getStore().getState().config.youtubeEmbedUrl;
  return `${embedUrl}${id}?showinfo=0&rel=0`;
}
export function YouTubeEmbed({ id, className, largePreview } : {id: string, className?: string, largePreview?: boolean }) {
  return (
    <iframe
      className={className}
      src={getYouTubeEmbedUrl(id)}
      allowFullScreen
      frameBorder={0}
      {...(largePreview && { height: "250px", width: "400px" })}
    />
  );
}
