import React from 'react';
import SelectBox from '../../FormFields/SelectBox';

class YoutubeCategorySelect extends React.Component {

  hasCategories = () => this.props.youtube.categories.length !== 0;

  defaultOption = "Select a youtube category";

  componentWillMount() {
    if (! this.hasCategories()) {
      this.props.youtubeActions.getCategories();
    }
  }

  updateVideoCategory = (e) => {

    if (e.target.value === this.defaultOption) {
      return;
    }
    const newId = Object.assign({}, this.props.video, {
      youtubeCategoryId: e.target.value}
    );

    this.props.updateVideo(newId);
  };

  render () {
    if (! this.hasCategories()) {
      return (
        <select disabled>
          <option>loading...</option>
        </select>
      );
    }

    return (
    <SelectBox
      fieldName="YouTube Category"
      fieldValue={this.props.video.youtubeCategoryId}
      selectValues={this.props.youtube.categories || []}
      onUpdateField={this.updateVideoCategory}
      defaultOption={this.defaultOption}
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
import * as getCategories from '../../../actions/YoutubeActions/getCategories';

function mapStateToProps(state) {
  return {
    youtube: state.youtube
  };
}

function mapDispatchToProps(dispatch) {
  return {
    youtubeActions: bindActionCreators(Object.assign({}, getCategories), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(YoutubeCategorySelect);
