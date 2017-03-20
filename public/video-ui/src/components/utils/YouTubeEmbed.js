import React from 'react';
import {getStore} from '../../util/storeAccessor';

export default function YouTubeEmbed({id}) {
    const embedUrl = getStore().getState().config.youtubeEmbedUrl;
    const src = `${embedUrl}${id}?showinfo=0`;

    return <iframe
        className="baseline-margin"
        type="text/html"
        src={src}
        allowFullScreen
        frameBorder="0">
    </iframe>;
}