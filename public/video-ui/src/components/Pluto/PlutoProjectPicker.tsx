import React from 'react';
import PropTypes from 'prop-types';
import _set from 'lodash/fp/set';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import { PlutoCommission, PlutoProject } from '../../services/PlutoApi';
import { KnownAction } from '../../actions';

type PlutoData = any;

type Props = {
  pluto: {
    commissions: PlutoCommission[],
    projects: PlutoProject[],
  },
  plutoActions: {
    getCommissions: { (): { (dispatch: Dispatch<KnownAction>): Promise<void> } };
    getProjects: { (plutoData: PlutoData): void }
  }
  video: {
    plutoData: PlutoData
  }
  saveVideo: { (plutoData: PlutoData): Promise<void> }
}

class PlutoProjectPicker extends React.Component<Props> {
  static propTypes = {
    video: PropTypes.object.isRequired,
    saveVideo: PropTypes.func.isRequired
  };

  hasPlutoCommissions = () => this.props.pluto && this.props.pluto.commissions.length !== 0;

  UNSAFE_componentWillMount() {

    if (!this.hasPlutoCommissions()) {
      console.log('this.props.plutoActions.getCommissions',this.props.plutoActions.getCommissions);
      // exisitng bug?
      this.props.plutoActions.getCommissions().then(() => {
        const { plutoData } = this.props.video;
        if (plutoData.commissionId) {
          this.props.plutoActions.getProjects(plutoData);
        }
      });
    }
  }

  onCommissionSelection() {
    this.props.saveVideo(
      _set('plutoData.projectId', null, this.props.video)
    ).then(() => {
      const { plutoData } = this.props.video;
      this.props.plutoActions.getProjects(plutoData);
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
import * as getCommissions from '../../actions/PlutoActions/getCommissions';
import * as getProjects from '../../actions/PlutoActions/getProjects';


function mapStateToProps(state: { pluto: Props['pluto'] }) {
  return {
    pluto: state.pluto
  };
}

function mapDispatchToProps(dispatch: Dispatch) {
  return {
    plutoActions: bindActionCreators(
      Object.assign(
        {},
        getCommissions,
        getProjects
      ),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(PlutoProjectPicker);
