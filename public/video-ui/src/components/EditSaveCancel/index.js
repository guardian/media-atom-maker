import React from 'react';
import PropTypes from 'prop-types';
import Icon from '../Icon';

class EditSaveCancel extends React.Component {
  static propTypes = {
    editing: PropTypes.bool.isRequired,
    onEdit: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
    canSave: PropTypes.func.isRequired,
    canCancel: PropTypes.func
  };

  renderEditButton() {
    const { onEdit } = this.props;

    return (
      <button onClick={onEdit}>
        <Icon icon="edit" className="icon__edit">
          Edit
        </Icon>
      </button>
    );
  }

  renderSaveButton() {
    const { canSave, onSave } = this.props;

    return (
      <button onClick={onSave} disabled={!canSave()}>
        <Icon icon="save" className={`icon__done ${canSave() ? '' : 'disabled'}`}>
          Save changes
        </Icon>
      </button>
    );
  }

  renderCancelButton() {
    const { canCancel, onCancel } = this.props;

    return (
      <button onClick={onCancel} disabled={canCancel ? !canCancel() : false}>
        <Icon icon="cancel" className={`icon__cancel ${canCancel ? (canCancel() ? '' : 'disabled') : ''}`}>Cancel</Icon>
      </button>
    );
  }

  render() {
    const { editing } = this.props;

    if (editing) {
      return (
        <div>
          {this.renderSaveButton()}
          {this.renderCancelButton()}
        </div>
      );
    }

    return this.renderEditButton();
  }
}

export default EditSaveCancel;
