import React, { useState } from 'react';
import { TagAutocomplete, TagTable } from '@guardian/stand/tag-picker';
import type { Tag } from '../../services/tagmanager';
import { getTagsByType } from '../../services/tagmanager';

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
}

export const StandTagPicker = ({ tagManagerUrl, tagTypes, fieldName }: StandTagPickerProps) => {

  const [selectedTags, setSelectedTags] = useState<VideoTag[]>([]);
  const [options, setOptions] = useState<VideoTag[]>([]);
  const [value, setValue] = useState('');

  const videoTagFromTagManager = (data: Tag): VideoTag => {
    return {
      id: data.id,
      path: data.path,
      name: data.internalName,
      type: data.type,
      sectionName: data.section.name,
    };
  };

  const onTextInputChange = (inputText: string) => {
    setValue(inputText);
    if (inputText === '') {
      setOptions([]);
      return;
    }

    tagManagerUrl && getTagsByType(tagManagerUrl, inputText, tagTypes)
        .then(response => {
          const tags = response.data
            .map((tagItem) => tagItem.data)
            .map(videoTagFromTagManager)
            .filter((tag) => !selectedTags.some((selectedTag) => selectedTag.id === tag.id));
          setOptions(tags);
        })
        .catch(() => {
        });
  };
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
        addTag={(tag) =>
          setSelectedTags((tags) => [...tags, tag])
        }
        loading={false}
        placeholder={''}
        disabled={false}
        value={value}
      />
      <TagTable rows={selectedTags} filterRows={() => true} />
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
