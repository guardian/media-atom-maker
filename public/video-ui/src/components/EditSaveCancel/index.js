import React from 'react';
import PropTypes from 'prop-types';
import Icon from '../Icon';

class EditSaveCancel extends React.Component {
  static propTypes = {
    onEdit: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
    canSave: PropTypes.func.isRequired,
  };

  state = {
    editing: false
  };

  updateEditingState({ editing, save }) {
    this.setState({editing: editing});
    this.props.onEdit(editing);

    if (!editing) {
      if (save) {
        this.props.onSave();
      } else {
        this.props.onCancel();
      }
    }
  }

  renderEditButton() {
    return (
      <button onClick={() => this.updateEditingState({ editing: true })}>
        <Icon icon="edit" className="icon__edit">
          Edit
        </Icon>
      </button>
    );
  }

  renderSaveButton() {
    return (
      <button
        onClick={() => this.updateEditingState({editing: false, save: true})}
        disabled={!this.props.canSave()}>
        <Icon icon="save" className={`icon__done ${this.props.canSave() ? '' : 'disabled'}`}>
          Save changes
        </Icon>
      </button>
    );
  }

  renderCancelButton() {
    return (
      <button onClick={() => this.updateEditingState({editing: false, save: false})}>
        <Icon icon="cancel" className="icon__cancel">Cancel</Icon>
      </button>
    );
  }

  render() {
    const { editing } = this.state;

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
