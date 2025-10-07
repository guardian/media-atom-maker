import React from 'react';
import { PropTypes } from 'prop-types';
import { keyCodes } from '../../constants/keyCodes';
import UserActions from '../../constants/UserActions';
import TagTypes from '../../constants/TagTypes';
import TagSearch from '../TagSearch/TagSearch';
import removeStringTagDuplicates from '../../util/removeStringTagDuplicates';

export default class TextInputTagPicker extends React.Component {

  static propTypes = {
    tagValue: PropTypes.array.isRequired,
    onUpdate: PropTypes.func.isRequired,
    fetchTags: PropTypes.func.isRequired,
    removeFn: PropTypes.func.isRequired,
    searchResultTags: PropTypes.array.isRequired,
    tagsToVisible: PropTypes.func.isRequired,
    showTags: PropTypes.bool.isRequired,
    hideTagResults: PropTypes.func.isRequired,
    selectedTagIndex: PropTypes.number,
    inputClearCount: PropTypes.number.isRequired,
    disableCapiTags: PropTypes.bool,
    tagType: PropTypes.string,
    fieldName: PropTypes.string
  };

  state = {
    inputString: '',
    lastAction: UserActions.other
  };

  componentWillReceiveProps(nextProps) {
    if (this.props.inputClearCount !== nextProps.inputClearCount) {
      this.setState({
        inputString: ''
      });
    }
  }

  selectNewTag = (newFieldValue) => {
      this.setState({
        inputString: ''
      });

      this.props.onUpdate(newFieldValue);
  };

  getYoutubeInputValue = () => {
    const existingValueSet = new Set(this.props.tagValue.map(_ => _.id));
    const newValueSet = new Set();

    return this.state.inputString.split(',')
      .filter(candidate => candidate.length !== 0)
      .map(candidate => candidate.trim())
      .reduce((acc, candidate) => {
        const lowerCaseCandidate = candidate.toLowerCase();
        const isNewValue = !existingValueSet.has(lowerCaseCandidate) && !newValueSet.has(lowerCaseCandidate);

        if (isNewValue) {
          acc.push({ id: candidate, webTitle: candidate});
        }

        newValueSet.add(candidate);
        return acc;
      }, []);
  };

  updateInput = e => {
      // If the user did not add new text input, we update the tag search
    if (this.state.lastAction === UserActions.delete) {
      const length = this.props.tagValue.length;
      const lastInput = this.props.tagValue[length - 1];

      this.setState({
        inputString: lastInput,
        lastAction: UserActions.other
      });

      const newValue = this.props.tagValue.slice(
        0,
        this.props.tagValue.length - 1
      );
      this.props.onUpdate(newValue);
    } else {
      this.setState({
        inputString: e.target.value
      });

      if (!this.props.disableCapiTags) {
        const searchText = e.target.value;

        this.props.fetchTags(searchText);

      }
    }
  };

  processTagInput = e => {
    if (e.keyCode === keyCodes.enter) {

      if (this.props.selectedTagIndex === null) {
        const onlyWhitespace = !/\S/.test(this.state.inputString);
        if (!onlyWhitespace) {

          const newInput = this.props.tagType === TagTypes.youtube ? this.getYoutubeInputValue() : [this.state.inputString.trim()];

          const newFieldValue = this.props.tagValue.concat(newInput);

          this.props.onUpdate(newFieldValue)
          .then(() => {
            this.setState({
              inputString: ''
            });
          });
        }
      }

    } else if (e.keyCode === keyCodes.backspace) {
      if (this.state.inputString.length === 0) {
        const lastInput = this.props.tagValue[this.props.tagValue.length - 1];

        if (typeof lastInput === 'string') {
          //User is trying to delete a string input
          this.setState(
            {
              lastAction: UserActions.delete
            },
            () => {
              this.updateInput();
            }
          );
        }
      }
    } else {
      this.setState({
        lastAction: UserActions.other
      });
    }
  };

  renderValue = (field, i, isLastInput = false) => {

    if (field.id) {
      return (
        <span
          className="form__field--multiselect__value form__field__tag__remove"
          key={`${field.id}-${i}`}
          onClick={tag => this.props.removeFn(field)}
        >
          {field.webTitle}{' '}
        </span>
      );
    }
    return (
      <span
        className={ !isLastInput ? "form__field--multistring__value" : "form__field--multistring__last"}
        key={`${field.id}-${i}`}
      >
        {' '}{field}{' '}
      </span>
    );
  };

  renderTextInputElement(lastElement) {

    return (
      <span className="form__field__tag--container">
        {lastElement && this.renderValue(lastElement, 0, true)}
        <input
          type="text"
          className="form__field__tag--input"
          id={this.props.fieldName}
          onKeyDown={this.processTagInput}
          onChange={this.updateInput}
          value={this.state.inputString}
        />
      </span>
    );
  }

  renderInputElements() {

    const valueLength = this.props.tagValue.length;
    const lastElement = !valueLength || valueLength === 0
      ? null
      : this.props.tagValue[valueLength - 1];

    return (
      <div className={'form__field__tag--selector ' +
        (this.props.hasError(this.props) ? 'form__field--error' : '')}>
        {valueLength
          ? this.props.tagValue.map((value, i) => {
              if (i < valueLength - 1) {
                return this.renderValue(value, i);
              }
            })
          : ''}

        {this.renderTextInputElement(lastElement)}

      </div>
    );
  }

  render() {
    return (
      <div>

        {this.renderInputElements()}

        <TagSearch
          searchResultTags={this.props.searchResultTags}
          showTags={this.props.showTags}
          tagsToVisible={this.props.tagsToVisible}
          selectNewTag={this.selectNewTag}
          tagValue={this.props.tagValue}
          removeDupes={removeStringTagDuplicates}
          selectedTagIndex={this.props.selectedTagIndex}
        />

      </div>
    );
  }
};
