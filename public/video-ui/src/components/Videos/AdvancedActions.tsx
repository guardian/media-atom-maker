import React from 'react';
import { getStore } from '../../util/storeAccessor';
import Icon from '../Icon';
import { UsageState } from '../../slices/usage';
import { deleteVideo } from '../../actions/VideoActions/deleteVideo';

type Props = {
  video: any;
  usages: UsageState;
  deleteVideo: (...args: any[]) => any;
};

type State = {
  deleteDoubleCheck: boolean;
};

export default class AdvancedActions extends React.Component<Props, State> {
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
            <Icon
              icon={this.state.deleteDoubleCheck ? 'delete_forever' : 'delete'}
            >
              {deleteMsg}
            </Icon>
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
        <ul className="action-list">{this.renderDelete()}</ul>
      </div>
    );
  }
}
