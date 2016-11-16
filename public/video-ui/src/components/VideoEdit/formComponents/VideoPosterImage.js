import React from 'react';

export default class VideoPosterImageEdit extends React.Component {

  onUpdatePosterImage = (e) => {
    let newData = Object.assign({}, this.props.video, {
      posterUrl: e.target.value
    });

    this.props.updateVideo(newData);
  };

  render () {
    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
        <div className="form__row">
          <label className="form__label">Poster image</label>
          <input { ...this.props.input} className={"form__field " + (hasError ? "form__field--error" : "")} type="text" value={this.props.video.posterUrl || ""} onChange={this.onUpdatePosterImage} />
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        </div>
    );
  }
}


