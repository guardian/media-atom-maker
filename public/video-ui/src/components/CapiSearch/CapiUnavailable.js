 import React from 'react';

 export default class CapiUnavailable extends React.Component {

  render() {
    if (this.props.capiUnavailable) {
      return (
        <span className="form__field--external__error">
          Tags are currently unavailable
        </span>
      );
    }

    return null;
  }

 }
