import React from 'react';
import Modal from './Modal';
import PropTypes from 'prop-types';
import { parseImageFromGridCrop } from '../../util/parseGridMetadata';
import Logger from '../../logger';
import Icon from '../Icon';

export default class GridImageSelect extends React.Component {
  static propTypes = {
    image: PropTypes.object.isRequired,
    gridUrl: PropTypes.string.isRequired,
    gridDomain: PropTypes.string.isRequired,
    disabled: PropTypes.bool.isRequired,
    updateVideo: PropTypes.func.isRequired,
    fieldLocation: PropTypes.oneOf(['posterImage', 'trailImage', 'youtubeOverrideImage'])
  };

  state = {
    modalOpen: false
  };

  onUpdatePosterImage = (cropData, imageData) => {
    const image = parseImageFromGridCrop(cropData, imageData);

    this.props.updateVideo(image, this.props.fieldLocation);
  };

  toggleModal = () => {
    if (this.state.modalOpen) {
      this.closeModal();
    } else {
      this.openModal();
    }
  };

  closeModal = () => {
    this.setState({ modalOpen: false });
    window.removeEventListener('message', this.onMessage, false);
  };

  openModal = () => {
    this.setState({ modalOpen: true });
    window.addEventListener('message', this.onMessage, false);
  };

  validMessage(data) {
    return data && data.crop && data.crop.data && data.image && data.image.data;
  }

  onMessage = event => {
    if (event.origin !== this.props.gridDomain) {
      Logger.log("didn't come from the grid");
      return;
    }

    const data = event.data;

    if (!data) {
      Logger.log('got no data...');
      return;
    }

    if (!this.validMessage(data)) {
      Logger.log('not a valid message...');
      return;
    }

    this.closeModal();
    this.onUpdatePosterImage(data.crop.data, data.image);
  };

  render() {
    return (
      <div className="gridembedder">
        <button disabled={this.props.disabled} onClick={this.toggleModal}>
          <Icon
            icon="add_to_photos"
            className={'icon__edit ' + (this.props.disabled ? 'disabled' : '')}
          />
        </button>

        <Modal isOpen={this.state.modalOpen}>
          <iframe className="gridembedder__modal" src={this.props.gridUrl} />
        </Modal>
      </div>
    );
  }
}
