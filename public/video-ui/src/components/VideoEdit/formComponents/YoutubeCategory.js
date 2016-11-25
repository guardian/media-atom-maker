import React from 'react';
import SelectBox from '../../FormFields/SelectBox';

class YoutubeCategorySelect extends React.Component {

  hasCategories = () => this.props.youtube.categories.length !== 0;

  componentWillMount() {
    if (! this.hasCategories()) {
      this.props.youtubeActions.getCategories();
    }
  }

  updateVideoCategory = (e) => {
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
      defaultOption="Select a category..."
      {...this.props} />
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
