import reqwest from 'reqwest';
import {reEstablishSession} from 'babel?presets[]=es2015!panda-session';
import {getStore} from '../util/storeAccessor';

export function pandaReqwest(reqwestBody) {
  return new Promise((resolve, reject) => {
    reqwest(reqwestBody)
      .then(res => resolve(res))
      .fail(err => {
        if (err !== 419) {
          reject(err);
        }

        const store = getStore();
        const reauthUrl = store.getState().config.reauthUrl;

        reEstablishSession(reauthUrl, 5000)
          .then(() => {
            reqwest(reqwestBody)
              .then(res => resolve(res))
              .fail(err => reject(err));
          })
          .fail(error => {
              throw error;
          });
      });
  });
}
