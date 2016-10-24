import React from 'react';

export default class VideoTitleEdit extends React.Component {

  constructor(props) {
    super(props);
  }


  onUpdateTitle = (e) => {
    let newData = Object.assign({}, this.props.video.data, {
      title: e.target.value
    });

    this.props.updateVideo(Object.assign({}, this.props.video, {
      data: newData
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
          <input { ...this.props.input} className="form__field" type="text" value={this.props.video.data.title || ""} onChange={this.onUpdateTitle} />
          {this.props.meta.touched && (this.props.meta.error && <span>{this.props.meta.error}</span>)}
        </div>
    );
  }
}
