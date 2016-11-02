/**
 * Created by shaun_dillon on 01/11/2016.
 */
import React from 'react';

export default class VideoAssetUrl extends React.Component {

  onUpdateAssetUrl = (e) => {
    let newAssetData = Object.assign({}, this.props.asset, {
      title: e.target.value
    });

    this.props.updateAsset(Object.assign({}, this.props.asset, {
      url: e.target.value
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
          <input { ...this.props.input} className={"form__field " + (hasError ? "form__field--error" : "")} type="text" value={this.props.asset.id || ""} onChange={this.onUpdateAssetUrl} />
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        </div>
    );
  }
}
