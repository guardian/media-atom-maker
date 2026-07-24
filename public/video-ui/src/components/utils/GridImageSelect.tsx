import React, { RefObject } from 'react';
import Modal from './Modal';
import { parseImageFromGridCrop } from '../../util/parseGridMetadata';
import Logger from '../../logger';
import Icon from '../Icon';

type Props = {
  image: any;
  gridUrl: string;
  gridDomain: string;
  disabled: boolean;
  updateVideo: (...args: any[]) => any;
  fieldLocation?: 'posterImage' | 'trailImage' | 'youtubeOverrideImage';
};

type State = {
  dialogRef: undefined | RefObject<unknown>;
};

export default class GridImageSelect extends React.Component<Props, State> {
  state = {
    // @ts-expect-error TS(7018): Object literal's property 'dialogRef' implicitly h... Remove this comment to see the full error message
    dialogRef: undefined
  };

  constructor(props: Props) {
    super(props);
    this.state.dialogRef = React.createRef();
  }

  openGridModal = () => {
    window.addEventListener('message', this.onMessage, false);
    this.state.dialogRef.current?.showModal();
  };

  closeGridModal = () => {
    this.state.dialogRef.current?.close();
    window.removeEventListener('message', this.onMessage, false);
  };

  onUpdatePosterImage = (cropData: any, imageData: any) => {
    const image = parseImageFromGridCrop(cropData, imageData);

    this.props.updateVideo(image, this.props.fieldLocation);
  };

  validMessage(data: { crop: { data: any }; image: { data: any } }) {
    return data && data.crop && data.crop.data && data.image && data.image.data;
  }

  onMessage = (event: { origin: string; data: any }) => {
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

    this.closeGridModal();
    this.onUpdatePosterImage(data.crop.data, data.image);
  };

  render() {
    return (
      <div className="gridembedder">
        <button disabled={this.props.disabled} onClick={this.openGridModal}>
          <Icon
            icon="add_to_photos"
            className={'icon__edit ' + (this.props.disabled ? 'disabled' : '')}
          />
        </button>

        <Modal ref={this.state.dialogRef} onCloseModal={this.closeGridModal}>
          <iframe className="gridembedder__modal" src={this.props.gridUrl} />
        </Modal>
      </div>
    );
  }
}
