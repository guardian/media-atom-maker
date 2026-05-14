import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useSelector } from 'react-redux';
import { TagAutocomplete, TagTable } from '@guardian/stand/tag-picker';
import type { Tag } from '../../services/tagmanager';
import { getTagByPath, getTagsByType } from '../../services/tagmanager';
import BinSvg from "../../../images/bin.svg?react";
import FieldNotification from '../../constants/FieldNotification';
import debounce from "lodash/debounce";
import type { AppConfig } from '../../slices/config';

type VideoTag = {
  id: number;
  path: string;
  name: string;
  type: string;
  sectionName: string;
};

const videoTagFromTagManager = (data: Tag): VideoTag => {
  return {
    id: data.id,
    path: data.path,
    name: data.internalName,
    type: data.type,
    sectionName: data.section.name
  };
};

const videoTagsToStringList = (tags: VideoTag[]) => {
  return tags.map(tag => tag.path);
};

let pseudoIdCounter = -100; // For generating unique IDs for tags that don't exist in the tag manager

const generatePseudoId = () => {
  // actually value doesn't matter as long as it's unique, so we can just decrement from a negative number
  // to avoid conflicts with real tag IDs
  pseudoIdCounter -= 1;
  return pseudoIdCounter;
};

const fallbackVideoTagFromString = (tagPath: string): VideoTag => {
  return {
    id: generatePseudoId(),
    path: tagPath,
    name: tagPath,
    type: 'Unrecognised',
    sectionName: ''
  };
};

const videoTagsFromStringList = (tagPaths: string[], tagManagerUrl?: string): Promise<VideoTag[]> => {
  if (tagManagerUrl) {
    return Promise.all(
      tagPaths.map(tagPath => {
        return getTagByPath(tagManagerUrl, tagPath)
          .then(tag => {
            if (tag) {
              return videoTagFromTagManager(tag);
            }
            else {
              // If the tag doesn't exist in the tag manager, we create a fallback VideoTag with the path as the name
              // and a pseudo ID. This way we can still display the tag in the UI and keep the tag in
              // the atom's tag list until users explicitly remove it.
              return fallbackVideoTagFromString(tagPath);
            }
          });
      })
    );
  }
  else {
    return Promise.resolve(
      tagPaths.map(fallbackVideoTagFromString)
    );
  }
};



const nonEditableUiTheme = {
  row: {
    backgroundColor: '#00000000',
    borderBottom: {
      borderColor: '#BDBDBD'
    },
    backgroundHoverColor: '#00000000',
    firstRowBackgroundColor: '#00000000',
    firstRowBackgroundHoverColor: '#00000000'
  },
  cell: {
    borderBetweenCells: {
      borderColor: '#BDBDBD'
    }
  }
};

const editableUiTheme = {
  row: {
    backgroundColor: '#00000000',
    borderBottom: {
      borderColor: '#BDBDBD'
    },
    backgroundHoverColor: '#000',
    firstRowBackgroundColor: '#00000000',
    firstRowBackgroundHoverColor: '#000'
  },
  cell: {
    borderBetweenCells: {
      borderColor: '#BDBDBD'
    }
  },
  input: {
    color: '#BDBDBD',
    backgroundColor: '#393939',
    borderColor: '#BDBDBD',
    disabledBackgroundColor: '#393939'
  },
  listbox: {
    backgroundColor: '#393939',
    borderColor: '#BDBDBD',
    item: {
      color: '#BDBDBD',
      backgroundHoverColor: '#252525',
      colorHover: '#BDBDBD',
      backgroundFocusedColor: '#252525',
      colorFocused: '#BDBDBD'
    }
  }
};

interface StandTagPickerProps {
  tagTypes: string[];

  // the following properties are passed down from the parent ManagedField
  fieldName: string;
  fieldValue?: string[];
  editable: boolean;
  onUpdateField: (newValue: string[]) => void | Promise<void>;
  placeholder?: string;
  hasError: (prop: {notification: FieldNotification | undefined}) => boolean;
  hasWarning: (prop: {notification: FieldNotification | undefined}) => boolean;

  // it is set by validation logic in the parent component
  notification?: FieldNotification;
}


const isFieldValueChanged = (fieldValue: string[], selectedTags: VideoTag[]) => {
  if (fieldValue.length !== selectedTags.length) {
    return true;
  }
  for (let i = 0; i < selectedTags.length; i++) {
    if (fieldValue[i] !== selectedTags[i].path) {
      return true;
    }
  }
  return false;
};

const StandTagPicker = ({ tagTypes, fieldName, fieldValue, editable, onUpdateField, placeholder, hasError, hasWarning, notification }: StandTagPickerProps) => {

  const [selectedTags, setSelectedTags] = useState<VideoTag[]>([]);
  const [options, setOptions] = useState<VideoTag[]>([]);
  const [value, setValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [tagSearchError, setTagSearchError] = useState<boolean>(false);
  const tagManagerUrl = useSelector((state: {config: AppConfig}) => state.config.tagManagerUrl);

  const searchTags = useCallback((inputText: string, selectedTags: VideoTag[]) => {
    if (tagManagerUrl) {
      setIsLoading(true);
      getTagsByType(tagManagerUrl, inputText, tagTypes)
        .then(response => {
          const tags = response.data
            .map((tagItem) => tagItem.data)
            .filter((tag) => !tag.deprecated)
            .map(videoTagFromTagManager)
            .filter((tag) => !selectedTags.some((selectedTag) => selectedTag.path === tag.path));
          setTagSearchError(false);
          setOptions(tags);
        })
        .catch(() => {
          setTagSearchError(true);
        })
        .finally(() => {
          setIsLoading(false);
        });
    }
  }, [tagManagerUrl, tagTypes, setIsLoading, setTagSearchError, setOptions]);

  const debouncedSearchTags = useMemo(() => debounce(searchTags, 500), [searchTags]);

  const onTextInputChange = (inputText: string) => {
    setValue(inputText);
    if (inputText === '') {
      setOptions([]);
      return;
    }
    debouncedSearchTags(inputText, selectedTags);
  };

  const onUpdate = (newTags: VideoTag[]) => {
    setSelectedTags(newTags);
    const newValue = videoTagsToStringList(newTags);
    onUpdateField(newValue);
  };

  const onTagAdded = (tag: VideoTag) => {
    if (! selectedTags.some((t) => t.path === tag.path)) {
      const newTags = [...selectedTags, tag];
      onUpdate(newTags);
    }
  };

  const onTagRemoved = (tag: VideoTag) => {
    const index = selectedTags.findIndex((t) => t.path === tag.path);
    if (index !== -1) {
      const newTags = [...selectedTags];
      newTags.splice(index, 1);
      onUpdate(newTags);
    }
  };

  useEffect(() => {
    if (fieldValue) {
      if (isFieldValueChanged(fieldValue, selectedTags)) {
        videoTagsFromStringList(fieldValue, tagManagerUrl)
          .then(tags => {
            setSelectedTags(tags);
          });
      }
    }
  }, [fieldValue, selectedTags,tagManagerUrl]);

  const renderReadOnly = () => {
    if (selectedTags.length === 0) {
      return (
        <p className={'details-list__field details-list__empty'}>
          {placeholder}
        </p>
      );
    } else {
      return (
        <div className="stand-tag-table-container">
          <TagTable
            rows={selectedTags}
            filterRows={() => true}
            showTagType={true}
            showTagSectionName={true}
            theme={nonEditableUiTheme}
          />
        </div>
      );
    }
  };

  return (
    <>
      <div className="form__row">
        <div className="form__label__layout">
          <label className="form__label">{fieldName}</label>
        </div>
      </div>
      {editable && (
        <>
          {tagSearchError && (
            <div className="form__field--external-error">
              Tags are currently unavailable
            </div>
          )}
          <TagAutocomplete
            onTextInputChange={onTextInputChange}
            options={options}
            label="Tags"
            addTag={onTagAdded}
            loading={isLoading}
            placeholder={''}
            disabled={false}
            value={value}
            theme={editableUiTheme}
          />
          {selectedTags.length > 0 && (
            <div className="stand-tag-table-container">
              <TagTable
                rows={selectedTags}
                filterRows={() => true}
                removeIcon={<BinSvg color="#DCDCDC"/>}
                showTagType={true}
                showTagSectionName={true}
                onReorder={onUpdate}
                removeAction={onTagRemoved}
                theme={editableUiTheme}
              />
            </div>
          )}
          {hasWarning({notification})
            ? <p className="form__message form__message--warning">
              {notification?.message}
              </p>
            : ''}
          {hasError({notification})
            ? <p className="form__message form__message--error">
              {notification?.message}
            </p>
            : ''}
        </>
      )}
      {!editable && renderReadOnly()}
    </>
  );
 };

 export default StandTagPicker;
