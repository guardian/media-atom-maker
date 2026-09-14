import { orderBy, uniq } from 'lodash';
import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState
} from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { css } from '@emotion/react';
import { Autocomplete } from '@guardian/stand/tag-picker';
import { getVideo } from '../../actions/VideoActions/getVideo';
import { saveVideo } from '../../actions/VideoActions/saveVideo';
import { IconikCommission } from '../../services/IconikApi';
import { IconikData, Video } from '../../services/VideosApi';
import {
  fetchIconikCommissions,
  fetchIconikProjects,
  IconikState,
  resetCommissions,
  resetProjects
} from '../../slices/iconik';
import { tagAutocompleteTheme } from '../../constants/themeOverrides';
import { AppDispatch, RootState } from '../../util/setupStore';
import Icon from '../Icon';

type Props = {
  video: Video;
};

const ALL_COMMISSION_YEARS_OPTION = 'all-commission-years';

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

  const [selectedCommissionYear, setSelectedCommissionYear] = React.useState<
    string | undefined
  >();
  const [commission, setCommission] = React.useState<string | undefined>(
    () => video.iconikData?.commissionId
  );
  const [project, setProject] = React.useState<string | undefined>(
    () => video.iconikData?.projectId
  );

  const commissionYear =
    selectedCommissionYear ??
    existingCommission?.year ??
    commissionYearOptions[0]?.id;

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
  }, [dispatch, workingGroup]);

  useEffect(() => {
    if (commission) {
      dispatch(fetchIconikProjects(commission));
    } else {
      dispatch(resetProjects());
    }
  }, [dispatch, commission]);

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
      setSelectedCommissionYear(undefined);
      setCommission(undefined);
      setProject(undefined);
    },
    []
  );

  const onCommissionYearChange = useCallback(
    (selectedCommissionYear: string | undefined) => {
      setSelectedCommissionYear(selectedCommissionYear);
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
    setSelectedCommissionYear(undefined);
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
    setSelectedCommissionYear(undefined);
    setCommission(undefined);
    setProject(undefined);
  }, [saveVideoUpdate]);

  return (
    <div className="form__group">
      <header className="video__detailbox__header">Iconik</header>
      <IconikAutocomplete
        label="Iconik Working Group"
        items={workingGroups}
        selectedId={workingGroup}
        onSelect={onWorkingGroupChange}
      />
      <IconikAutocomplete
        label="Commission Year"
        items={commissionYearOptions}
        selectedId={commissionYear}
        onSelect={onCommissionYearChange}
      />
      <IconikAutocomplete
        label="Iconik Commission"
        items={filteredCommissions}
        selectedId={commission}
        onSelect={onCommissionChange}
      />
      <IconikAutocomplete
        label="Iconik Project"
        items={projects}
        selectedId={project}
        onSelect={onProjectChange}
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

function sortOptions<T extends { title: string }>(items: T[]): T[] {
  const startWithNumber = items.filter(item => startsWithNumber(item.title));
  const rest = items.filter(item => !startsWithNumber(item.title));
  return [
    ...startWithNumber.sort((a, b) => b.title.localeCompare(a.title)),
    ...rest.sort((a, b) => a.title.localeCompare(b.title))
  ];
}

type IconikAutocompleteItem = { id: string; title: string };

type IconikAutocompleteProps = {
  label: string;
  items: IconikAutocompleteItem[];
  selectedId: string | undefined;
  onSelect: (id: string | undefined) => void;
};

function IconikAutocomplete({
  label,
  items,
  selectedId,
  onSelect
}: IconikAutocompleteProps) {
  const selectedItem = items.find(item => item.id === selectedId);

  const [inputValue, setInputValue] = useState<string>(
    selectedItem?.title ?? ''
  );

  // The underlying combobox calls `onTextInputChange('')` itself right
  // after a selection is made (see addSelection below). We want to ignore
  // that one forced call so the selected item's name stays visible in the
  // input, instead of immediately being blanked out again.
  const justSelectedRef = useRef(false);

  // Keep the input text in sync when the selection changes externally -
  // e.g. initial load, restore, or the underlying list being replaced
  // after a parent selection (working group/commission) changes.
  useEffect(() => {
    setInputValue(selectedItem?.title ?? '');
  }, [selectedItem?.title]);

  const visibleOptions = useMemo(() => {
    const query = inputValue.trim().toLowerCase();
    const matches = query
      ? items.filter(item => item.title.toLowerCase().includes(query))
      : items;
    return sortOptions(matches).map(item => ({
      id: item.id,
      name: item.title
    }));
  }, [items, inputValue]);

  const handleTextInputChange = (text: string) => {
    if (justSelectedRef.current) {
      justSelectedRef.current = false;
      if (text === '') {
        return;
      }
    }
    setInputValue(text);
  };

  const handleAddSelection = (selection: {
    id: string | number;
    name: string;
  }) => {
    onSelect(String(selection.id));
    justSelectedRef.current = true;
    setInputValue(selection.name);
  };

  return (
    <div className="form-element">
      <div className="form__row">
        <label className="form__label">{label}</label>
        <div className="form__autocomplete__container">
          <Autocomplete
            onTextInputChange={handleTextInputChange}
            options={visibleOptions}
            label={label}
            addSelection={handleAddSelection}
            loading={false}
            placeholder={`Search ${label}`}
            disabled={false}
            value={inputValue}
            cssOverrides={css`
              input {
                padding-right: 40px;
              }
            `}
            theme={tagAutocompleteTheme}
          />
          {Boolean(selectedId && inputValue) && (
            <button
              type="button"
              className="form__autocomplete__clear-button"
              aria-label={`Clear ${label}`}
              onClick={() => {
                onSelect(undefined);
                setInputValue('');
              }}
            >
              <Icon icon="cancel" className="icon__edit" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

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
