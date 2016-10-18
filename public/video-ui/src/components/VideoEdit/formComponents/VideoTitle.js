import React from 'react';

export default class VideoTitleEdit extends React.Component {

  constructor(props) {
    super(props);
  }


  onUpdateTitle = (e) => {
    this.props.updateVideo(Object.assign({}, this.props.video, {
      title: e.target.value
    }));
  };

  render () {
    if (!this.props.video) {
      console.log('VideoEdit loaded without video provided');
      return false;
    }

    return (
        <div className="form__row">
          <label className="form__label">Title</label>
          <input className="form__field" type="text" value={this.props.video.title || ""} onChange={this.onUpdateTitle} disabled={!this.props.videoEditable}/>
        </div>
    );
  }
}
