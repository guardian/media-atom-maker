import React from 'react';

class YoutubeCategorySelect extends React.Component {

  hasCategories () {
    return this.props.youtube.categories.length !== 0;
  }

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
      <div className="form__row">
        <label className="form__label">YouTube Category</label>
        <select {...this.props.input}
                className={"form__field form__field--select " + (hasError ? "form__field--error" : "") }
                value={this.props.video.data.metadata.category || ''}
                onChange={this.updateVideoCategory}>
          <option value='' disabled>select a category...</option>
          {this.props.youtube.categories.map(category => {
              return (<option value={category.id} key={category.id}>{category.title}</option>);
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
