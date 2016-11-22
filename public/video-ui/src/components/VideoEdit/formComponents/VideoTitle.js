import React from 'react';

export default class VideoTitleEdit extends React.Component {

  onUpdateTitle = (e) => {
    let newData = Object.assign({}, this.props.video, {
      title: e.target.value
    });

    this.props.updateVideo(newData);
  };

  render () {
    if (!this.props.video) {
      console.log('VideoEdit loaded without video provided');
      return false;
    }

    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
        <div className="form__row">
          <label className="form__label">Title</label>
          <input { ...this.props.input} className={"form__field " + (hasError ? "form__field--error" : "")} type="text" value={this.props.video.title || ""} onChange={this.onUpdateTitle} />
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        </div>
    );
  }
}
