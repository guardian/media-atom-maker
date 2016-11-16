/**
 * Created by shaun_dillon on 01/11/2016.
 */
import React from 'react';

export default class VideoAssetUrl extends React.Component {

  onUpdateAssetUrl = (e) => {
    const newVersion = this.props.video.data.activeVersion ? this.props.video.activeVersion + 1 : 0;

    this.props.updateAsset(Object.assign({}, this.props.asset, {
      uri: e.target.value,
      version: newVersion
    }));
  };

  render () {
    if (!this.props.video) {
      console.log('VideoAssetAdd loaded without video provided');
      return false;
    }

    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
        <div className="form__row">
          <label className="form__label">Asset Url</label>
          <input { ...this.props.input} className={"form__field " + (hasError ? "form__field--error" : "")} type="text" value={this.props.asset.uri || ""} onChange={this.onUpdateAssetUrl} />
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        </div>
    );
  }
}
