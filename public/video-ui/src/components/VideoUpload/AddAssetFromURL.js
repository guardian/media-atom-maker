import React from 'react';

export default class AddAssetFromURL extends React.Component {
  constructor(props) {
    super(props);
    this.state = { uri: null };
  }

  addAsset = () => {
    if (this.state.uri) {
      this.props.createAsset(this.state, this.props.video);
    }
  };

  onChange = e => {
    this.setState({ uri: e.target.value });
  };

  render() {
    const disabled = !this.state.uri;

    return (
      <div>
        <div className="video__detailbox video__detailbox__assets">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">Asset URL</header>
          </div>
          <div className="form__group">
            <div className="form__row">
              <div>
                <input
                  className="form__field"
                  type="text"
                  placeholder="Paste YouTube URL here"
                  onChange={this.onChange}
                />
                <button
                  className="btn"
                  type="button"
                  onClick={this.addAsset}
                  disabled={disabled}
                >
                  Add
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
