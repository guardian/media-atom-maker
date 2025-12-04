import { uniqueId } from 'lodash';
import React, { useCallback, useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { getVideo } from '../../actions/VideoActions/getVideo';
import { saveVideo } from '../../actions/VideoActions/saveVideo';
import { IconikData, Video } from '../../services/VideosApi';
import {
  fetchIconikCommissions,
  fetchIconikProjects,
  IconikState,
  resetCommissions,
  resetProjects
} from '../../slices/iconik';
import { AppDispatch, RootState } from '../../util/setupStore';

type Props = {
  video: Video;
};

export const IconikProjectPicker = ({ video }: Props) => {
  const dispatch = useDispatch<AppDispatch>();

  const { workingGroups, commissions, projects } = useSelector<
    RootState,
    IconikState
  >(({ iconik }) => iconik);

  const [workingGroup, setWorkingGroup] = React.useState<string | undefined>(
    () => video.iconikData?.workingGroupId
  );
  const [commission, setCommission] = React.useState<string | undefined>(
    () => video.iconikData?.commissionId
  );
  const [project, setProject] = React.useState<string | undefined>(
    () => video.iconikData?.projectId
  );

  useEffect(() => {
    if (workingGroup) {
      dispatch(fetchIconikCommissions(workingGroup));
    } else {
      dispatch(resetCommissions());
    }
    if (commission) {
      dispatch(fetchIconikProjects(commission));
    } else {
      dispatch(resetProjects());
    }
  }, [commission, dispatch, workingGroup]);

  const hasBeenEdited =
    video.iconikData?.workingGroupId !== workingGroup ||
    video.iconikData?.commissionId !== commission ||
    video.iconikData?.projectId !== project;

  const saveVideoUpdate = useCallback(
    (newIconikData: IconikData) =>
      dispatch(saveVideo({ ...video, iconikData: newIconikData })).then(() =>
        dispatch(getVideo(video.id))
      ),
    [dispatch, video]
  );

  const onWorkingGroupChange = useCallback(
    (selectedWorkingGroupId: string | undefined) => {
      setWorkingGroup(selectedWorkingGroupId);
      setCommission(undefined);
      setProject(undefined);
    },
    []
  );

  const onCommissionChange = useCallback(
    (selectedCommissionId: string | undefined) => {
      setCommission(selectedCommissionId);
      setProject(undefined);
    },
    []
  );

  const onProjectChange = useCallback(
    (selectedProjectId: string | undefined) => {
      setProject(selectedProjectId);
    },
    []
  );

  const restoreToSavedState = useCallback(() => {
    setWorkingGroup(video.iconikData?.workingGroupId);
    setCommission(video.iconikData?.commissionId);
    setProject(video.iconikData?.projectId);
  }, [
    video.iconikData?.commissionId,
    video.iconikData?.projectId,
    video.iconikData?.workingGroupId
  ]);
  
  const deleteIconikDataFromStore = useCallback(() => {
    saveVideoUpdate({
      workingGroupId: undefined,
      commissionId: undefined,
      projectId: undefined
    });
    setWorkingGroup(undefined);
    setCommission(undefined);
    setProject(undefined);
  }, [saveVideoUpdate]);

  return (
    <div className="form__group">
      <header className="video__detailbox__header">Iconik</header>
      <Select
        fieldName={'Iconik Working Group'}
        fieldValue={workingGroup}
        selectOptions={workingGroups.map(({ id, title }) => ({ id, title }))}
        notification={null}
        onUpdateField={onWorkingGroupChange}
      ></Select>
      <Select
        fieldName={'Iconik Commission'}
        fieldValue={commission}
        selectOptions={commissions.map(({ id, title }) => ({
          id,
          title
        }))}
        notification={null}
        onUpdateField={onCommissionChange}
      ></Select>
      <Select
        fieldName={'Iconik Project'}
        fieldValue={project}
        selectOptions={projects.map(({ id, title }) => ({
          id,
          title
        }))}
        notification={null}
        onUpdateField={onProjectChange}
      ></Select>
      {workingGroup && (!commission || !project) && (
        <p className="form__message form__message--error">
          Please select a project in order to save.
        </p>
      )}
      <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
        <button
          type="button"
          className="btn"
          disabled={project === undefined || !hasBeenEdited}
          onClick={() =>
            saveVideoUpdate({
              workingGroupId: workingGroup,
              commissionId: commission,
              projectId: project
            })
          }
        >
          Save
        </button>
        <button
          type="button"
          className="btn"
          disabled={!hasBeenEdited}
          onClick={restoreToSavedState}
        >
          Reset
        </button>
        <button
          type="button"
          className="btn button__secondary--remove"
          disabled={video.iconikData?.projectId === undefined}
          onClick={deleteIconikDataFromStore}
        >
          Remove
        </button>
      </div>
    </div>
  );
};

type SelectProps = {
  fieldName: string;
  fieldValue: string | undefined;
  selectOptions: { id: string; title: string }[];
  notification: {
    type: 'error' | 'info' | 'warning'; // we aren't doing anything with info or warning, but preserving this for now, for compatibility with pre-existing SelectBox component.
    message: string;
  } | null;
  onUpdateField: (newValue: string | undefined) => void;
  id?: string;
};

function Select({
  fieldName,
  fieldValue,
  selectOptions,
  notification,
  onUpdateField,
  id
}: SelectProps) {
  /**
   * @todo replace with useId when we upgrade to React 18+
   */
  const [elementId] = useState(id ?? uniqueId('select-box-'));

  const matchingValues =
    fieldValue === undefined
      ? []
      : selectOptions.filter(option => option.id === fieldValue);

  const hasError = notification && notification.type === 'error';

  return (
    <div className="form-element">
      <div>
        <div className="form__row">
          <label className="form__label" htmlFor={elementId}>
            {fieldName}
          </label>
          <select
            className={
              'form__field form__field--select ' +
              (hasError ? 'form__field--error' : '')
            }
            value={fieldValue ?? ''}
            onChange={e => {
              if (e.target.value === '') {
                onUpdateField(undefined);
                return;
              }
              onUpdateField(e.target.value);
            }}
            id={elementId}
          >
            {(fieldValue === undefined || matchingValues.length === 0) && (
              <option value={''}>Please select...</option>
            )}
            {selectOptions.map(function (option) {
              return (
                <option value={option.id} key={option.id}>
                  {option.title}
                </option>
              );
            })}
          </select>
          {hasError ? (
            <p className="form__message form__message--error">
              {notification.message}
            </p>
          ) : undefined}
        </div>
      </div>
    </div>
  );
}
