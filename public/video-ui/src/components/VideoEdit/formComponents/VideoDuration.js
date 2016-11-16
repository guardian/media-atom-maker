import React from 'react';

export default class VideoDurationEdit extends React.Component {

  onUpdateDuration= (e) => {
    let newData = Object.assign({}, this.props.video, {
      duration: e.target.value
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
          <label className="form__label">Duration (in seconds)</label>
          <input { ...this.props.input} className={"form__field " + (hasError ? "form__field--error" : "")} type="number" value={this.props.video.duration || ""} onChange={this.onUpdateDuration} />
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        </div>
    );
  }
}
