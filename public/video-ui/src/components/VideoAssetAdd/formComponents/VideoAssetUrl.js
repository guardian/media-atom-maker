import React from 'react';

export default class VideoAssetUrl extends React.Component {

  state = {
    asset: {
      uri: ""
    }
  };

  onUpdateAssetUrl = (e) => {
    const newAsset = Object.assign({}, this.state.asset, {
      uri: e.target.value
    });
    this.setState({
      asset: newAsset
    });
  };

  onAssetSave = () => {
    this.props.createAsset(this.state.asset);
    this.props.hideAssetForm();
  };

  render () {
    if (!this.props.video) {
      return false;
    }

    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
      <div>
        <div className="form__row">
          <label className="form__label">Asset Url</label>
          <input { ...this.props.input}
                 className={"form__field " + (hasError ? "form__field--error" : "")}
                 type="text" value={this.state.asset.uri || ""}
                 onChange={this.onUpdateAssetUrl} />
          {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
        </div>
        <div className="btn__group">
          <button className="btn" type="button" disabled={!this.state.asset.uri} onClick={this.onAssetSave}>Save</button>
          <button className="btn" type="button" onClick={this.props.hideAssetForm}>Cancel</button>
        </div>
      </div>
    );
  }
}
