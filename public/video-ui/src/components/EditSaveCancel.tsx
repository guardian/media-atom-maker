import React from 'react';
import Icon from './Icon';

type EditSaveCancelProps = {
  editing: boolean;
  onEdit: () => void;
  onSave: () => void;
  onCancel: () => void;
  canSave: boolean;
  canCancel?: boolean;
};

export const EditSaveCancel = ({
  editing,
  onEdit,
  onSave,
  onCancel,
  canSave,
  canCancel = true
}: EditSaveCancelProps) => {
  if (editing) {
    return (
      <div>
        <button onClick={onSave} disabled={!canSave}>
          <Icon
            icon="save"
            className={`icon__done ${canSave ? '' : 'disabled'}`}
          >
            Save changes
          </Icon>
        </button>
        <button onClick={onCancel} disabled={!canCancel}>
          <Icon
            icon="cancel"
            className={`icon__cancel ${canCancel ? '' : 'disabled'}`}
          >
            Cancel
          </Icon>
        </button>
      </div>
    );
  }

  return (
    <button onClick={onEdit}>
      <Icon icon="edit" className="icon__edit">
        Edit
      </Icon>
    </button>
  );
};
