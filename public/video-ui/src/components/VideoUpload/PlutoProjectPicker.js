import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';

export default function PlutoProjectPicker({ video, projects, saveVideo }) {
  return (
    <div>
      <div className="video__detailbox__header__container">
        <header className="video__detailbox__header">
          Pluto
        </header>
      </div>
      <div className="form__group">
        <ManagedForm
          data={video}
          updateData={saveVideo}
          editable={true}
          formName="Pluto"
        >
          <ManagedField
            fieldLocation="plutoData.projectId"
            name="Project"
            isRequired={false}
          >
            <SelectBox selectValues={projects} />
          </ManagedField>
        </ManagedForm>
      </div>
    </div>
  );
}
