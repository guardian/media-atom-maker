import React from 'react';

export default class TagFieldValue extends React.Component {

  renderFieldValue(value, index) {
    if (value.webTitle) {
      return (
        <span key={`${value.id}-${index}`}>
          <span className="form__field__tag__display">{value.webTitle}</span>
          {' '}
        </span>
      );

    }
    return `${value} `;
  }

  render() {
    return (
      <span>
        {this.props.tagValue.map(this.renderFieldValue)}
      </span>
    );

  }
}
