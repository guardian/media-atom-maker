import React from 'react';
import Modal from './Modal';
import { parseImageFromGridCrop } from '../../util/parseGridMetadata';
import Logger from '../../logger';

export default class GridEmbedder extends React.Component {

    state = {
        modalOpen: false
    }

    onUpdatePosterImage = (cropData) => {

      const image = parseImageFromGridCrop(cropData);

      const newData = Object.assign({}, this.props.video, {
        posterImage: image
      });

      console.log(newData);

      this.props.saveAndUpdateVideo(newData);
    };

    toggleModal = () => {
        if (this.state.modalOpen) {
            this.closeModal();
        } else {
            this.openModal();
        }
    }

    closeModal = () => {
        this.setState({ modalOpen: false });
        window.removeEventListener('message', this.onMessage, false);
    }

    openModal = () => {
        this.setState({ modalOpen: true });
        window.addEventListener('message', this.onMessage, false);
    }

    validMessage(data) {
        return data && data.crop && data.crop.data && data.image && data.image.data;
    }

    onMessage = (event) => {
        if (event.origin !== this.props.gridUrl) {
            Logger.log("didn't come from the grid");
            return;
        }

        const data = event.data;

        if (!data) {
            Logger.log("got no data...");
            return;
        }

        if (!this.validMessage(data)) {
            Logger.log("not a valid message...");
            return;
        }

        this.closeModal();
        this.onUpdatePosterImage(data.crop.data);
    }


    render() {

        return (
            <div className="gridembedder">
                <div className="gridembedder__button" onClick={this.toggleModal}>
                    <i className="icon icon__edit">add_to_photos</i>
                </div>

                <Modal isOpen={this.state.modalOpen} dismiss={this.closeModal}>
                    <iframe className="gridembedder__modal" src={this.props.gridUrl}></iframe>
                </Modal>
            </div>
        );
    }
}
