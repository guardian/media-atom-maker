import React from 'react';
import {getStore} from '../../util/storeAccessor';

class AdvancedActions extends React.Component {
    // the permissions are also validated on the server-side for each request
    permissions = getStore().getState().config.permissions;
    showActions = this.permissions.deleteAtom;

    renderDelete() {
        if(!this.permissions.deleteAtom) {
            return false;
        }

        const doDelete = () => {
            const result = prompt("Enter the atom ID to confirm deletion (it can be copied from the URL)");

            if(result === this.props.video.id) {
                this.props.videoActions.deleteVideo(this.props.video);
            }
        };

        return <li className="action-list__item">
            <button className="btn label__expired action-list__button" onClick={doDelete}>DELETE</button>
            <span className="right">Usages will not be replaced and the video will remain on YouTube</span>
        </li>;
    }

    render() {
        if(!this.showActions) {
            return false;
        }

        return <div>
            <div className="video__detailbox__header__container">
                <span className="video__detailbox__header">Advanced</span>
            </div>  
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
    video: state.video
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, deleteVideo), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(AdvancedActions);