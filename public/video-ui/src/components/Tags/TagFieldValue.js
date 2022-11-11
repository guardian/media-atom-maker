import React from 'react';

export default class TagFieldValue extends React.Component {
  renderFieldValue(value, index) {
    if (value.detailedTitle) {
      return (
        <span key={`${value.id}-${index}`}>
          <span className="form__field__tag__display">
            {value.detailedTitle}
          </span>{' '}
        </span>
      );
    } else if (value.webTitle) {
      // In YouTube Furniture tab, the `keyword` field passes an object with webTitle to this component
      return value.webTitle;
    }

    if (index === 0 || value === ',') {
      return value;
    }

    return ` ${value}`;
  }

  render() {
    return <span>{this.props.tagValue.map(this.renderFieldValue).join(',')}</span>;
  }
}
