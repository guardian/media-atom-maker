import React from 'react';
import { getStore } from '../../util/storeAccessor';


export const getYouTubeEmbedUrl = (id) => {
  const embedUrl = getStore().getState().config.youtubeEmbedUrl;
  return `${embedUrl}${id}?showinfo=0&rel=0`;
}
export function YouTubeEmbed({ id, className }) {
  return (
    <iframe
      type="text/html"
      className={className}
      src={getYouTubeEmbedUrl(id)}
      allowFullScreen
      frameBorder="0"
    />
  );
}
