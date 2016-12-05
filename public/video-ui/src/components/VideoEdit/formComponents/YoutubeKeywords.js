import React from 'react';
import KeywordPicker from '../../utils/KeywordPicker';

class YoutubeKeywordsSelect extends React.Component {

  updateKeywords = (keywords) => {
    this.props.saveAndUpdateVideo(Object.assign({}, this.props.video, {
      tags: keywords
    }));
  };

  render() {
    return (
      <div className="form__row">
        <label className="form__label">Keywords</label>
        <KeywordPicker keywords={this.props.video.tags} updateKeywords={this.updateKeywords}/>
      </div>
    );
  }

}

export default YoutubeKeywordsSelect;
