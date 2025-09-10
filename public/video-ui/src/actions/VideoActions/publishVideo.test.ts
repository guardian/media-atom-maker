import { TextDecoder, TextEncoder } from 'util';
global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder;
import { http, HttpResponse, delay } from 'msw'
import { setupServer } from 'msw/node'
import {setupStore} from "../../util/setupStore";
import {publishVideo} from "./publishVideo";
import {blankVideoData} from "../../constants/blankVideoData";

export const handlers = [
  http.put('/api/atom/:id/publish', async ({params}) => {
    await delay(200)
    return HttpResponse.json({...blankVideoData, id: params.id})
  })
]

const server = setupServer(...handlers)

// Enable API mocking before tests.
beforeAll(() => server.listen())

// Reset any runtime request handlers we may add during the tests.
afterEach(() => server.resetHandlers())

// Disable API mocking after the tests are done.
afterAll(() => server.close())

describe('publishVideo', () => {
  it("It correctly sets the saveState and sets the returned Video", async () => {
    // @ts-ignore blah
    window.guardian = {
      csrf: {
        token: "1234"
      }
    }
    const atomId = "ATOM_ID"
    const store = setupStore();
    const publishAction = store.dispatch(publishVideo(atomId));

    expect(store.getState().saveState.publishing).toBe(true)

    await publishAction.then(() => {
        expect(store.getState().saveState.publishing).toBe(false);
        expect(store.getState().publishedVideo.id).toBe(atomId);
        const video = store.getState().video;
        const id = video ? video.id : null;
        expect(id).toBe(atomId);

      }
    )
  })
})
