import React from 'react';

class YoutubeChannelSelect extends React.Component {

  hasChannels = () => this.props.youtube.channels.length !== 0;

  componentWillMount() {
    if (! this.hasChannels()) {
      this.props.youtubeActions.getChannels();
    }
  }

  updateVideoChannel = (e) => {
    const newMetadata = Object.assign({}, this.props.video.data.metadata, {
      channelId: e.target.value
    });

    const newData = Object.assign({}, this.props.video.data, {
      metadata: newMetadata
    });

    this.props.updateVideo(Object.assign({}, this.props.video, {
      data: newData
    }));
  };

  render () {
    if (! this.hasChannels()) {
      return (
        <select disabled>
          <option>loading...</option>
        </select>
      );
    }

    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
      <div className="form__row">
        <label className="form__label">YouTube Channel</label>
        <select {...this.props.input}
                className={"form__field form__field--select " + (hasError ? "form__field--error" : "") }
                value={this.props.video.data.metadata.channelId || ''}
                onChange={this.updateVideoChannel}>
          <option value='' disabled>select a channel...</option>
          {this.props.youtube.channels.map(channel => {
              return (<option value={channel.id} key={channel.id}>{channel.name}</option>);
          })}
        </select>
        {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getChannels from '../../../actions/YoutubeActions/getChannels';

function mapStateToProps(state) {
  return {
    youtube: state.youtube
  };
}

function mapDispatchToProps(dispatch) {
  return {
    youtubeActions: bindActionCreators(Object.assign({}, getChannels), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(YoutubeChannelSelect);
