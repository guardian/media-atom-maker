import React from 'react';

export default class TagFieldValue extends React.Component {
  
  renderFieldValue(value, index, array) {
    // Add a trailing comma if it's a youtube keyword field and it's not the last keyword in the array
    const shouldAddComma = (this?.props?.tagType === "youtube" && index !== array.length - 1);
    if (value.detailedTitle) {
      return (
        <span key={`${value.id}-${index}`}>
          <span className="form__field__tag__display">
            {value.detailedTitle}
          </span>
          {shouldAddComma ? ',' : ' '}
        </span>
      );
    } else if (value.webTitle) {
      // In a  YouTube Furniture tab with non-launched changes, the `keyword` field 
      //passes an object with webTitle to this component
      const titleToRender = value.webTitle.concat(shouldAddComma ? ',' : '');
      return titleToRender;
    }

    if (index === 0 || value === ',') {
      return value;
    }

    return ` ${value}`;
  }
  getRenderedValues(){
    return this.props.tagValue.map((tagValue, index, array) => this.renderFieldValue(tagValue, index, array));
  }

  render() {
    return <span>{this.getRenderedValues()}</span>;
  }
}
