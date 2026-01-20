import React from 'react';

export default class Modal extends React.Component {
  props: React.PropsWithChildren<{
    onCloseModal?: () => void;
  }>;
  state: { dialogRef : React.RefObject<HTMLDialogElement> | undefined, isOpen: boolean } = {
    dialogRef: undefined,
    isOpen: false
  };

  constructor(props: React.PropsWithChildren) {
    super(props);
    this.state.dialogRef = React.createRef<HTMLDialogElement>();
  }

  showModal() {
    this.state.dialogRef.current?.showModal();
    this.setState({ isOpen: true });
  }

  close() {
    this.state.dialogRef.current?.close();
  }

  handleCloseEvent = () => {
    this.props?.onCloseModal?.();
    this.setState({ isOpen: false });
  };

  componentDidMount() {
    this.state.dialogRef.current.addEventListener("close", this.handleCloseEvent);
  }
  componentWillUnmount() {
    this.state.dialogRef.current.removeEventListener("close", this.handleCloseEvent);
  }

  render() {
    return (
      <dialog className="modal" ref={this.state.dialogRef} onClick={() => this.close()}>
        {this.state.isOpen &&
            <div
                className="modal__content"
                onClick={(e) => {
                  // Prevent clicks on modal content from closing the modal
                  e.stopPropagation();
                }}>
              {this.props.children}
            </div>
        }
        <div className="modal__content__header">
          <button
            className="i-cross button__secondary modal__dismiss"
            onClick={() => this.close()}
          >
            Close
          </button>
        </div>
      </dialog>
    );
  }
}
