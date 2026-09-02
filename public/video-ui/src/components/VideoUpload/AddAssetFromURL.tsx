import React from 'react';
import { createAsset } from '../../actions/VideoActions/createAsset';
import { Video } from '../../services/VideosApi';

type Props = {
  createAsset: typeof createAsset;
  video: Video;
  isAdding: boolean;
};

type State = {
  uri: any;
};

export default class AddAssetFromURL extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { uri: null };
  }

  addAsset = () => {
    if (this.state.uri) {
      this.props.createAsset(this.state, this.props.video);
    }
  };

  onChange = (e: { target: { value: any } }) => {
    this.setState({ uri: e.target.value });
  };

  render() {
    const disabled = !this.state.uri;

    return (
      <div className="video__detailbox video__detailbox__assets">
        <div className="form__group">
          <header className="video__detailbox__header video__detailbox__header-with-border">
            Asset URL
          </header>
          <div className="form__row">
            <div>
              <p className="form__message form__message--warning">
                This should only be used as a backup if the &apos;Upload to
                YouTube&apos; option is not available.
              </p>
              <p className="form__message form__message--warning">
                Using an Asset URL from an existing YouTube video will not pass
                video data to our video commissioning and syndication tool,
                Iconik.
              </p>
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
                disabled={disabled || this.props.isAdding}
              >
                Add
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
