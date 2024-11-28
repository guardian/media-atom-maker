import React from 'react';
import PropTypes from 'prop-types';
import Icon from '../Icon';
import WorkflowApi from '../../services/WorkflowApi';

export default class WorkflowLink extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  render() {
    return (
      <a
        className="button inline-block"
        target="_blank"
        rel="noopener noreferrer"
        href={WorkflowApi.workflowItemLink(this.props.video)}
      >
        <Icon icon="open_in_new" className="icon__edit">Open in Workflow</Icon>
      </a>
    );
  }
}
