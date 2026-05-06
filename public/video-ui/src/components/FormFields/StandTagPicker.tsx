import React, { useEffect, useState } from 'react';
import { TagAutocomplete, TagTable } from '@guardian/stand/tag-picker';
import type { Tag } from '../../services/tagmanager';
import { getTagByPath, getTagsByType } from '../../services/tagmanager';
import BinSvg from "../../../images/bin.svg?react";
import TagTypes from '../../constants/TagTypes';

let pseudoIdCounter = -100; // For generating unique IDs for tags that don't exist in the tag manager

const generatePseudoId = () => {
  pseudoIdCounter -= 1;
  return pseudoIdCounter;
}

type VideoTag = {
  id: number;
  path: string;
  name: string;
  type: string;
  sectionName: string;
};

interface StandTagPickerProps {
  tagManagerUrl?: string;
  tagTypes: string[];
  fieldName: string;
  fieldValue?: string[];
  onUpdateField: (newValue: string[]) => void | Promise<void>;
}

const videoTagFromTagManager = (data: Tag): VideoTag => {
  return {
    id: data.id,
    path: data.path,
    name: data.internalName,
    type: data.type,
    sectionName: data.section.name,
  };
};

const videoTagsToStringList = (tags: VideoTag[]) => {
  return tags.map(tag => tag.path);
};

const fallbackVideoTagFromString = (tagPath: string): VideoTag => {
  return {
    id: generatePseudoId(),
    path: tagPath,
    name: tagPath,
    type: '',
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
}



export const StandTagPicker = ({ tagManagerUrl, tagTypes, fieldName, fieldValue, onUpdateField }: StandTagPickerProps) => {

  const [selectedTags, setSelectedTags] = useState<VideoTag[]>([]);
  const [options, setOptions] = useState<VideoTag[]>([]);
  const [value, setValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);



  const onTextInputChange = (inputText: string) => {
    setValue(inputText);
    if (inputText === '') {
      setOptions([]);
      return;
    }
    if (tagManagerUrl) {
      setIsLoading(true);
      getTagsByType(tagManagerUrl, inputText, tagTypes)
        .then(response => {
          const tags = response.data
            .map((tagItem) => tagItem.data)
            .map(videoTagFromTagManager)
            .filter((tag) => !selectedTags.some((selectedTag) => selectedTag.id === tag.id));
          setOptions(tags);
        })
        .catch(() => {
        })
        .finally(() => {
          setIsLoading(false);
        });
      }
  };

  const onUpdate = (newTags: VideoTag[]) => {
    setSelectedTags(newTags);
    const newValue = videoTagsToStringList(newTags);
    onUpdateField(newValue);
  }

  const onTagAdded = (tag: VideoTag) => {
    if (! selectedTags.some((t) => t.id === tag.id)) {
      const newTags = [...selectedTags, tag];
      onUpdate(newTags);
    }
  }

  const onTagRemoved = (tag: VideoTag) => {
    const index = selectedTags.findIndex((t) => t.id === tag.id);
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
  }, [fieldValue, tagManagerUrl]);

  return (
    <>
      <div className="form__row">
        <div className="form__label__layout">
          <label className="form__label">{fieldName}</label>
        </div>
      </div>
      <TagAutocomplete
        onTextInputChange={onTextInputChange}
        options={options}
        label="Tags"
        addTag={onTagAdded}
        loading={isLoading}
        placeholder={''}
        disabled={false}
        value={value}
      />
      <TagTable
        rows={selectedTags}
        filterRows={() => true}
        removeIcon={<BinSvg />}
      	showTagType={true}
		    showTagSectionName={true}
 				onReorder={onUpdate}
				removeAction={onTagRemoved}
      />
    </>
  );
 };

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { AppConfig } from '../../slices/config';

function mapStateToProps(state: {config: AppConfig}) {
  return {
    tagManagerUrl: state.config.tagManagerUrl
  };
}

export default connect(mapStateToProps)(StandTagPicker);
