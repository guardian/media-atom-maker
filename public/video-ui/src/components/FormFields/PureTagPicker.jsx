import React from 'react';
import { PropTypes } from 'prop-types';
import TagSearch from '../TagSearch/TagSearch';
import removeTagDuplicates from '../../util/removeTagDuplicates';

class PureTagPicker extends React.Component {
  static propTypes = {
    tagValue: PropTypes.array.isRequired,
    fetchTags: PropTypes.func.isRequired,
    onUpdate: PropTypes.func.isRequired,
    searchResultTags: PropTypes.array.isRequired,
    tagsToVisible: PropTypes.func.isRequired,
    showTags: PropTypes.bool.isRequired,
    hideTagResults: PropTypes.func.isRequired,
    selectedTagIndex: PropTypes.number,
    inputClearCount: PropTypes.number.isRequired,
    inputPlaceholder: PropTypes.string.isRequired,
    updateSideEffects: PropTypes.func
  };

  state = {
    inputString: ''
  };

  componentDidUpdate(prevProps) {
    if (prevProps.inputClearCount !== this.props.inputClearCount) {
      this.setState({
        inputString: ''
      });
    }
  }

  updateInput = e => {

    const searchText = e.target.value;
    this.props.fetchTags(searchText);
    this.setState({
      inputString: searchText
    });
  };

  selectNewTag = (newFieldValue) => {

      this.setState({
        inputString: ''
      });

      this.props.onUpdate(newFieldValue);
  };

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


        <TagSearch
          searchResultTags={this.props.searchResultTags}
          showTags={this.props.showTags}
          tagsToVisible={this.props.tagsToVisible}
          selectNewTag={this.selectNewTag}
          tagValue={this.props.tagValue}
          removeDupes={removeTagDuplicates}
          selectedTagIndex={this.props.selectedTagIndex}
        />

      </div>
    );
  }
}

export default React.memo(PureTagPicker);
