import React from 'react';
import ContentApi from '../../services/capi';
import { tagsFromStringList, tagsToStringList } from '../../util/tagParsers';
import { keyCodes } from '../../constants/keyCodes';
import UserActions from '../../constants/UserActions';
import TagTypes from '../../constants/TagTypes';
import getTagDisplayNames from '../../util/getTagDisplayNames';
import TextInputTagPicker from './TextInputTagPicker';
import PureTagPicker from './PureTagPicker';
import TagFieldValue from '../Tags/TagFieldValue';
import CapiUnavailable from '../CapiSearch/CapiUnavailable';
import DragSortableList from 'react-drag-sortable';
import removeTagDuplicates from '../../util/removeTagDuplicates';
import removeStringTagDuplicates from '../../util/removeStringTagDuplicates';
import ReactTooltip from 'react-tooltip';
import { getYouTubeTagCharCount } from '../../util/getYouTubeTagCharCount';
import YouTubeKeywords from '../../constants/youTubeKeywords';

export default class TagPicker extends React.Component {

  state = {
    capiTags: [],
    tagValue: [],
    capiUnavailable: false,
    showTags: true,
    tagsVisible: false,
    selectedTagIndex: null,
    inputClearCount: 0
  };

  componentDidMount() {
    ReactTooltip.rebuild();
    if (this.props.fieldValue !== this.props.placeholder) {
      tagsFromStringList(this.props.fieldValue, this.props.tagType)
        .then(result => {
          this.setState({
            tagValue: getTagDisplayNames(result)
          });
        })
        .catch(() => {
          // capi is unavailable and we cannot get webtitles for tags
          this.setState({
            tagValue: this.props.fieldValue.slice(),
            capiUnavailable: true
          });
        });
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.tagType === TagTypes.youtube) {
      if (this.props.fieldValue.length !== nextProps.fieldValue.length) {
        tagsFromStringList(nextProps.fieldValue, this.props.tagType)
        .then(result => {
          this.setState({
            tagValue: result
          });
        })
      }
    }
  }

  _getTagTypes() {
    const defaultTagTypes = [TagTypes.tone, TagTypes.series, TagTypes.keyword];

    switch (this.props.tagType) {
      case TagTypes.keyword:
        return defaultTagTypes;
      case TagTypes.commercial:
        return [TagTypes.commercial, ...defaultTagTypes];
      default:
        return [this.props.tagType];
    }
  }

  fetchTags = searchText => {
    const tagTypes = this._getTagTypes();

    ContentApi.getTagsByType(searchText, tagTypes)
      .then(capiResponses => {
        const tags = capiResponses.reduce((tags, capiResponse) => {
          return tags.concat(getTagDisplayNames(capiResponse.response.results));
        }, []);

        this.setState({
          capiTags: tags
        });
      })
      .catch(() => {
        this.setState({
          capiTags: [],
          capiUnavailable: true
        });
      });
  }

  onUpdate = newValue => {
    this.setState({
      tagValue: newValue
    });
    this.props.onUpdateField(tagsToStringList(newValue));

    this.setState({
      capiTags: []
    });
  };

  removeFn = (tag) => {
    const newFieldValue = this.state.tagValue.filter(oldField => {
      return tag.id !== oldField.id;
    });

    this.onUpdate(newFieldValue);
  };


  hideTagResults = (e) => {

    //First we need to make sure to set the selectedTagIndex back to null

    if (this.state.selectedTagIndex !== null) {
      this.setState({
        selectedTagIndex: null
      });
    }

    // For each tag picker component, there is a tagsVisible state variable.
    // The onBlur event attached to the tag picker gets fired when
    // any of its children are clicked. This variable is used to check if the event
    // was fired by clicking on one of the child elements and makes sure that this
    // does not hide the tag search results.

    const tagsVisible = this.state.tagsVisible;

    if (!tagsVisible) {
      this.setState({
        showTags: false,
      });
    } else {
      this.setState({
        tagsVisible: false
      });
    }

    this.setState({
      inputClearCount: this.state.inputClearCount + 1
    });
  }

  tagsToVisible = () => {
    this.setState({
      tagsVisible: true
    });
  }

  onKeyDown = (e) => {

    this.setState({
      showTags: true
    });

    if (e.keyCode === keyCodes.down) {
      if (this.state.selectedTagIndex === null && this.state.capiTags.length > 0) {

        this.setState({
          selectedTagIndex: 0
        });

    } else {
        if (this.state.selectedTagIndex < this.state.capiTags.length - 1) {
          this.setState({
            selectedTagIndex: this.state.selectedTagIndex + 1
          });
        }
      }
    }

    if (e.keyCode === keyCodes.up) {
      if (this.state.selectedTagIndex && this.state.selectedTagIndex !== 0) {
        this.setState({
          selectedTagIndex: this.state.selectedTagIndex - 1
          });
      }
    }

    if (e.keyCode === keyCodes.enter && this.state.selectedTagIndex !== null) {
      const newTag = this.state.capiTags[this.state.selectedTagIndex];

      const valueWithoutDupes = this.props.tagType === TagTypes.contributor ?
        removeStringTagDuplicates(newTag, this.state.tagValue) :
        removeTagDuplicates(newTag, this.state.tagValue);

      const newFieldValue = valueWithoutDupes.concat([newTag]);

      this.setState({
        selectedTagIndex: null,
        inputClearCount: this.state.inputClearCount + 1
      });

      this.onUpdate(newFieldValue);

    }
  }

  onSort = (sortedList) => {
    const newTagValues = sortedList.reduce((sortedTagList, sortedValue) => {

      //For each component in the list of dragged elements,
      //we have to extract the name of the tag it represents.
      const tagTitle = sortedValue.content.props.children[0].props.children;

      const tagValue = this.state.tagValue.find(value => value.detailedTitle === tagTitle);

      return [...sortedTagList, tagValue];
    }, []);

    this.onUpdate(newTagValues);
  }

  renderSelectedTags = () => {

    if (this.props.tagType !== TagTypes.keyword) {
      return (
          this.state.tagValue.map((tag, index) => this.renderTag(tag, index))
      );
    }

    const tagItems = this.state.tagValue.map((value, index) => {
      return {
        content: this.renderTag(value, index)
      }
    });

    return (
        <DragSortableList
          items={tagItems}
          moveTransitionDuration={0.3}
          dropBackTransitionDuration={0.3}
          type="vertical"
          onSort={this.onSort}
          placeholder={<span></span>}
        />
    );
  }

  renderTag = (tag, index) => {
    return (
      <div
        key={`${tag.id}-${index}`}
        className="form__field__selected__tag"
      >
        <span>
          {tag.detailedTitle}
        </span>
        <span
          className="form__field__tag__remove"
          onClick={() => this.removeFn(tag)}>
        </span>
      </div>
    );
  }

  renderTagPicker() {

    if (this.props.tagType === TagTypes.contributor ||
        this.props.tagType === TagTypes.youtube) {
      return (
          <TextInputTagPicker
            tagValue={this.state.tagValue}
            onUpdate={this.onUpdate}
            fetchTags={this.fetchTags}
            capiTags={this.state.capiTags}
            tagsToVisible={this.tagsToVisible}
            showTags={this.state.showTags}
            hideTagResults={this.hideTagResults}
            removeFn={this.removeFn}
            selectedTagIndex={this.state.selectedTagIndex}
            inputClearCount={this.state.inputClearCount}

            {...this.props}
          />

      );

    }

    return (
      <PureTagPicker
        tagValue={this.state.tagValue}
        onUpdate={this.onUpdate}
        fetchTags={this.fetchTags}
        capiTags={this.state.capiTags}
        tagsToVisible={this.tagsToVisible}
        showTags={this.state.showTags}
        hideTagResults={this.hideTagResults}
        selectedTagIndex={this.state.selectedTagIndex}
        inputClearCount={this.state.inputClearCount}

        {...this.props}
      />
    );
  }

  renderAddedTags() {

    if (this.state.tagValue.length !== 0) {
      if (this.props.tagType === TagTypes.contributor ||
          this.props.tagType === TagTypes.youtube) {
        return (
          <TagFieldValue tagValue={this.state.tagValue}/>
        );

      }
      return (
        <div className="form__field__tag__list">
          {this.renderSelectedTags()}
        </div>
      );
    }
    return null;

  }

  renderBylineInstructions() {
    if (this.props.tagType === TagTypes.contributor) {
      return (
        <span className="form__field__instructions">
          Press enter to add byline as text
        </span>
      );
    }
  }

  renderCharCount() {
    if (this.props.tagType === TagTypes.youtube) {
      const charCount = getYouTubeTagCharCount(this.props.fieldValue);
      return (
        <span>
          Character count: {charCount} / {YouTubeKeywords.maxCharacters}
        </span>
      );
    }
  }

  renderCopyButton() {
    if (this.props.updateSideEffects) {
      return (
        <button
          type="button"
          className="btn form__label__button"
          onClick={this.props.updateSideEffects}
          data-tip="Copy composer keywords to youtube keywords"
          data-place="top"
        >
          <i className="icon">edit</i>
        </button>
      );
    }
  }

  render() {

    const hasWarning = this.props.hasWarning(this.props) && this.state.capiTags.length === 0;
    const hasError = this.props.hasError(this.props);

    if (!this.props.editable) {
      if (!this.state.tagValue || this.state.tagValue.length === 0) {
        return (
          <div>
            <p className="details-list__title">{this.props.fieldName}</p>
            <p className={'details-list__field details-list__empty'}>
              {this.props.placeholder}
            </p>
          </div>
        );
      }
      return (
        <div>
          <p className="details-list__title">{this.props.fieldName}</p>
          <CapiUnavailable capiUnavailable={this.state.capiUnavailable} />
          <p className="details-list__field ">
            <TagFieldValue tagValue={this.state.tagValue}/>
          </p>
        </div>
      );
    }

    return (
      <div className="form__row"
        onBlur={this.hideTagResults}
        onKeyDown={this.onKeyDown}
      >

        <div className="form__label__layout">
          <label className="form__label">{this.props.fieldName}</label>
          {this.renderBylineInstructions()}
          {this.renderCopyButton()}
          {this.renderCharCount()}
        </div>

        <CapiUnavailable capiUnavailable={this.state.capiUnavailable} />
        {this.renderTagPicker()}
        {this.renderAddedTags()}
        {hasWarning
          ? <p className="form__message form__message--warning">
          {this.props.notification.message}
          </p>
            : ''}
        {hasError
          ? <p className="form__message form__message--error">
          {this.props.notification.message}
          </p>
            : ''}
      </div>
    );
  }
}
