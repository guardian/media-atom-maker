import React from 'react';
import Modal from './Modal';

const GRID_URL = 'https://media.gutools.co.uk';

export default class GridEmbedder extends React.Component {

    state = {
        modalOpen: false
    }

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

        console.log("message receiveD")

        if (event.origin !== GRID_URL) {
            console.log("didn't come from the grid");
            return;
        }

        const data = event.data;

        if (!data) {
            console.log("got no data...");
            return;
        }

        if (!this.validMessage(data)) {
            console.log("not a valid message...");
            return;
        }

        // const image = fetchImageData(data.image, data.crop.data);
        console.log(data.image, data.crop.data)
        this.closeModal();

        this.props.onEmbed(image);
    }


    render() {

        return (
            <div className="gridembedder">
                <div className="gridembedder__button" onClick={this.toggleModal}>
                    Add Image from Grid
                </div>

                <Modal isOpen={this.state.modalOpen} dismiss={this.closeModal}>
                    <iframe className="gridembedder__modal" src={GRID_URL}></iframe>
                </Modal>
            </div>
        );
    }
}