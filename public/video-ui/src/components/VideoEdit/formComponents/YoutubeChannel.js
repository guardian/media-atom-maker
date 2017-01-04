import React from 'react';
import SelectBox from '../../FormFields/SelectBox';

class YoutubeChannelSelect extends React.Component {

  hasChannels = () => this.props.youtube.channels.length !== 0;

  componentWillMount() {
    if (! this.hasChannels()) {
      this.props.youtubeActions.getChannels();
    }
  }

  updateVideoChannel = (e) => {
    const newId = Object.assign({}, this.props.video, {
      channelId: e.target.value
    });

    this.props.updateVideo(newId);
  };

  render () {
    if (! this.hasChannels()) {
      return (
        <select disabled>
          <option>loading...</option>
        </select>
      );
    }

    return (
      <SelectBox
        fieldName="YouTube Channel"
        fieldValue={this.props.video.channelId}
        selectValues={this.props.youtube.channels || []}
        onUpdateField={this.updateVideoChannel}
        defaultOption="Select a channel..."
        video={this.props.video}
        editable={this.props.editable}
        input={this.props.input}
        meta={this.props.meta} />
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
