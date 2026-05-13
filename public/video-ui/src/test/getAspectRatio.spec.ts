import { Asset, Video } from '../services/VideosApi';
import getAspectRatio from '../util/getAspectRatio';

describe('getAspectRatio', () => {
    const emptyVideo: Video = {
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
    const videoAsset1: Asset = {
        assetType: 'Video',
        version: 1,
        id: 'id',
        platform: 'Youtube'
    };
    const videoAsset2: Asset = {
        assetType: 'Video',
        version: 2,
        id: 'id',
        platform: 'Youtube'
    };
    const audioAsset1: Asset = {
        assetType: 'Audio',
        version: 1,
        id: 'id',
        platform: 'Youtube'
    };
    it('should return undefined if no activeVersion is present', () => {
        expect(getAspectRatio(emptyVideo)).toEqual(undefined);
    });
    it('should return undefined if there are no video assets', () => {
        const video = {
            ...emptyVideo,
            activeVersion: 1,
            assets: [
                audioAsset1
            ]
        };
        expect(getAspectRatio(video)).toEqual(undefined);
    });
    it('should return undefined if there is a not an aspectRatio on the active video asset', () => {
        const video = {
            ...emptyVideo,
            activeVersion: 1,
            assets: [
                videoAsset1,
                videoAsset2
            ]
        };
        expect(getAspectRatio(video)).toEqual(undefined);
    });
    it('should return the aspect ratio of the asset with the version that matches the active version', () => {
        const video = {
            ...emptyVideo,
            activeVersion: 2,
            assets: [
                { 
                    ...videoAsset1,
                    aspectRatio: '5:4'
                },
                {   ...videoAsset2,
                    aspectRatio: '7:3'
                }
            ]
        };
        expect(getAspectRatio(video)).toEqual('7:3');
    });
     it('should return the aspect ratio of the first asset if multiple video assets match the active version', () => {
        const video = {
            ...emptyVideo,
            activeVersion: 1,
            assets: [
                { 
                    ...videoAsset1,
                    aspectRatio: '5:4'
                },
                {   ...videoAsset1,
                    aspectRatio: '7:3'
                }
            ]
        };
        expect(getAspectRatio(video)).toEqual('5:4');
    });
});