import React from 'react';

class KeywordPicker extends React.Component {

  state = {
    newKeywordText: ""
  }

  updateNewKeywordText = (e) => {
    this.setState({
      newKeywordText: e.target.value || ""
    });
  }

  addKeyword = () => {
    const oldKeywords = this.props.keywords ? this.props.keywords : [];
    const newKeywords = oldKeywords.concat([this.state.newKeywordText]);
    this.props.updateKeywords(newKeywords);
    this.setState({
      newKeywordText: ""
    });
  }

  isValidKeyword = () => {

    if (!this.state.newKeywordText) {
      return false;
    }

    //Validation against existing keywords
    if (!this.props.keywords) {
      return true; // If there are no existing keywords then this should be valid
    }

    if (this.props.keywords.indexOf(this.state.newKeywordText) !== -1) {
      return false; // Is there already an matching keyword
    }

    return true
  }

  removeKeyword = (keyword) => {
    const newKeywords = this.props.keywords.filter((k) => k !== keyword)
    this.props.updateKeywords(newKeywords)
  }

  renderKeyword = (keyword) => {

    return (
      <div className="keywords__item" key={keyword}>
        <div className="keyword__item__text">{keyword}</div>
        <div className="keyword__item__remove" onClick={this.removeKeyword.bind(this, keyword)}>X</div>
      </div>
    )
  }

  renderKeywords = () => {

    if (!this.props.keywords || !this.props.keywords.length) {
      return (
        <span className="keywords__message">No Keywords</span>
      )
    }

    return this.props.keywords.map(this.renderKeyword)
  }

  render() {


    return (
      <div className="keywords">
        {this.renderKeywords()}
        <div className="keywords__add">
          <input type="text" className="form__field" value={this.state.newKeywordText} onChange={this.updateNewKeywordText} placeholder="Enter new keyword..." />
          <button disabled={!this.isValidKeyword()} className="keywords__add__button btn" onClick={this.addKeyword}>Add</button>
        </div>
      </div>
    );
  }

}

export default KeywordPicker;
