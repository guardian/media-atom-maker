import React from 'react';
import TagPicker from '../FormFields/TagPicker';
import CustomDatePicker from '../FormFields/DatePicker';
import TagTypes from '../../constants/TagTypes';
import { ManagedForm, ManagedField } from '../ManagedForm';
import { connect } from 'react-redux';
import { AnyAction, bindActionCreators, Dispatch } from 'redux';
import Icon from '../Icon';
import {
  getTargets,
  createTarget,
  updateTarget,
  deleteTarget
} from '../../slices/targeting';
import { Video } from '../../services/VideosApi';

const isDeleting = (target: { id: any }, deleting: string | any[]) =>
  deleting.indexOf(target.id) > -1;

const TargetPrefix = ({
  targetIndex,
  targetCount
}: {
  targetIndex: any;
  targetCount: any;
}) =>
  targetIndex === 0 ? (
    <span>{targetCount > targetIndex + 1 ? 'Either an ' : 'An '}</span>
  ) : (
    <span>
      ... <strong className="highlight">or</strong> an
    </span>
  );

const TargetSuffix = ({
  targetIndex,
  targetCount
}: {
  targetIndex: any;
  targetCount: any;
}) => <span>{targetCount > targetIndex + 1 ? ' ...' : '.'}</span>;

const TargetDescription = (
  props: React.JSX.IntrinsicAttributes & { targetIndex: any; targetCount: any }
) => (
  <p>
    <TargetPrefix {...props} /> article must match
    <strong className="highlight"> all </strong>
    of the tags in this rule to suggest this video
    <TargetSuffix {...props} />
  </p>
);

type TargetingProps = {
  video: Video;
};

class Targeting extends React.Component<TargetingProps> {
  constructor(props: TargetingProps) {
    super(props);
    (this.props as any).targetingActions.getTargets(this.props.video);
  }

  createTarget = () => {
    (this.props as any).targetingActions.createTarget(this.props.video);
  };

  updateTarget = (target: any) => {
    (this.props as any).targetingActions.updateTarget(target);
    return Promise.resolve(target);
  };

  deleteTarget = (target: any) => {
    (this.props as any).targetingActions.deleteTarget(target);
  };

  render() {
    return (
      <div>
        {(this.props as any).targetsLoaded && (
          <div>
            <p>
              A targeting rule will allow this video to be suggested for certain
              articles.
            </p>
            {(this.props as any).targets.map((target: any, index: any) => (
              <div key={target.id} className="targeting__form">
                {/* @ts-expect-error TS(2769): No overload matches this call. */}
                {!isDeleting(target, (this.props as any).deleting) && (
                  <ManagedForm
                    data={target}
                    updateData={this.updateTarget}
                    editable={true}
                    formName="TargetingForm"
                  >
                    <TargetDescription
                      targetIndex={index}
                      targetCount={(this.props as any).targets.length}
                    />
                    {/* @ts-expect-error TS(2769): No overload matches this call. */}
                    <ManagedField
                      fieldLocation="tagPaths"
                      name="Targeting tags"
                      formRowClass="form__row__byline"
                      tagType={TagTypes.keyword}
                      isDesired={false}
                      isRequired={false}
                      inputPlaceholder="Target these tags (type '*' to show all)"
                    >
                      {/* @ts-expect-error TS(2322): Type '{ disableTextInput: true; }' is not assignab... Remove this comment to see the full error message */}
                      <TagPicker disableTextInput />
                    </ManagedField>
                    {/* @ts-expect-error TS(2769): No overload matches this call. */}
                    <ManagedField
                      fieldLocation="activeUntil"
                      name="Active until"
                    >
                      {/* @ts-expect-error TS(2739): Type '{ canCancel: false; dayOnly: true; }' is mis... Remove this comment to see the full error message */}
                      <CustomDatePicker canCancel={false} dayOnly />
                    </ManagedField>
                  </ManagedForm>
                )}
                {!isDeleting(target, (this.props as any).deleting) && (
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

function mapStateToProps(state: {
  targeting: { targets: any; deleting: any };
}) {
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

function mapDispatchToProps(dispatch: Dispatch<AnyAction>) {
  return {
    targetingActions: bindActionCreators(
      { getTargets, createTarget, deleteTarget, updateTarget },
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Targeting);
