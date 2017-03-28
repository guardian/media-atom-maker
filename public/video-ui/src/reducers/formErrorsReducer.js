export default function errors(state = {}, action) {
  switch (action.type) {

    case 'FORM_ERRORS_UPDATE_REQUEST':
      const formName = Object.keys(action.error)[0];
      const newFormErrors = action.error[formName];
      const currentFormErrors = state[formName] || {};
      const updatedFormErrors = Object.assign({}, currentFormErrors, newFormErrors);
      const updatedForm = {[formName]: updatedFormErrors};
      return Object.assign({}, state, updatedForm);

    default:
      return state;
  }
}
