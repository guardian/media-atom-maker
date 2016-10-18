import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoPosterImageEdit from './formComponents/VideoPosterImage';


export default class VideoEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {
    if (!this.props.video) {
      console.log('VideoEdit loaded without video provided');
      return false;
    }

    return (
        <div>
          <VideoTitleEdit {...this.props}/>
          <VideoCategorySelect {...this.props}/>
          <VideoPosterImageEdit {...this.props}/>
        </div>
    );
  }
}
