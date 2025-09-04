/* eslint-disable no-console */

export default class Logger {
  static log(message: unknown) {
    // TODO turn off in PROD?
    console.log(message);
  }

  static error(message: unknown) {
    console.error(message);
  }
}
