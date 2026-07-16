import { orderBy, uniqueId, uniq } from 'lodash';
import React, { useCallback, useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { getVideo } from '../../actions/VideoActions/getVideo';
import { saveVideo } from '../../actions/VideoActions/saveVideo';
import {
  IconikCommission,
  IconikProject,
  IconikWorkingGroup
} from '../../services/IconikApi';
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

  const commissionYearOptions = getCommissionYearOptions(commissions);
  const existingCommission = commissions.find(
    commission => commission.id === video.iconikData?.commissionId
  );

  const [workingGroup, setWorkingGroup] = React.useState<string | undefined>(
    () => video.iconikData?.workingGroupId
  );
  const [commissionYear, setCommissionYear] = React.useState<
    string | undefined
  >(existingCommission?.year ?? commissionYearOptions[0]?.id);
  const [commission, setCommission] = React.useState<string | undefined>(
    () => video.iconikData?.commissionId
  );
  const [project, setProject] = React.useState<string | undefined>(
    () => video.iconikData?.projectId
  );

  const filteredCommissions =
    commissionYear && commissionYear !== ALL_COMMISSION_YEARS_OPTION
      ? commissions.filter(commission => commission.year === commissionYear)
      : commissions;

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

  useEffect(() => {
    setCommissionYear(commissionYearOptions[0]?.id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [commissions]);

  const onWorkingGroupChange = useCallback(
    (selectedWorkingGroupId: string | undefined) => {
      setWorkingGroup(selectedWorkingGroupId);
      setCommission(undefined);
      setProject(undefined);
    },
    []
  );

  const onCommissionYearChange = useCallback(
    (selectedCommissionYear: string | undefined) => {
      setCommissionYear(selectedCommissionYear);
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
    setCommissionYear(existingCommission?.year);
    setCommission(video.iconikData?.commissionId);
    setProject(video.iconikData?.projectId);
  }, [
    video.iconikData?.commissionId,
    video.iconikData?.projectId,
    video.iconikData?.workingGroupId,
    existingCommission?.year
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
        selectOptions={sortProjects(workingGroups)}
        notification={null}
        onUpdateField={onWorkingGroupChange}
      />
      <Select
        fieldName={'Commission Year'}
        fieldValue={commissionYear}
        selectOptions={commissionYearOptions}
        notification={null}
        onUpdateField={onCommissionYearChange}
      />
      <Select
        fieldName={'Iconik Commission'}
        fieldValue={commission}
        selectOptions={sortProjects(filteredCommissions)}
        notification={null}
        onUpdateField={onCommissionChange}
      />
      <Select
        fieldName={'Iconik Project'}
        fieldValue={project}
        selectOptions={sortProjects(projects)}
        notification={null}
        onUpdateField={onProjectChange}
      />
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

function startsWithNumber(str: string) {
  return /^\d/.test(str);
}

function sortProjects(
  projects: Array<IconikWorkingGroup | IconikCommission | IconikProject>
) {
  const startWithNumber = projects.filter(project =>
    startsWithNumber(project.title)
  );
  const rest = projects.filter(project => !startsWithNumber(project.title));
  return [
    ...startWithNumber.sort((a, b) => b.title.localeCompare(a.title)),
    ...rest.sort((a, b) => a.title.localeCompare(b.title))
  ];
}

const ALL_COMMISSION_YEARS_OPTION = 'all-commission-years';
function getCommissionYearOptions(commissions: IconikCommission[]) {
  const commissionYears = orderBy(
    uniq(
      commissions
        .map(commission => commission.year)
        .filter((year): year is string => !!year)
    ),
    [year => year],
    ['desc']
  );
  const commissionYearOptions = commissionYears.map(year => ({
    id: year,
    title: year
  }));
  return [
    ...commissionYearOptions,
    { id: ALL_COMMISSION_YEARS_OPTION, title: 'View all' }
  ];
}

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
