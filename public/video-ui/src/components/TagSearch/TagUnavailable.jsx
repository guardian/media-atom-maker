 import React from 'react';

 export default class TagUnavailable extends React.Component {

  render() {
    if (this.props.capiUnavailable) {
      return (
        <div className="form__field--external-error">
          Tags are currently unavailable
        </div>
      );
    }

    return null;
  }

 }
