import React from 'react';

export default class Icon extends React.Component {
  renderText () {
    if (this.props.children) {
      return (
        <span className="icon--text">{this.props.children}</span>
      );
    }
}

  render () {
    if (! this.props.icon) {
      return;
    }

    return (
      <span {...this.props}>
        <i className="icon">{this.props.icon}</i>
        {this.renderText()}
      </span>
    );
  }
}
