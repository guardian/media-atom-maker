import React from 'react';
import PropTypes from 'prop-types';

class Targeting extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  componentWillMount() {
    this.props.targetingActions.getTargets(this.props.video);
  }

  render () {
    return (
      <h1>hi</h1>
    );
  }
}

import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import * as createTarget from '../../actions/TargetingActions/createTarget';
import * as getTargets from '../../actions/TargetingActions/getTargets';
import * as deleteTarget from '../../actions/TargetingActions/deleteTarget';

function mapStateToProps(state) {
  return {
    targeting: state.targeting
  };
}

function mapDispatchToProps(dispatch) {
  return {
    targetingActions: bindActionCreators(
      Object.assign(
        {},
        createTarget,
        getTargets,
        deleteTarget
      ),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Targeting);
