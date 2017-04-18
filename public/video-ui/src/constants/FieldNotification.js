export default class FieldNotification {
  constructor(title, message, type) {
    this.title = title;
    this.message = message;
    this.type = type;
  }

  static warning = 'warning';

  static error = 'error';
}
