import React from 'react';

export default class VideoTagsEdit extends React.Component {

  onUpdateTags = (e) => {

    let newMetaData = Object.assign({}, this.props.video.data.metadata, {
      tags: e.target.value
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
          <label className="form__label">Tags</label>
          <input { ...this.props.input} className={"form__field " + (hasError ? "form__field--error" : "")} type="text" value={this.props.video.data.metadata.tags || ""} onChange={this.onUpdateTags} />
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        </div>
    );
  }
}
