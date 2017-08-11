import React from 'react';
import { PropTypes } from 'prop-types';
import TagTypes from '../../constants/TagTypes';
import DragSortableList from 'react-drag-sortable';
import CapiSearch from '../CapiSearch/CapiSearch';
import removeTagDuplicates from '../../util/removeTagDuplicates';

export default class PureTagPicker extends React.Component {

  static propTypes = {
    tagValue: PropTypes.array.isRequired,
    onUpdate: PropTypes.func.isRequired,
    fetchTags: PropTypes.func.isRequired,
    onUpdate: PropTypes.func.isRequired,
    capiTags: PropTypes.array.isRequired,
    tagsToVisible: PropTypes.func.isRequired,
    showTags: PropTypes.bool.isRequired,
    hideTagResults: PropTypes.func.isRequired,
    selectedTagIndex: PropTypes.number,
    inputClearCount: PropTypes.number.isRequired,
    inputPlaceholder: PropTypes.string.isRequired,
    updateSideEffects: PropTypes.func
  }

  state = {
    inputString: '',
  };

  componentWillReceiveProps(nextProps) {
    if (this.props.inputClearCount !== nextProps.inputClearCount) {
      this.setState({
        inputString: '',
      });
    }
  }

  updateInput = e => {

    const searchText = e.target.value;
    this.props.fetchTags(searchText);
    this.setState({
      inputString: searchText
    });
  }

  selectNewTag = (newFieldValue) => {

      this.setState({
        inputString: ''
      });

      this.props.onUpdate(newFieldValue);
  }

  render() {

    return (
      <div>
        <input
          type="text"
          className="form__field"
          onChange={this.updateInput}
          placeholder={this.props.inputPlaceholder}
          value={this.state.inputString}
        />


        <CapiSearch
          capiTags={this.props.capiTags}
          showTags={this.props.showTags}
          tagsToVisible={this.props.tagsToVisible}
          selectNewTag={this.selectNewTag}
          tagValue={this.props.tagValue}
          removeDupes={removeTagDuplicates}
          selectedTagIndex={this.props.selectedTagIndex}
          updateSideEffects={this.props.updateSideEffects}
        />

      </div>
    );
  }
};
