import React from 'react';
import { getTagsByType, Section } from '../../services/tagmanager';
import { tagsFromStringList, tagsToStringList } from '../../util/tagParsers';
import { keyCodes } from '../../constants/keyCodes';
import TagTypes from '../../constants/TagTypes';
import { getTagDisplayNames } from '../../util/getTagDisplayNames';
import TextInputTagPicker from './TextInputTagPicker';
import PureTagPicker from './PureTagPicker';
import TagFieldValue from '../Tags/TagFieldValue';
import { DraggableTagList } from './DraggableTagList';
import { removeTagDuplicates } from '../../util/removeTagDuplicates';
import { removeStringTagDuplicates } from '../../util/removeStringTagDuplicates';
import ReactTooltip from 'react-tooltip';
import { getYouTubeTagCharCount } from '../../util/getYouTubeTagCharCount';
import YouTubeKeywords from '../../constants/youTubeKeywords';
import debounce from 'lodash/debounce';

type State = {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  searchResultTags: any[] | never[];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  tagValue: any[] | ParsedTag[] | any[];
  capiError: string | null;
  showTags: boolean;
  tagsVisible: boolean;
  selectedTagIndex: number;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  inputClearCount: any;
};

class TagPicker extends React.Component<
  object & { disableTextInput?: boolean; disableCapiTags?: boolean } & {
    placeholder?: string;
    tagSubType?: string;
    editable?: boolean;
    fieldName?: string;
  },
  State
> {
  constructor(props: object) {
    super(props);
    this.state = {
      searchResultTags: [],
      tagValue: [],
      capiError: null,
      showTags: true,
      tagsVisible: false,
      // @ts-expect-error TS(2322): Type 'null' is not assignable to type 'number'.
      selectedTagIndex: null,
      inputClearCount: 0
    };
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  formatMissingTagError = (missingTagIds: any) => {
    if (!missingTagIds || missingTagIds.length === 0) {
      return null;
    }

    return `Tag not found: ${missingTagIds.join(', ')}`;
  };

  revalidateSavedTags = (savedTagIds: string[]) => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return tagsFromStringList(savedTagIds, (this.props as any).tagType)
      .then(result => {
        this.setState({
          capiError: this.formatMissingTagError(result.missingTagIds)
        });
      })
      .catch(() => {
        this.setState({
          capiError: 'Tags are currently unavailable'
        });
      });
  };

  componentDidUpdate(prevProps: object) {
    const nextProps = this.props;

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    if ((prevProps as any).tagType === TagTypes.youtube) {
      if (
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (prevProps as any).fieldValue.length !==
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (nextProps as any).fieldValue.length
      ) {
        tagsFromStringList(
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (nextProps as any).fieldValue,
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (prevProps as any).tagType
        ).then(result => {
          this.setState({
            tagValue: result.tags
          });
        });
      }
    }
  }

  componentDidMount() {
    ReactTooltip.rebuild();
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    if ((this.props as any).fieldValue !== this.props.placeholder) {
      tagsFromStringList(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (this.props as any).fieldValue,
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (this.props as any).tagType
      )
        .then(result => {
          if (result.missingTagIds.length > 0) {
            this.setState({
              capiError: this.formatMissingTagError(result.missingTagIds)
            });
          }
          this.setState({
            // @ts-expect-error TS(2345): Argument of type 'ParsedTag[]' is not assignable t... Remove this comment to see the full error message
            tagValue: getTagDisplayNames(result.tags)
          });
        })
        .catch(() => {
          // capi is unavailable and we cannot get webtitles for tags
          this.setState({
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            tagValue: (this.props as any).fieldValue.slice(),
            capiError: 'Tags are currently unavailable'
          });
        });
    }
  }

  _getTagTypes() {
    const defaultTagTypes = [TagTypes.tone, TagTypes.series, TagTypes.keyword];

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    switch ((this.props as any).tagType) {
      case TagTypes.keyword:
        return defaultTagTypes;
      case TagTypes.commercial:
        return [TagTypes.commercial, ...defaultTagTypes];
      default:
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        return [(this.props as any).tagType];
    }
  }

  getTagFromTagManager = (tag: {
    id?: number;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    path: any;
    type?: string;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    internalName: any;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    externalName: any;
    deprecated?: boolean;
    section?: Section;
    subType?: string | undefined;
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
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (this.props as any).tagManagerUrl,
        searchText,
        tagTypes,
        this.props.tagSubType
      )
        .then(response => {
          const tags = response.data.reduce((tags, { data }) => {
            // @ts-expect-error TS(2769): No overload matches this call.
            return tags.concat(this.getTagFromTagManager(data));
          }, []);
          this.setState({
            searchResultTags: tags
          });
        })
        .catch(() => {
          this.setState({
            searchResultTags: [],
            capiError: 'Tags are currently unavailable'
          });
        });
    }
  };

  debouncedFetchTags = debounce(this.fetchTags, 500);

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onUpdate = (newValue: any) => {
    const savedTagsList = tagsToStringList(newValue);

    this.setState({
      tagValue: newValue
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return (this.props as any).onUpdateField(savedTagsList).then(() => {
      this.setState({
        searchResultTags: []
      });
      return this.revalidateSavedTags(savedTagsList);
    });
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  removeFn = (tag: { id: any }) => {
    const newFieldValue = this.state.tagValue.filter(oldField => {
      return tag.id !== oldField.id;
    });

    this.onUpdate(newFieldValue);
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  hideTagResults = (e: any) => {
    //First we need to make sure to set the selectedTagIndex back to null

    if (this.state.selectedTagIndex !== null) {
      this.setState({
        // @ts-expect-error TS(2322): Type 'null' is not assignable to type 'number'.
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
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (this.props as any).tagType === TagTypes.contributor
          ? removeStringTagDuplicates(newTag, this.state.tagValue)
          : removeTagDuplicates(newTag, this.state.tagValue);

      const newFieldValue = valueWithoutDupes.concat([newTag]);

      this.setState({
        // @ts-expect-error TS(2322): Type 'null' is not assignable to type 'number'.
        selectedTagIndex: null,
        inputClearCount: this.state.inputClearCount + 1
      });

      this.onUpdate(newFieldValue);
    }
  };

  renderSelectedTags = () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
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
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      id: any;
      detailedTitle:
        | string
        | number
        | boolean
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        | React.ReactElement<any, string | React.JSXElementConstructor<any>>
        | Iterable<React.ReactNode>
        | React.ReactPortal
        | null
        | undefined;
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
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (this.props as any).tagType === TagTypes.contributor ||
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
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
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (this.props as any).tagType === TagTypes.contributor ||
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (this.props as any).tagType === TagTypes.youtube
      ) {
        return (
          <TagFieldValue
            tagValue={this.state.tagValue}
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
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
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    if ((this.props as any).tagType === TagTypes.contributor) {
      return (
        <span className="form__field__instructions">
          Press enter to add byline as text
        </span>
      );
    }
  }

  renderCharCount() {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    if ((this.props as any).tagType === TagTypes.youtube) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const charCount = getYouTubeTagCharCount((this.props as any).fieldValue);
      return (
        <span>
          Character count: {charCount} / {YouTubeKeywords.maxCharacters}
        </span>
      );
    }
  }

  renderCopyButton() {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    if ((this.props as any).updateSideEffects) {
      return (
        <button
          type="button"
          className="btn form__label__button"
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
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
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (this.props as any).hasWarning(this.props) &&
      this.state.searchResultTags.length === 0;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const hasError = (this.props as any).hasError(this.props);

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
          {this.state.capiError ? (
            <div className="form__field--external-error">
              {this.state.capiError}
            </div>
          ) : (
            ''
          )}
          <p className="details-list__field ">
            <TagFieldValue
              tagValue={this.state.tagValue}
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
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
          <label className="form__label">{this.props.fieldName}</label>
          {this.renderBylineInstructions()}
          {this.renderCopyButton()}
          {this.renderCharCount()}
        </div>

        {this.state.capiError ? (
          <div className="form__field--external-error">
            {this.state.capiError}
          </div>
        ) : (
          ''
        )}
        {this.renderTagPicker()}
        {this.renderAddedTags()}
        {hasWarning ? (
          <p className="form__message form__message--warning">
            {/* eslint-disable-next-line @typescript-eslint/no-explicit-any */}
            {(this.props as any).notification.message}
          </p>
        ) : (
          ''
        )}
        {hasError ? (
          <p className="form__message form__message--error">
            {/* eslint-disable-next-line @typescript-eslint/no-explicit-any */}
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

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function mapStateToProps(state: { config: { tagManagerUrl: any } }) {
  return {
    tagManagerUrl: state.config.tagManagerUrl
  };
}

export default connect(mapStateToProps)(TagPicker);
