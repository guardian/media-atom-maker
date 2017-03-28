import React from 'react';
import KeywordPicker from '../utils/KeywordPicker';

class ItemPicker extends React.Component {

  render() {
    return (
      <div className="form__row">
        <label className="form__label">Keywords</label>
        <KeywordPicker
          keywords={this.props.fieldValue}
          updateKeywords={(keywords) => {this.props.onUpdateField(keywords);}}
          editable={this.props.editable}/>
      </div>
    );
  }

}

export default ItemPicker;
