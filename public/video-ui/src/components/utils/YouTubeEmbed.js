import React from 'react';
import {getStore} from '../../util/storeAccessor';

export function YouTubeEmbed({id, className, width, height}) {
    const embedUrl = getStore().getState().config.youtubeEmbedUrl;
    const src = `${embedUrl}${id}?showinfo=0`;

    return <iframe
        type="text/html"
        className={className}
        width={width}
        height={height}
        src={src}
        allowFullScreen
        frameBorder="0">
    </iframe>;
}