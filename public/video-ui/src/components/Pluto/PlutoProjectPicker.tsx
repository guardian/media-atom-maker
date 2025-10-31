import React, { useCallback, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { saveVideo } from '../../actions/VideoActions/saveVideo';
import { Video } from '../../services/VideosApi';
import {
  fetchCommissions,
  fetchProjects,
  PlutoState
} from '../../slices/pluto';
import { AppDispatch, RootState } from '../../util/setupStore';
import SelectBox from '../FormFields/SelectBox';
import { ManagedField, ManagedForm } from '../ManagedForm';

type Props = {
  video: Video;
};

const cloneVideoWithoutPlutoProjectId = (video: Video): Video => {
  const clone = structuredClone(video);
  if (clone.plutoData) {
    delete clone.plutoData.projectId;
  }
  return clone;
};

export const PlutoProjectPicker = ({ video }: Props) => {
  const dispatch = useDispatch<AppDispatch>();

  const { commissions, projects } = useSelector<RootState, PlutoState>(
    ({ pluto }) => pluto
  );

  useEffect(() => {
    dispatch(fetchCommissions());
  }, []);

  const dispatchSaveVideo = (video: Video) => dispatch(saveVideo(video));

  useEffect(() => {
    const commissionId = video.plutoData?.commissionId;
    if (commissionId) {
      dispatch(fetchProjects(video.plutoData.commissionId));
    }
  }, [video.plutoData?.commissionId]);

  const onCommissionSelection = useCallback(() => {
    dispatchSaveVideo(cloneVideoWithoutPlutoProjectId(video)).then(() => {
      // commissionId is expected to be set since this method is a side effect
      // of a commissionId being selected, but testing to maintain
      // type safety.
      const commissionId = video.plutoData?.commissionId;
      if (commissionId) {
        dispatch(fetchProjects(commissionId));
      }
    });
  }, [video]);

  return (
    <ManagedForm
      data={video}
      updateData={dispatchSaveVideo}
      editable={true}
      formName="Pluto"
    >
      <header className="video__detailbox__header">Pluto</header>
      <ManagedField
        fieldLocation="plutoData.commissionId"
        name="Commission"
        isRequired={false}
        updateSideEffects={onCommissionSelection}
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
