import React, {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState
} from 'react';

export interface ModalHandle {
  showModal: () => void;
  close: () => void;
}

type Props = React.PropsWithChildren<{
  onCloseModal?: () => void;
}>;

const Modal = forwardRef<ModalHandle, Props>(function Modal(
  { onCloseModal, children },
  ref
) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const [isOpen, setIsOpen] = useState(false);

  useImperativeHandle(ref, () => ({
    showModal() {
      dialogRef.current?.showModal();
      setIsOpen(true);
    },
    close() {
      dialogRef.current?.close();
    }
  }));

  useEffect(() => {
    const dialog = dialogRef.current;
    const handleClose = () => {
      onCloseModal?.();
      setIsOpen(false);
    };
    dialog?.addEventListener('close', handleClose);
    return () => {
      dialog?.removeEventListener('close', handleClose);
    };
  }, [onCloseModal]);

  return (
    <dialog
      className="modal"
      ref={dialogRef}
      onClick={() => dialogRef.current?.close()}
    >
      {isOpen && (
        <div
          className="modal__content"
          onClick={e => {
            // Prevent clicks on modal content from closing the modal
            e.stopPropagation();
          }}
        >
          {children}
        </div>
      )}
      <div className="modal__content__header">
        <button
          className="i-cross button__secondary modal__dismiss"
          onClick={() => dialogRef.current?.close()}
        >
          Close
        </button>
      </div>
    </dialog>
  );
});

export default Modal;
