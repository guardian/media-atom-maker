import React from 'react';

export default class TagFieldValue extends React.Component {

  renderFieldValue(value, index, arr) {
    if (value.webTitle) {
      const isLastItem = index === arr.length - 1;

      return (
        <span key={`${value.id}-${index}`}>
          <span className="form__field__tag__display">{value.webTitle}</span>
          {isLastItem ? '' : ', '}
        </span>
      );

    }

    if (index === 0 || value === ',') {
      return value;
    }

    return ` ${value}`;
  }

  render() {
    return (
      <span>
        {this.props.tagValue.map(this.renderFieldValue)}
      </span>
    );

  }
}
