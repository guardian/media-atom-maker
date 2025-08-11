import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import { PlutoCommission, PlutoProject } from '../../services/PlutoApi';
import { Video } from '../../services/VideosApi';

type Props = {
  pluto: {
    commissions: PlutoCommission[],
    projects: PlutoProject[],
  },
  plutoActions: {
    getCommissions: { (): Promise<void> };
    getProjects: { (plutoData: { commissionId: string }): void }
  }
  video: Video;
  saveVideo: { (video: Video): Promise<void> }
}

const cloneVideoWithoutPlutoProjectId = (video: Video): Video => {
  const clone = structuredClone(video);
  if (clone.plutoData) {
    delete clone.plutoData.projectId;
  }
  return clone;
};

class PlutoProjectPicker extends React.Component<Props> {

  hasPlutoCommissions = () => this.props.pluto && this.props.pluto.commissions.length !== 0;

  UNSAFE_componentWillMount() {

    if (!this.hasPlutoCommissions()) {
      this.props.plutoActions.getCommissions().then(() => {
        const commissionId = this.props.video.plutoData?.commissionId;
        if (commissionId) {
          this.props.plutoActions.getProjects({ commissionId });
        }
      });
    }
  }

  onCommissionSelection() {
    this.props.saveVideo(
      cloneVideoWithoutPlutoProjectId(this.props.video)
    ).then(() => {
      // commissionId is expected to be set since this method is a side effect
      // of a commissionId being selected, but testing to maintain
      // type safety.
      const commissionId = this.props.video.plutoData?.commissionId;
      if (commissionId) {
        this.props.plutoActions.getProjects({ commissionId });
      }
    });
  }

  render() {
    const { video, saveVideo } = this.props;
    const { commissions, projects } = this.props.pluto;

    return (
      <ManagedForm
        data={video}
        updateData={saveVideo}
        editable={true}
        formName="Pluto"
      >
        <header className="video__detailbox__header">
          Pluto
        </header>
        <ManagedField
          fieldLocation="plutoData.commissionId"
          name="Commission"
          isRequired={false}
          updateSideEffects={() => this.onCommissionSelection()}
        >
          <SelectBox selectValues={commissions} />
        </ManagedField>
        <ManagedField
          fieldLocation="plutoData.projectId"
          name="Project"
          isRequired={false}
        >
          <SelectBox selectValues={projects} />
        </ManagedField>
      </ManagedForm>
    );
  };
}

import { connect } from 'react-redux';
import { bindActionCreators, Dispatch } from 'redux';
import { getCommissions } from '../../actions/PlutoActions/getCommissions';
import { getProjects } from '../../actions/PlutoActions/getProjects';


function mapStateToProps(state: { pluto: Props['pluto'] }) {
  return {
    pluto: state.pluto
  };
}

const actionCreators = {
  getCommissions, getProjects
};

function mapDispatchToProps(dispatch: Dispatch) {
  return {
    plutoActions: bindActionCreators<typeof actionCreators, Props['plutoActions']>(
      actionCreators,
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(PlutoProjectPicker);
