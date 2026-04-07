import React from 'react';
import { getTagsByType } from '../../services/tagmanager';
import { tagsFromStringList, tagsToStringList } from '../../util/tagParsers';
import TagTypes from '../../constants/TagTypes';
import getTagDisplayNames from '../../util/getTagDisplayNames';
import TagFieldValue from '../Tags/TagFieldValue';
import TagUnavailable from '../TagSearch/TagUnavailable';
import { DraggableTagList } from './DraggableTagList';
import ReactTooltip from 'react-tooltip';
import { getYouTubeTagCharCount } from '../../util/getYouTubeTagCharCount';
import YouTubeKeywords from '../../constants/youTubeKeywords';
import debounce from "lodash/debounce";
import Typeahead from '../Typeahead/Typeahead';

class TagPicker extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      searchResultTags: [],
      tagValue: [],
      capiUnavailable: false
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

  tagToTypeaheadOption = (tag) => {
    if (typeof tag === 'string') {
      return { id: tag, label: tag };
    }
    return {
      id: tag.id,
      label: tag.detailedTitle || tag.webTitle,
      detail: tag.detailedTitle,
    };
  };

  // Converts YouTube comma-separated free-text into individual keyword objects,
  // deduplicating against the current selection.
  getYouTubeTagsFromFreeText = (text) => {
    const existingIds = new Set(
      this.state.tagValue.map(t => (typeof t === 'string' ? t : t.id))
    );
    return text
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0 && !existingIds.has(s))
      .map(s => ({ id: s, webTitle: s }));
  };

  handleTypeaheadSelection = (selectedOptions) => {
    const newTags = selectedOptions.flatMap(opt => {
      // Prefer the full tag object from the most recent search results.
      const fromSearch = this.state.searchResultTags.find(t => t.id === opt.id);
      if (fromSearch) return [fromSearch];

      // Preserve already-selected values in their original form.
      const existing = this.state.tagValue.find(t =>
        typeof t === 'string' ? t === opt.id : t.id === opt.id
      );
      if (existing) return [existing];

      // Free-text entry — shape depends on tag type.
      if (this.props.tagType === TagTypes.youtube) {
        // Support comma-separated YouTube keywords typed into the input.
        if (opt.label.includes(',')) {
          return this.getYouTubeTagsFromFreeText(opt.label);
        }
        return [{ id: opt.id, webTitle: opt.label }];
      }

      // Contributor: plain string byline.
      return [opt.label];
    });

    this.onUpdate(newTags);
  };

  renderTagPicker() {
    const typeaheadOptions = this.state.searchResultTags.map(tag => ({
      id: tag.id,
      label: tag.detailedTitle || tag.webTitle,
      detail: tag.detailedTitle,
    }));

    const typeaheadSelectedItems = this.state.tagValue.map(
      this.tagToTypeaheadOption
    );

    const allowFreeText =
      this.props.tagType === TagTypes.contributor ||
      this.props.tagType === TagTypes.youtube;

    // For contributor/youtube the selected items are shown via showSelectedItems
    // so the user retains the ability to remove them. For all other tag types
    // renderAddedTags() handles the display with its own removable chip list.
    const showSelectedItems = allowFreeText;

    return (
      <Typeahead
        options={typeaheadOptions}
        selectedItems={typeaheadSelectedItems}
        onSelectionChange={this.handleTypeaheadSelection}
        onInputChange={
          this.props.disableCapiTags ? undefined : this.debouncedFetchTags
        }
        placeholder={this.props.inputPlaceholder}
        allowFreeText={allowFreeText}
        showSelectedItems={showSelectedItems}
        inputId={this.props.fieldName}
      />
    );
  }

  renderAddedTags() {

    if (this.state.tagValue.length !== 0) {
      // For contributor and youtube, selected items are rendered as chips by
      // the Typeahead component (showSelectedItems=true), so there is nothing
      // extra to render here.
      if (
        this.props.tagType === TagTypes.contributor ||
        this.props.tagType === TagTypes.youtube
      ) {
        return null;
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
      <div className="form__row">

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
