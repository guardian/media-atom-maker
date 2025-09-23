import React from 'react';
import PropTypes from 'prop-types';
import ReactTooltip from 'react-tooltip';
import Icon from './Icon';

export default class DeleteButton extends React.Component {
  static propTypes = {
    tooltip: PropTypes.string.isRequired,
    onDelete: PropTypes.func.isRequired
  };

  state = {
    confirmDelete: false
  };

  changeState() {
    this.setState({ confirmDelete: true });

    setTimeout(() => {
      this.setState({ confirmDelete: false });
    }, 2000);
  }

  renderConfirmDelete() {
    return (
      <button
        className="btn button__secondary--remove-confirm"
        onClick={this.props.onDelete}
        data-tip="Confirm delete. This cannot be undone."
        data-testid="delete-button"
      >
        <Icon icon="delete_forever">Confirm delete</Icon>
      </button>
    );
  }

  renderDelete() {
    return (
      <button
        className="btn button__secondary--remove"
        onClick={() => this.changeState()}
        data-tip={this.props.tooltip}
        data-testid="delete-button"
      >
        <Icon icon="delete">Delete</Icon>
      </button>
    );
  }

  render() {
    return (
      <>
        <ReactTooltip />
        {this.state.confirmDelete
          ? this.renderConfirmDelete()
          : this.renderDelete()}
      </>
    );
  }
}
