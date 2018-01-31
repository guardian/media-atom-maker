import React from 'react';
import PropTypes from 'prop-types';
import TagPicker from '../FormFields/TagPicker';
import TagTypes from '../../constants/TagTypes';
import { ManagedForm, ManagedField } from '../ManagedForm';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createTarget from '../../actions/TargetingActions/createTarget';
import * as getTargets from '../../actions/TargetingActions/getTargets';
import * as deleteTarget from '../../actions/TargetingActions/deleteTarget';

const logArgs = (...args) => console.log(...args);
const logArgsAync = (...args) => Promise.resolve(logArgs(...args));

class Targeting extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  componentWillMount() {
    this.props.targetingActions.getTargets(this.props.video);
  }

  render() {
    return (
      <ManagedForm
        data={this.props.video}
        updateData={logArgsAync}
        editable={true}
        updateErrors={(...args) => console.log(args)}
        updateWarnings={(...args) => console.log(args)}
        formName={this.props.formName}
        formClass="atom__edit__form"
      >
        <ManagedField
          fieldLocation="commissioningDesks"
          name="Tracking tags"
          formRowClass="form__row__byline"
          tagType={TagTypes.tracking}
          isDesired={false}
          isRequired={false}
          inputPlaceholder="Search commissioning info (type '*' to show all)"
        >
          <TagPicker />
        </ManagedField>
      </ManagedForm>
    );
  }
}

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
