import {pandaReqwest} from './pandaReqwest';

export function fetchAtoms() {
  return pandaReqwest({
    url: '/api/atoms'
  });
}

export function fetchAtom(atomId) {
  return pandaReqwest({
    url: '/api/atom/' + atomId
  });
}