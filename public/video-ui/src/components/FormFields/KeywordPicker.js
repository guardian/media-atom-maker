import React from 'react';

class KeywordPicker extends React.Component {
  state = {
    newKeywordText: ''
  };

  updateNewKeywordText = e => {
    this.setState({
      newKeywordText: e.target.value || ''
    });
  };

  addKeyword = () => {
    const oldKeywords = this.props.fieldValue ? this.props.fieldValue : [];
    const newKeywords = oldKeywords.concat([this.state.newKeywordText]);
    this.props.onUpdateField(newKeywords);
    this.setState({
      newKeywordText: ''
    });
  };

  onInputKeyDown = e => {
    if (e.key === 'Enter') {
      e.preventDefault();
      this.addKeyword();
    }
  };

  isValidKeyword = () => {
    if (!this.state.newKeywordText) {
      return false;
    }

    //Validation against existing keywords
    if (!this.props.fieldValue) {
      return true; // If there are no existing keywords then this should be valid
    }

    if (this.props.fieldValue.indexOf(this.state.newKeywordText) !== -1) {
      return false; // Is there already an matching keyword
    }

    return true;
  };

  removeKeyword = keyword => {
    const newKeywords = this.props.fieldValue.filter(k => k !== keyword);
    this.props.onUpdateField(newKeywords);
  };

  renderKeyword = keyword => {
    return (
      <div className="keywords__item" key={keyword}>
        <div className="keyword__item__text">{keyword}</div>
        <button
          className="keyword__item__remove"
          disabled={!this.props.editable}
          onClick={this.removeKeyword.bind(this, keyword)}
        >
          X
        </button>
      </div>
    );
  };

  renderKeywords = () => {
    if (!this.props.fieldValue || !this.props.fieldValue.length) {
      return <span className="details-list__empty">No Keywords</span>;
    }

    return this.props.fieldValue.map(this.renderKeyword);
  };

  renderInputField = () => {
    if (this.props.editable) {
      return (
        <input
          type="text"
          className="form__field"
          value={this.state.newKeywordText}
          onChange={this.updateNewKeywordText}
          onKeyDown={this.onInputKeyDown}
          placeholder="Enter new keyword..."
        />
      );
    }
  };

  render() {
    return (
      <div className="form__row">
        <label className="form__label">{this.props.fieldName}</label>
        <div className="keywords">
          {this.renderKeywords()}
          <div className="keywords__add">
            {this.renderInputField()}
          </div>
        </div>
      </div>
    );
  }
}

export default KeywordPicker;
