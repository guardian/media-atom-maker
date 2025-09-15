import {updateCheckedFormFieldsErrors} from "../../slices/checkedFormFields";

export function updateFormErrors(error) {
  return updateCheckedFormFieldsErrors(error)
}
