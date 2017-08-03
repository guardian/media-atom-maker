import React from 'react';
import TagTypes from '../../constants/TagTypes';
import DragSortableList from 'react-drag-sortable';
import CapiSearch from '../CapiSearch/Capisearch';
import removeTagDuplicates from '../../util/removeTagDuplicates';

export default class PureTagPicker extends React.Component {

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

    const getInputPlaceholder = () => {
      if (!this.props.fieldValue || this.props.fieldValue.length === 0) {
        return this.props.inputPlaceholder;
      }
      return '';
    };

    return (
      <div>
        <input
          type="text"
          className="form__field"
          onChange={this.updateInput}
          placeholder={getInputPlaceholder()}
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
        />

      </div>
    );
  }
};
