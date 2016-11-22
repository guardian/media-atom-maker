import React from 'react';
import KeywordPicker from '../../utils/KeywordPicker';

class YoutubeKeywordsSelect extends React.Component {

  updateKeywords = (keywords) => {
    const newMetadata = Object.assign({}, this.props.video.data.metadata, {
      tags: keywords
    });

    const newData = Object.assign({}, this.props.video.data, {
      metadata: newMetadata
    });

    this.props.updateVideo(Object.assign({}, this.props.video, {
      data: newData
    }));
  };

  render() {
    return (
      <div className="keyword-select">
        <div className="">
          <KeywordPicker keywords={this.props.video.data.metadata.tags} updateKeywords={this.updateKeywords}/>
        </div>
      </div>
    );
  }

}

export default YoutubeKeywordsSelect;
