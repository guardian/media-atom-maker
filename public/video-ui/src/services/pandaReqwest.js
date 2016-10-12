import Reqwest from 'reqwest';
import Q from 'q';
import {reEstablishSession} from 'babel?presets[]=es2015!panda-session';

export function pandaReqwest(reqwestBody) {
  return Q.Promise(function(resolve, reject) {
    Reqwest(reqwestBody)
        .then(res => {
          resolve(res)
        })
        .fail(err => {
          if (err.status == 419) {
            const reauthUrl = '/reauth';

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
