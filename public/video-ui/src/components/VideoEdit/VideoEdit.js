import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';


export default class VideoEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {
    if (!this.props.video || !this.props.video.data) {
      console.log('VideoEdit loaded without video provided');
      return false;
    }

    return (
        <form className="form">
          <VideoTitleEdit {...this.props}/>
        </form>
    );
  }
}
