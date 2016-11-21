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
    const newMetadata = Object.assign({}, this.props.video.data.metadata, {
      category: e.target.value}
    );

    const newData = Object.assign({}, this.props.video.data, {
      metadata: newMetadata
    });

    this.props.updateVideo(Object.assign({}, this.props.video, {
      data: newData
    }));
  };

  render () {
    if (! this.hasCategories()) {
      return (
        <select disabled>
          <option>loading...</option>
        </select>
      );
    }

    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
    <SelectBox
      fieldName="YouTube Category"
      fieldValue={this.props.video.data.category}
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
