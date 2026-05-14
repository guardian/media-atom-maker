export default class RequiredForDefaultVideo {
  static get warning() {
    return 'This field is required for publishing non-youtube videos';
  }

  static get error() {
    return 'This field is required for publishing non-youtube videos';
  }

  static get fields() {
    return ['atomTagIds'];
  }
}
