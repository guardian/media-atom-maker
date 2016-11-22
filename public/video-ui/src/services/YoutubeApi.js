import {pandaReqwest} from './pandaReqwest';

export default {
  getCategories: () => {
    return pandaReqwest({
      url: '/api/youtube/categories',
      contentType: 'application/json',
      method: 'get'
    })
  }
}
