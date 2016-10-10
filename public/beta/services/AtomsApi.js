import {pandaReqwest} from './pandaReqwest';

export function getAtoms() {
  return pandaReqwest({
    url: '/api/atoms'
  });
}