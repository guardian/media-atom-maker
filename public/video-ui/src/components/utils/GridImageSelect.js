import React from 'react';
import Modal from './Modal';
import { parseImageFromGridCrop } from '../../util/parseGridMetadata';
import Logger from '../../logger';
import Icon from '../Icon';

export default class GridEmbedder extends React.Component {
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
    if (event.origin !== this.props.gridUrl) {
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

  getGridUrl = () => {

    if (this.props.posterImage && this.props.isComposerImage) {
      const imageIdParts = this.props.posterImage.mediaId.split("/");
      const imageGridId = imageIdParts[imageIdParts.length - 1];
      return this.props.gridUrl + '/images/' + imageGridId + '/crop?cropType=composer';
    }

    return this.props.gridUrl + '?cropType=video';
  }

  render() {

    const gridUrl = this.getGridUrl();

    return (
      <div className="gridembedder">
        <button disabled={this.props.disabled} onClick={this.toggleModal}>
          <Icon
            icon="add_to_photos"
            className={
              'icon__edit ' + (this.props.disabled ? 'disabled' : '')
            }
          />
        </button>

        <Modal isOpen={this.state.modalOpen} dismiss={this.closeModal}>
          <iframe className="gridembedder__modal" src={gridUrl} />
        </Modal>
      </div>
    );
  }
}
