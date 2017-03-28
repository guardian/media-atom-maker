import React from 'react';
import {getStore} from '../../util/storeAccessor';

class AdvancedActions extends React.Component {
    renderDelete() {
        // the permissions are also validated on the server-side for each request
        if(!getStore().getState().config.permissions.deleteAtom) {
            return false;
        }

        const doDelete = () => {
            this.props.videoActions.deleteVideo(this.props.video);
        };

        return <li className="asset-list__item">
            <button className="btn label__expired" onClick={doDelete}>DELETE</button>
            Usages will not be replaced automatically
        </li>;
    }

    render() {
        return <div>
            <div className="video__detailbox__header__container">
                <span className="video__detailbox__header">Advanced</span>
            </div>  
            <ul className="asset-list">
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