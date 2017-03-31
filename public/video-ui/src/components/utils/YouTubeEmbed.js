import React from 'react';
import {getStore} from '../../util/storeAccessor';

export function YouTubeEmbed({ id, className }) {
    const embedUrl = getStore().getState().config.youtubeEmbedUrl;
    const src = `${embedUrl}${id}?showinfo=0`;

    return <iframe
        type="text/html"
        className={className}
        src={src}
        allowFullScreen
        frameBorder="0">
    </iframe>;
}