import React from 'react';

import SelectBox from '../../FormFields/SelectBox';
import { publishedPrivacyStates } from '../../../constants/privacyStates';
import { statusNotification } from '../../../constants/notificationMessages';
import { getPublishErrors } from '../../../util/getPublishErrors';

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
        selectValues={publishedPrivacyStates || []}
        onUpdateField={this.updatePrivacyStatus}
        video={this.props.video}
        editable={this.props.editable}
        input={this.props.input}
        hasNotifications={getPublishErrors(this.props.video).errors.includes('privacyStatus')}
        notificationMessage={statusNotification}
        meta={this.props.meta} />
    );
  }
}
