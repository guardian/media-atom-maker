import { getAspectRatioFromVideo } from '../util/getAspectRatio';
import { audioAsset1, emptyVideo, videoAsset1, videoAsset2 } from './models';

describe('getAspectRatio', () => {
    
    it('should return undefined if no activeVersion is present', () => {
        expect(getAspectRatioFromVideo(emptyVideo)).toEqual(undefined);
    });
    it('should return undefined if there are no video assets', () => {
        const video = {
            ...emptyVideo,
            activeVersion: 1,
            assets: [
                audioAsset1
            ]
        };
        expect(getAspectRatioFromVideo(video)).toEqual(undefined);
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
        expect(getAspectRatioFromVideo(video)).toEqual(undefined);
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
        expect(getAspectRatioFromVideo(video)).toEqual('7:3');
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
        expect(getAspectRatioFromVideo(video)).toEqual('5:4');
    });
});