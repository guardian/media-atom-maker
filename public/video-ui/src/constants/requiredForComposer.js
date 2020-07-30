export default class RequiredForComposer {
  static get warning() {
    return 'This field is required for creating composer pages';
  }

  static get error() {
    return 'This field is required for updating composer pages';
  }

  static get fields() {
    return ['commissioningDesks', 'keywords', 'description']
  }
}
