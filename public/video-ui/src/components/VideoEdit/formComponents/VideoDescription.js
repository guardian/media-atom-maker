import React from 'react';

export default class VideoDescriptionEdit extends React.Component {

  onUpdateDescription = (e) => {
    let newData = Object.assign({}, this.props.video.data, {
      description: e.target.value
    });

    this.props.updateVideo(Object.assign({}, this.props.video, {
      data: newData
    }));
  };

  render () {
    console.log(this.props.meta);
    if (!this.props.video) {
      console.log('VideoEdit loaded without video provided');
      return false;
    }

    const hasError = this.props.meta.touched && this.props.meta.error;
    const hasWarning = this.props.meta.touched && this.props.meta.warning;

    return (
        <div className="form__row">
          <label className="form__label">Description</label>
          <textarea { ...this.props.input} className={"form__field " + (hasError ? "form__field--error" : "")} value={this.props.video.data.description || ""} type="text" onChange={this.onUpdateDescription}>
            {this.props.video.data.description}
          </textarea>
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
          {hasWarning ? <p className="form__message form__message--error">{this.props.meta.warning}</p> : ""}
        </div>
    );
  }
}
