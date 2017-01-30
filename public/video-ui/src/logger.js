/* eslint-disable no-console */

export default class Logger {
  static log (message) {
    // TODO turn off in PROD?
    console.log(message);
  }

  static error (message) {
    console.error(message);
  }
}
