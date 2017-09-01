import React from 'react';
import PropTypes from 'prop-types';
import { getStore } from '../../util/storeAccessor';

export default class AdvancedActions extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired,
    usages: PropTypes.object.isRequired,
    deleteVideo: PropTypes.func.isRequired
  };

  // the permissions are also validated on the server-side for each request
  permissions = getStore().getState().config.permissions;
  showActions = this.permissions.deleteAtom;

  state = { deleteDoubleCheck: false };

  renderDelete() {
    if (!this.permissions.deleteAtom) {
      return false;
    }

    const disabled = this.props.usages.totalUsages > 0;

    const deleteMsg = this.state.deleteDoubleCheck
      ? 'Confirm delete from database'
      : 'Delete from database';
    const helpMsg = disabled
      ? 'All usages of the atom must be removed before deletion'
      : 'The video will remain on YouTube as private after the atom has been deleted';

    const doDelete = () => {
      if (this.state.deleteDoubleCheck) {
        this.props.deleteVideo(this.props.video);
      } else {
        this.setState({ deleteDoubleCheck: true });
      }
    };

    return (
      <li className="action-list__item">
        <span data-tip={helpMsg}>
          <button
            className="btn label__expired action-list__button"
            onClick={doDelete}
            disabled={disabled}
          >
            {deleteMsg}
          </button>
        </span>
      </li>
    );
  }

  render() {
    if (!this.showActions) {
      return false;
    }

    return (
      <div>
        <ul className="action-list">
          {this.renderDelete()}
        </ul>
      </div>
    );
  }
}
