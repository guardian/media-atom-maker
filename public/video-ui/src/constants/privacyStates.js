export default class PrivacyStates {
  static get defaultStates() {
    return ['Public', 'Unlisted'];
  }

  static forForm(states) {
    return states.filter(_ => _ !== 'Private').map(state => {
      return { id: state, title: state };
    });
  }
}
