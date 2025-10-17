import React from 'react';
import { getTagsByType } from '../../services/tagmanager';
import { tagsFromStringList, tagsToStringList } from '../../util/tagParsers';
import { keyCodes } from '../../constants/keyCodes';
import TagTypes from '../../constants/TagTypes';
import getTagDisplayNames from '../../util/getTagDisplayNames';
import TextInputTagPicker from './TextInputTagPicker';
import PureTagPicker from './PureTagPicker';
import TagFieldValue from '../Tags/TagFieldValue';
import TagUnavailable from '../TagSearch/TagUnavailable';
import { DraggableTagList } from './DraggableTagList';
import removeTagDuplicates from '../../util/removeTagDuplicates';
import removeStringTagDuplicates from '../../util/removeStringTagDuplicates';
import ReactTooltip from 'react-tooltip';
import { getYouTubeTagCharCount } from '../../util/getYouTubeTagCharCount';
import YouTubeKeywords from '../../constants/youTubeKeywords';
import debounce from "lodash/debounce";

class TagPicker extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      searchResultTags: [],
      tagValue: [],
      capiUnavailable: false,
      showTags: true,
      tagsVisible: false,
      selectedTagIndex: null,
      inputClearCount: 0
    };
  }

  componentDidUpdate(prevProps) {
    const nextProps = this.props;

    if (prevProps.tagType === TagTypes.youtube) {
      if (prevProps.fieldValue.length !== nextProps.fieldValue.length) {
        tagsFromStringList(nextProps.fieldValue, prevProps.tagType).then(
          result => {
            this.setState({
              tagValue: result
            });
          }
        );
      }
    }
  }

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

  getTagFromTagManager = tag => {
    return {
      id: tag.path,
      webTitle: tag.externalName,
      detailedTitle: tag.internalName
    };
  };

  fetchTags = searchText => {
    const tagTypes = this._getTagTypes();

    if (!searchText) {
      this.setState({
        searchResultTags: []
      });
    } else {
      getTagsByType(this.props.tagManagerUrl, searchText, tagTypes)
        .then(response => {

          const tags = response.data.reduce((tags, {data}) => {
            return tags.concat(this.getTagFromTagManager(data));
          }, []);

          this.setState({
            searchResultTags: tags
          });
        })
        .catch(() => {
          this.setState({
            searchResultTags: [],
            capiUnavailable: true
          });
        });
    }
  };

  debouncedFetchTags = debounce(this.fetchTags, 500);

  onUpdate = newValue => {
    this.setState({
      tagValue: newValue
    });
    return this.props.onUpdateField(tagsToStringList(newValue))
    .then(() => {

      return this.setState({
        searchResultTags: []
      });
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
        showTags: false
      });
    } else {
      this.setState({
        tagsVisible: false
      });
    }

    this.setState({
      inputClearCount: this.state.inputClearCount + 1
    });
  };

  tagsToVisible = () => {
    this.setState({
      tagsVisible: true
    });
  };

  onKeyDown = (e) => {

    this.setState({
      showTags: true
    });

    if (e.keyCode === keyCodes.down) {
      if (this.state.selectedTagIndex === null && this.state.searchResultTags.length > 0) {

        this.setState({
          selectedTagIndex: 0
        });

    } else {
        if (this.state.selectedTagIndex < this.state.searchResultTags.length - 1) {
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
      const newTag = this.state.searchResultTags[this.state.selectedTagIndex];

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
  };

  renderSelectedTags = () => {

    if (this.props.tagType !== TagTypes.keyword) {
      return (
          this.state.tagValue.map((tag, index) => this.renderTag(tag, index))
      );
    }

    return (
      <DraggableTagList
        tags={this.state.tagValue}
        setTags={this.onUpdate}
        removeFn={this.removeFn}
      />
    );
  };

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
  };

  renderTagPicker() {

    if (this.props.tagType === TagTypes.contributor ||
        this.props.tagType === TagTypes.youtube) {
      return (
          <TextInputTagPicker
            tagValue={this.state.tagValue}
            onUpdate={this.onUpdate}
            fetchTags={this.debouncedFetchTags}
            searchResultTags={this.state.searchResultTags}
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
        fetchTags={this.debouncedFetchTags}
        searchResultTags={this.state.searchResultTags}
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
          <TagFieldValue
            tagValue={this.state.tagValue}
            tagType={this.props.tagType}
          />
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

    const hasWarning = this.props.hasWarning(this.props) && this.state.searchResultTags.length === 0;
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
          <TagUnavailable capiUnavailable={this.state.capiUnavailable} />
          <p className="details-list__field ">
            <TagFieldValue tagValue={this.state.tagValue} tagType={this.props.tagType}/>
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

        <TagUnavailable capiUnavailable={this.state.capiUnavailable} />
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

//REDUX CONNECTIONS
import { connect } from 'react-redux';

function mapStateToProps(state) {
  return {
    tagManagerUrl: state.config.tagManagerUrl
  };
}

export default connect(mapStateToProps)(TagPicker);
