import React from 'react';

export default class Modal extends React.Component {

    constructor(props) {
        super(props);
    }

    preventClosingClick (event) {
        event.stopPropagation();
    }

    render() {
        if(!this.props.isOpen) {
            return false;
        }

        return (
            <div className="modal" onClick={this.props.dismiss}>
                <div className="modal__content" onClick={this.preventClosingClick}>
                    <div className="modal__content__header">
                        <span className="i-cross modal__dismiss" onClick={this.props.dismiss}>
                            Close
                        </span>
                    </div>
                    {this.props.children}
                </div>
            </div>
        );

    }
}