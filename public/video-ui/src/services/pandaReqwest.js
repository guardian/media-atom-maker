import Reqwest from 'reqwest';
import Q from 'q';
import {reEstablishSession} from 'babel?presets[]=es2015!panda-session';
import {getStore} from '../util/storeAccessor';

export function pandaReqwest(reqwestBody) {
  return Q.Promise(function(resolve, reject) {
    Reqwest(reqwestBody)
        .then(res => {
          resolve(res)
        })
        .fail(err => {
          if (err.status == 419) {
            const store = getStore();
            var reauthUrl = store.getState().config.reauthUrl;

            reEstablishSession(reauthUrl, 5000).then(
                res => {
                  Reqwest(reqwestBody).then(res => resolve(res)).fail(err => reject(err));
                },
                error => {
                  throw error;
                });

          } else {
            reject(err)
          }
        });
  });
}
