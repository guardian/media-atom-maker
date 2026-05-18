import React, { useRef } from 'react';
import Logger from '../../logger';
import { parseImageFromGridCrop } from '../../util/parseGridMetadata';
import Icon from '../Icon';
import { ImageField } from '../VideoImages/VideoImages';
import Modal, { ModalHandle } from './Modal';

type Props = {
  image: any;
  gridUrl: string;
  gridDomain: string;
  disabled: boolean;
  updateVideo: (image: any, location: ImageField) => void;
  fieldLocation: ImageField;
};

export default function GridImageSelect(props: Props) {
  const dialogRef = useRef<ModalHandle>(null);

  const openGridModal = () => {
    window.addEventListener('message', onMessage, false);
    dialogRef.current?.showModal();
  };

  const closeGridModal = () => {
    dialogRef.current?.close();
    window.removeEventListener('message', onMessage, false);
  };

  const onUpdatePosterImage = (cropData, imageData) => {
    const image = parseImageFromGridCrop(cropData, imageData);

    props.updateVideo(image, props.fieldLocation);
  };

  const validMessage = data => {
    return data && data.crop && data.crop.data && data.image && data.image.data;
  };

  const onMessage = event => {
    if (event.origin !== props.gridDomain) {
      Logger.log("didn't come from the grid");
      return;
    }

    const data = event.data;

    if (!data) {
      Logger.log('got no data...');
      return;
    }

    if (!validMessage(data)) {
      Logger.log('not a valid message...');
      return;
    }

    closeGridModal();
    onUpdatePosterImage(data.crop.data, data.image);
  };

  return (
    <div className="gridembedder">
      <button disabled={props.disabled} onClick={openGridModal}>
        <Icon
          icon="add_to_photos"
          className={'icon__edit ' + (props.disabled ? 'disabled' : '')}
        />
      </button>

      <Modal ref={dialogRef} onCloseModal={closeGridModal}>
        <iframe className="gridembedder__modal" src={props.gridUrl} />
      </Modal>
    </div>
  );
}
