import { Asset, Video } from "../services/VideosApi";

export const emptyVideo: Video = {
    id: '',
    labels: [],
    contentChangeDetails: {
        lastModified: {
            date: '',
            user: {
                email: '',
                firstName: '',
                lastName: ''
            }
        },
        created: {
            date: '',
            user: {
                email: '',
                firstName: '',
                lastName: ''
            }
        },
        published: {
            date: '',
            user: {
                email: '',
                firstName: '',
                lastName: ''
            }
        },
        revision: 0,
        scheduledLaunch: {
            date: '',
            user: {
                email: '',
                firstName: '',
                lastName: ''
            }
        },
        embargo: {
            date: '',
            user: {
                email: '',
                firstName: '',
                lastName: ''
            }
        },
        expiry: {
            date: '',
            user: {
                email: '',
                firstName: '',
                lastName: ''
            }
        }
    },
    assets: [],
    title: '',
    category: undefined,
    youtubeOverrideImage: undefined,
    tags: [],
    byline: [],
    commissioningDesks: [],
    keywords: [],
    youtubeTitle: '',
    blockAds: undefined
};
export const videoAsset1: Asset = {
    assetType: 'Video',
    version: 1,
    id: 'id',
    platform: 'Youtube'
};
export const videoAsset2: Asset = {
    assetType: 'Video',
    version: 2,
    id: 'id',
    platform: 'Youtube'
};
export const audioAsset1: Asset = {
    assetType: 'Audio',
    version: 1,
    id: 'id',
    platform: 'Youtube'
};
export const videoWithActiveAsset = {
    ...emptyVideo,
    activeVersion: 1,
    assets: [
        { 
            ...videoAsset1,
            aspectRatio: '5:4'
        }
    ]
};