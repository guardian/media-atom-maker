import {pandaReqwest} from './pandaReqwest';


export default {

  fetchVideos: () => {
    return pandaReqwest({
      url: '/api/atoms'
    });
  },

  fetchVideo: (videoId) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId
    });
  },

  createVideo: () => {
    return pandaReqwest({
      url: '/api/atom',
      method: 'post'
    })
  }

}