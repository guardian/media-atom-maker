import React from 'react';

export default class VideoLicenseEdit extends React.Component {

  onUpdateLicense = (e) => {

    let newMetaData = Object.assign({}, this.props.video.data.metadata, {
      license: e.target.value
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
          <label className="form__label">License</label>
          <input { ...this.props.input} className={"form__field " + (hasError ? "form__field--error" : "")} type="text" value={this.props.video.data.metadata.license || ""} onChange={this.onUpdateLicense} />
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        </div>
    );
  }
}
