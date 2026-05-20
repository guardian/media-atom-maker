import { getGridQueryParams } from "../util/getGridMediaId";
import { emptyVideo, videoWithActiveAsset5by4 } from "./models";

describe('getGridQueryParams', () => {
    it('should return the default video aspect ratio if the cropType is verticalVideo', () => {
        const params = getGridQueryParams('verticalVideo', emptyVideo);
        expect(params).toEqual(`cropType=verticalVideo&customRatio=verticalVideo,9,16`);
    });
    it('should return a custom aspect ratio if the crop type is custom and one is available on the video', () => {
        const params = getGridQueryParams('custom', videoWithActiveAsset5by4);
        expect (params).toEqual(`cropType=custom&customRatio=custom,5,4`);
    });
     it('should default to a video crop if crop type is custom and no valid asset exists', () => {
        const params = getGridQueryParams('custom', emptyVideo);
        expect (params).toEqual(`cropType=video`);
    });
    it('should return the crop type otherwise', () => {
        const params = getGridQueryParams('video', videoWithActiveAsset5by4);
        expect (params).toEqual(`cropType=video`);
    });
});