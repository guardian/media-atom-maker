import React from 'react';
import PropTypes from 'prop-types';
import TagPicker from '../FormFields/TagPicker';
import CustomDatePicker from '../FormFields/DatePicker';
import TagTypes from '../../constants/TagTypes';
import { ManagedForm, ManagedField } from '../ManagedForm';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import Icon from '../Icon';
import * as updateTarget from '../../actions/TargetingActions/updateTarget';
import { getTargets, createTarget, deleteTarget } from "../../slices/targeting";

const isDeleting = (target, deleting) => deleting.indexOf(target.id) > -1;

const TargetPrefix = ({ targetIndex, targetCount }) =>
  targetIndex === 0 ? (
    <span>{targetCount > targetIndex + 1 ? 'Either an ' : 'An '}</span>
  ) : (
    <span>
      ... <strong className="highlight">or</strong> an
    </span>
  );

const TargetSuffix = ({ targetIndex, targetCount }) => (
  <span>{targetCount > targetIndex + 1 ? ' ...' : '.'}</span>
);

const TargetDescription = props => (
  <p>
    <TargetPrefix {...props} /> article must match
    <strong className="highlight"> all </strong>
    of the tags in this rule to suggest this video
    <TargetSuffix {...props} />
  </p>
);

class Targeting extends React.Component {
  constructor(props) {
    super(props);
    this.props.targetingActions.getTargets(this.props.video);
  }

  static propTypes = {
    video: PropTypes.object.isRequired
  };

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
            <p>
              A targeting rule will allow this video to be suggested for certain
              articles.
            </p>
            {this.props.targets.map((target, index) => (
              <div key={target.id} className="targeting__form">
                {!isDeleting(target, this.props.deleting) && (
                  <ManagedForm
                    data={target}
                    updateData={this.updateTarget}
                    editable={true}
                    formName="TargetingForm"
                  >
                    <TargetDescription
                      targetIndex={index}
                      targetCount={this.props.targets.length}
                    />
                    <ManagedField
                      fieldLocation="tagPaths"
                      name="Targeting tags"
                      formRowClass="form__row__byline"
                      tagType={TagTypes.keyword}
                      isDesired={false}
                      isRequired={false}
                      inputPlaceholder="Target these tags (type '*' to show all)"
                    >
                      <TagPicker disableTextInput />
                    </ManagedField>
                    <ManagedField
                      fieldLocation="activeUntil"
                      name="Active until"
                    >
                      <CustomDatePicker canCancel={false} dayOnly />
                    </ManagedField>
                  </ManagedForm>
                )}
                {!isDeleting(target, this.props.deleting) && (
                  <button
                    className="button__secondary--cancel"
                    onClick={() => this.deleteTarget(target)}
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
  const {
    targeting: { targets: currentTargets, deleting }
  } = state;
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
      Object.assign({ getTargets, createTarget, deleteTarget }, updateTarget),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Targeting);
