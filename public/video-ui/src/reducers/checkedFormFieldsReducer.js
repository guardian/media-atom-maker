export default function errors(state = {}, action) {
  let formName,
    newFormErrors,
    currentFormErrors,
    updatedFormErrors,
    updatedForm;
  switch (action.type) {
    case 'CHECKED_FIELDS_UPDATE_REQUEST':
      formName = Object.keys(action.error)[0];
      newFormErrors = action.error[formName];
      currentFormErrors = state[formName] || {};
      updatedFormErrors = Object.assign({}, currentFormErrors, newFormErrors);
      updatedForm = { [formName]: updatedFormErrors };
      return Object.assign({}, state, updatedForm);

    default:
      return state;
  }
}
