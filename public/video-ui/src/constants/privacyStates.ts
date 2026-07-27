export default class PrivacyStates {
  static get defaultStates(): string[] {
    return ['Unlisted'];
  }

  static forForm(states: string[]): { id: string; title: string }[] {
    return states
      .filter(_ => _ !== 'Private')
      .map(state => {
        return { id: state, title: state };
      });
  }
}
