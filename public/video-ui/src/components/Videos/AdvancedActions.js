import React from 'react';
import {getStore} from '../../util/storeAccessor';
import ReactTooltip from 'react-tooltip';

class AdvancedActions extends React.Component {
    // the permissions are also validated on the server-side for each request
    permissions = getStore().getState().config.permissions;
    showActions = this.permissions.deleteAtom;

    state = { deleteDoubleCheck: false }

    renderDelete() {
        if(!this.permissions.deleteAtom) {
            return false;
        }

        const disabled = this.props.usage.length > 0;
        const deleteMsg = this.state.deleteDoubleCheck ? "Confirm delete from database" : "Delete from database";
        const helpMsg = disabled ? "All usages of the atom must be removed before deletion" : "The video will remain on YouTube as private after the atom has been deleted";

        const doDelete = () => {
            if(this.state.deleteDoubleCheck) {
                this.props.videoActions.deleteVideo(this.props.video);
            } else {
                this.setState({ deleteDoubleCheck: true });
            }
        };

        return <li className="action-list__item">
            <button className="btn label__expired action-list__button" onClick={doDelete} disabled={disabled} data-tip={helpMsg}>
                {deleteMsg}
            </button>
        </li>;
    }

    render() {
        if(!this.showActions) {
            return false;
        }

        return <div>
            <ul className="action-list">
                {this.renderDelete()}
            </ul>
        </div>;
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as deleteVideo from '../../actions/VideoActions/deleteVideo';

function mapStateToProps(state) {
  return {
    video: state.video,
    usage: state.usage
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, deleteVideo), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(AdvancedActions);
