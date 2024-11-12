export default class FieldNotification {
  title: string;
  message: string;
  type: string;
  
  constructor(title: string, message: string, type: string) {
    this.title = title;
    this.message = message;
    this.type = type;
  }

  static warning = 'warning';

  static error = 'error';
}
