import React from 'react';
import { getTagsByType, Section } from '../../services/tagmanager';
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
import { removeStringTagDuplicates } from '../../util/removeStringTagDuplicates';
import ReactTooltip from 'react-tooltip';
import { getYouTubeTagCharCount } from '../../util/getYouTubeTagCharCount';
import YouTubeKeywords from '../../constants/youTubeKeywords';
import debounce from 'lodash/debounce';

type State = {
  searchResultTags: any[];
  tagValue: any[] | ParsedTag[];
  capiUnavailable: boolean;
  showTags: boolean;
  tagsVisible: boolean;
  selectedTagIndex: null | number;
  inputClearCount: number;
};

class TagPicker extends React.Component<object, State> {
  constructor(props: object) {
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

  componentDidUpdate(prevProps: object) {
    const nextProps = this.props;

    if ((prevProps as any).tagType === TagTypes.youtube) {
      if (
        (prevProps as any).fieldValue.length !==
        (nextProps as any).fieldValue.length
      ) {
        tagsFromStringList(
          (nextProps as any).fieldValue,
          (prevProps as any).tagType
        ).then(result => {
          this.setState({
            tagValue: result
          });
        });
      }
    }
  }

  componentDidMount() {
    ReactTooltip.rebuild();
    if ((this.props as any).fieldValue !== (this.props as any).placeholder) {
      tagsFromStringList(
        (this.props as any).fieldValue,
        (this.props as any).tagType
      )
        .then(result => {
          this.setState({
            tagValue: getTagDisplayNames(result)
          });
        })
        .catch(() => {
          // capi is unavailable and we cannot get webtitles for tags
          this.setState({
            tagValue: (this.props as any).fieldValue.slice(),
            capiUnavailable: true
          });
        });
    }
  }

  _getTagTypes() {
    const defaultTagTypes = [TagTypes.tone, TagTypes.series, TagTypes.keyword];

    switch ((this.props as any).tagType) {
      case TagTypes.keyword:
        return defaultTagTypes;
      case TagTypes.commercial:
        return [TagTypes.commercial, ...defaultTagTypes];
      default:
        return [(this.props as any).tagType];
    }
  }

  getTagFromTagManager = (tag: {
    id?: number;
    path: any;
    type?: string;
    internalName: any;
    externalName: any;
    deprecated?: boolean;
    section?: Section;
    subType?: string;
  }) => {
    return {
      id: tag.path,
      webTitle: tag.externalName,
      detailedTitle: tag.internalName
    };
  };

  fetchTags = (searchText: string) => {
    const tagTypes = this._getTagTypes();

    if (!searchText) {
      this.setState({
        searchResultTags: []
      });
    } else {
      getTagsByType(
        (this.props as any).tagManagerUrl,
        searchText,
        tagTypes,
        (this.props as any).tagSubType
      )
        .then(response => {
          const tags = response.data.reduce((tags, { data }) => {
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

  onUpdate = (newValue: any) => {
    this.setState({
      tagValue: newValue
    });
    return (this.props as any)
      .onUpdateField(tagsToStringList(newValue))
      .then(() => {
        return this.setState({
          searchResultTags: []
        });
      });
  };

  removeFn = (tag: { id: any }) => {
    const newFieldValue = this.state.tagValue.filter(oldField => {
      return tag.id !== oldField.id;
    });

    this.onUpdate(newFieldValue);
  };

  hideTagResults = (e: any) => {
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

  onKeyDown = (e: { keyCode: number }) => {
    this.setState({
      showTags: true
    });

    if (e.keyCode === keyCodes.down) {
      if (
        this.state.selectedTagIndex === null &&
        this.state.searchResultTags.length > 0
      ) {
        this.setState({
          selectedTagIndex: 0
        });
      } else {
        if (
          this.state.selectedTagIndex <
          this.state.searchResultTags.length - 1
        ) {
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

      const valueWithoutDupes =
        (this.props as any).tagType === TagTypes.contributor
          ? removeStringTagDuplicates(newTag, this.state.tagValue)
          : removeTagDuplicates(newTag, this.state.tagValue);

      const newFieldValue = valueWithoutDupes.concat([newTag]);

      this.setState({
        selectedTagIndex: null,
        inputClearCount: this.state.inputClearCount + 1
      });

      this.onUpdate(newFieldValue);
    }
  };

  renderSelectedTags = () => {
    if ((this.props as any).tagType !== TagTypes.keyword) {
      return this.state.tagValue.map((tag, index) =>
        this.renderTag(tag, index)
      );
    }

    return (
      <DraggableTagList
        // @ts-expect-error TS(2322): Type 'any[] | ParsedTag[]' is not assignable to ty... Remove this comment to see the full error message
        tags={this.state.tagValue}
        setTags={this.onUpdate}
        removeFn={this.removeFn}
      />
    );
  };

  renderTag = (
    tag: {
      id: any;
      detailedTitle:
        | string
        | number
        | boolean
        | React.ReactElement<any, string | React.JSXElementConstructor<any>>
        | Iterable<React.ReactNode>
        | React.ReactPortal;
    },
    index: number
  ) => {
    return (
      <div key={`${tag.id}-${index}`} className="form__field__selected__tag">
        <span>{tag.detailedTitle}</span>
        <span
          className="form__field__tag__remove"
          onClick={() => this.removeFn(tag)}
        ></span>
      </div>
    );
  };

  renderTagPicker() {
    if (
      (this.props as any).tagType === TagTypes.contributor ||
      (this.props as any).tagType === TagTypes.youtube
    ) {
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
      // @ts-expect-error TS(2741): Property 'inputPlaceholder' is missing in type '{ ... Remove this comment to see the full error message
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
      if (
        (this.props as any).tagType === TagTypes.contributor ||
        (this.props as any).tagType === TagTypes.youtube
      ) {
        return (
          <TagFieldValue
            tagValue={this.state.tagValue}
            tagType={(this.props as any).tagType}
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
    if ((this.props as any).tagType === TagTypes.contributor) {
      return (
        <span className="form__field__instructions">
          Press enter to add byline as text
        </span>
      );
    }
  }

  renderCharCount() {
    if ((this.props as any).tagType === TagTypes.youtube) {
      const charCount = getYouTubeTagCharCount((this.props as any).fieldValue);
      return (
        <span>
          Character count: {charCount} / {YouTubeKeywords.maxCharacters}
        </span>
      );
    }
  }

  renderCopyButton() {
    if ((this.props as any).updateSideEffects) {
      return (
        <button
          type="button"
          className="btn form__label__button"
          onClick={(this.props as any).updateSideEffects}
          data-tip="Copy composer keywords to youtube keywords"
          data-place="top"
        >
          <i className="icon">edit</i>
        </button>
      );
    }
  }

  render() {
    const hasWarning =
      (this.props as any).hasWarning(this.props) &&
      this.state.searchResultTags.length === 0;
    const hasError = (this.props as any).hasError(this.props);

    if (!(this.props as any).editable) {
      if (!this.state.tagValue || this.state.tagValue.length === 0) {
        return (
          <div>
            <p className="details-list__title">
              {(this.props as any).fieldName}
            </p>
            <p className={'details-list__field details-list__empty'}>
              {(this.props as any).placeholder}
            </p>
          </div>
        );
      }
      return (
        <div>
          <p className="details-list__title">{(this.props as any).fieldName}</p>
          <TagUnavailable capiUnavailable={this.state.capiUnavailable} />
          <p className="details-list__field ">
            <TagFieldValue
              tagValue={this.state.tagValue}
              tagType={(this.props as any).tagType}
            />
          </p>
        </div>
      );
    }

    return (
      <div
        className="form__row"
        onBlur={this.hideTagResults}
        onKeyDown={this.onKeyDown}
      >
        <div className="form__label__layout">
          <label className="form__label">{(this.props as any).fieldName}</label>
          {this.renderBylineInstructions()}
          {this.renderCopyButton()}
          {this.renderCharCount()}
        </div>

        <TagUnavailable capiUnavailable={this.state.capiUnavailable} />
        {this.renderTagPicker()}
        {this.renderAddedTags()}
        {hasWarning ? (
          <p className="form__message form__message--warning">
            {(this.props as any).notification.message}
          </p>
        ) : (
          ''
        )}
        {hasError ? (
          <p className="form__message form__message--error">
            {(this.props as any).notification.message}
          </p>
        ) : (
          ''
        )}
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { ParsedTag } from '../../types/tags';

function mapStateToProps(state: { config: { tagManagerUrl: any } }) {
  return {
    tagManagerUrl: state.config.tagManagerUrl
  };
}

export default connect(mapStateToProps)(TagPicker);
