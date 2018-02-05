import React from 'react';
import PropTypes from 'prop-types';
import TagPicker from '../FormFields/TagPicker';
import DatePicker from '../FormFields/DatePicker';
import TagTypes from '../../constants/TagTypes';
import { ManagedForm, ManagedField } from '../ManagedForm';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import Icon from '../Icon';
import * as createTarget from '../../actions/TargetingActions/createTarget';
import * as getTargets from '../../actions/TargetingActions/getTargets';
import * as deleteTarget from '../../actions/TargetingActions/deleteTarget';
import * as updateTarget from '../../actions/TargetingActions/updateTarget';

const isDeleting = (target, deleting) => deleting.indexOf(target.id) > -1;

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

  deleteTarget = target => {
    this.props.targetingActions.deleteTarget(target);
  };

  render() {
    return (
      <div>
        {this.props.targetsLoaded && (
          <div>
            <h3>Targeting rules</h3>
            {this.props.targets.map((target, index) => (
              <div key={target.id} className="targeting__form">
                {!isDeleting(target, this.props.deleting) && (
                  <ManagedForm
                    data={target}
                    updateData={this.updateTarget}
                    editable={true}
                    formName="TargetingForm"
                  >
                    <p>
                      {index === 0 ? (
                        <span>An</span>
                      ) : (
                        <span>... <strong className="highlight">or</strong> an</span>
                      )} article must match
                      <strong className="highlight"> all </strong>
                      of the tags in this group to suggest this atom
                      {this.props.targets.length > index + 1 ? ' ...' : '.'}
                    </p>
                    <ManagedField
                      fieldLocation="tagPaths"
                      name="Targeting tags"
                      formRowClass="form__row__byline"
                      tagType={TagTypes.tracking}
                      isDesired={false}
                      isRequired={false}
                      inputPlaceholder="Search commissioning info (type '*' to show all)"
                    >
                      <TagPicker disableTextInput />
                    </ManagedField>
                    <ManagedField fieldLocation="activeUntil" name="ActiveUntil">
                      <DatePicker canCancel={false} dayOnly />
                    </ManagedField>
                  </ManagedForm>
                )}
                {!isDeleting(target, this.props.deleting) && (
                  <button
                    className="button__secondary--cancel"
                    onClick={() =>this.deleteTarget(target)}
                  >
                    <Icon icon="delete" /> Delete
                  </button>
                )}
              </div>
            ))}
            <button className="btn" onClick={this.createTarget}>
              <Icon icon="add" /> Add targeting rule
            </button>
          </div>
        )}
      </div>
    );
  }
}

function mapStateToProps(state) {
  const { targeting: { targets: currentTargets, deleting } } = state;
  const targetsLoaded = !!currentTargets;
  const targets = currentTargets || [];
  return {
    targetsLoaded,
    targets,
    deleting
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
