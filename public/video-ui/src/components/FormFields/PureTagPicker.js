import React from 'react';
import TagTypes from '../../constants/TagTypes';
import DragSortableList from 'react-drag-sortable';
import CapiSearch from '../CapiSearch/Capisearch';
import removeTagDuplicates from '../../util/removeTagDuplicates';

export default class PureTagPicker extends React.Component {

  state = {
    inputString: ''
  };

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
      <div className="form__row"
        onBlur={this.props.hideTagResults}
        onMouseDown={this.props.showTagResults}
      >

        <div className="form__label__layout">
          <label className="form__label">{this.props.fieldName}</label>
        </div>

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
        />

      </div>
    );
  }
};
