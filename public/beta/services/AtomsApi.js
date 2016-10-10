import {pandaReqwest} from './pandaReqwest';

export function getAtoms() {
  return pandaReqwest({
    url: '/api/atoms'
  });
}

export function getAtom(atomId) {
  return pandaReqwest({
    url: '/api/atom/' + atomId
  });
}