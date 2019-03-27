import React from 'react';
import PropTypes from 'prop-types';
import Icon from '../Icon';
import { getPlutoProjectLink } from '../../services/PlutoApi';

export default class PlutoProjectLink extends React.Component {
  static propTypes = {
    projectId: PropTypes.string.isRequired
  };

  render() {
    return (
      <a
        className="button inline-block"
        target="_blank"
        rel="noopener noreferrer"
        href={getPlutoProjectLink(this.props.projectId)}
      >
        <Icon icon="open_in_new" className="icon__edit">Open Pluto Project</Icon>
      </a>
    );
  }
}
