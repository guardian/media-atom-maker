import React from 'react';
import { getStore } from '../../util/storeAccessor';


export const getYouTubeEmbedUrl = (id: string) => {
  const embedUrl = getStore().getState().config.youtubeEmbedUrl;
  return `${embedUrl}${id}?showinfo=0&rel=0`;
};
export function YouTubeEmbed({ id, largePreview } : {id: string, largePreview: boolean}) {
  return (
    <iframe
      src={getYouTubeEmbedUrl(id)}
      allowFullScreen
      style={{ border: 'none' }}
      height={largePreview ? "250px" : undefined}
      width={largePreview ? "400px" : undefined}
    />
  );
}
