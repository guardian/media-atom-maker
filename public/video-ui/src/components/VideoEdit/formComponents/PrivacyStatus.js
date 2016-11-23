import React from 'react';

import SelectBox from '../../FormFields/SelectBox';
import { privacyStates } from '../../../constants/privacyStates';

export default class PrivacyStatusSelect extends React.Component {

  updatePrivacyStatus = (e) => {
    const newData = Object.assign({}, this.props.video, {
      privacyStatus: e.target.value
    });

    this.props.updateVideo(newData);
  };

  render () {
    return (
      <SelectBox
        fieldName="Privacy Status"
        fieldValue={this.props.video.privacyStatus}
        selectValues={privacyStates || []}
        onUpdateField={this.updatePrivacyStatus}
        {...this.props} />
    );
  }
}
