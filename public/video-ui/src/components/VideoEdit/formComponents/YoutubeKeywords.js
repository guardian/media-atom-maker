import React from 'react';
import KeywordPicker from '../../utils/KeywordPicker';

class YoutubeKeywordsSelect extends React.Component {

  updateKeywords = (keywords) => {
    this.props.updateVideo(Object.assign({}, this.props.video, {
      tags: keywords
    }));
  };

  render() {
    return (
      <div className="keyword-select">
        <div className="">
          <KeywordPicker keywords={this.props.video.tags} updateKeywords={this.updateKeywords}/>
        </div>
      </div>
    );
  }

}

export default YoutubeKeywordsSelect;
