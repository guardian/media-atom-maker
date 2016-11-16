import React from 'react';

export default class VideoCommentsEdit extends React.Component {

  onUpdateComments = (e) => {
    console.log(e.target.checked);
    let newMetaData = Object.assign({}, this.props.video.data.metadata, {
      commentsEnabled: e.target.checked
    });

    let newData = Object.assign({}, this.props.video.data, {
      metadata: newMetaData
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

    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
        <div className="form__row">
          <label className="form__label">Comments enabled</label>
          <input { ...this.props.input} className={"form__field " + (hasError ? "form__field--error" : "")} type="checkbox" checked={this.props.video.data.metadata.commentsEnabled || false} onChange={this.onUpdateComments} />
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        </div>
    );
  }
}
