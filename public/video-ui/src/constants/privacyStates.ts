export default class PrivacyStates {
  static get defaultStates() {
    return ['Unlisted'];
  }

  static forForm(states: any) {
    return states
      .filter((_: string) => _ !== 'Private')
      .map((state: any) => {
        return { id: state, title: state };
      });
  }
}
