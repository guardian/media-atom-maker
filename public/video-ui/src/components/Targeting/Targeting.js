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
import * as updateTarget from '../../actions/TargetingActions/updateTarget';

class Targeting extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  componentWillMount() {
    // Reset for each video every time we mount (state will only contain one
    // video's data)
    this.props.targetingActions.getTargets(this.props.video);
  }

  createTarget = () => {
    this.props.targetingActions.createTarget(this.props.video);
  };

  updateTarget = target => {
    this.props.targetingActions.updateTarget(target);
    return Promise.resolve(target);
  };

  render() {
    return (
      <div>
        {this.props.targetsLoaded &&
          (!this.props.target ? (
            <button className="btn" onClick={this.createTarget}>
              Create targeting
            </button>
          ) : (
            <ManagedForm
              data={this.props.target}
              updateData={this.updateTarget}
              editable={true}
              formName="TargetingForm"
              formClass="atom__edit__form"
            >
              <ManagedField
                fieldLocation="tagPaths"
                name="Tracking tags"
                formRowClass="form__row__byline"
                tagType={TagTypes.tracking}
                isDesired={false}
                isRequired={false}
                inputPlaceholder="Search commissioning info (type '*' to show all)"
              >
                <TagPicker disableTextInput />
              </ManagedField>
            </ManagedForm>
          ))}
      </div>
    );
  }
}

function mapStateToProps(state) {
  const { targeting: { targets: currentTargets } } = state;
  const targetsLoaded = !!currentTargets;
  const target = (currentTargets || [])[0]; // use the first target only
  return {
    targetsLoaded,
    target
  };
}

function mapDispatchToProps(dispatch) {
  return {
    targetingActions: bindActionCreators(
      Object.assign({}, createTarget, getTargets, deleteTarget, updateTarget),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Targeting);
