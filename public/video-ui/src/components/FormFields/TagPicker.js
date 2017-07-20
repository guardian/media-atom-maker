import React from 'react';
import ContentApi from '../../services/capi';
import { tagsFromStringList, tagsToStringList } from '../../util/tagParsers';
import removeStringTagDuplicates from '../../util/removeStringTagDuplicates';
import { keyCodes } from '../../constants/keyCodes';
import UserActions from '../../constants/UserActions';
import TagTypes from '../../constants/TagTypes';
import DragSortableList from 'react-drag-sortable';

export default class TagPicker extends React.Component {
  state = {
    capiTags: [],
    inputString: '',
    lastAction: UserActions.other,
    tagValue: [],
    capiUnavailable: false
  };

  componentDidMount() {
    if (this.props.fieldValue !== this.props.placeholder) {
      tagsFromStringList(this.props.fieldValue, this.props.tagType)
        .then(result => {
          this.setState({
            tagValue: result
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

  parseTags = results => {
    return results.map(result => {
      if (this.props.tagType === TagTypes.keyword) {
        let detailedTitle;

        //Some webtitles on keyword tags are too unspecific and we need to add
        //the section name to them to know what tags they are referring to

        if (
          result.webTitle !== result.sectionName &&
          result.webTitle.split(' ').length <= 2
        ) {
          detailedTitle = result.webTitle + ' (' + result.sectionName + ')';
        } else {
          detailedTitle = result.webTitle;
        }

        return { id: result.id, webTitle: detailedTitle };
      }
      return { id: result.id, webTitle: result.webTitle };
    });
  };

  onUpdate = newValue => {
    this.setState({
      tagValue: newValue
    });
    this.props.onUpdateField(tagsToStringList(newValue));
  };

  getYoutubeInputValue = () => {
    if (
      this.state.tagValue.every(value => {
        return value.id !== this.state.inputString;
      })
    ) {
      return {
        id: this.state.inputString,
        webTitle: this.state.inputString
      };
    }
    return [];
  };

  updateInput = e => {
      // If the user did not add new text input, we update the tag search
    if (this.state.lastAction === UserActions.delete) {
      const length = this.state.tagValue.length;
      const lastInput = this.state.tagValue[length - 1];

      this.setState({
        inputString: lastInput,
        lastAction: UserActions.other
      });

      const newValue = this.state.tagValue.slice(
        0,
        this.state.tagValue.length - 1
      );
      this.onUpdate(newValue);
    } else {
      this.setState({
        inputString: e.target.value
      });

      if (!this.props.disableCapiTags) {
        const searchText = e.target.value;

        ContentApi.getTagsByType(searchText, this.props.tagType)
          .then(capiResponse => {
            const tags = this.parseTags(capiResponse.response.results);
            this.setState({
              capiTags: tags
            });

            // Because the keyword tag input field is inside
            // a component that makes the keywords sortable,
            // we need to refocus to the input field to keep
            // typing.
            if (this.props.tagType === TagTypes.keyword) {
              this.refs.keywordInput.focus()
            }
          })
          .catch(() => {
            this.setState({
              capiTags: [],
              capiUnavailable: true
            });
          });
      }
    }
  };

  processTagInput = e => {
    if (e.keyCode === keyCodes.enter) {
      const onlyWhitespace = !/\S/.test(this.state.inputString);
      if (!onlyWhitespace) {

        const newInput = this.props.tagType === TagTypes.youtube ? this.getYoutubeInputValue() : this.state.inputString;

        const newFieldValue = this.state.tagValue.concat([newInput]);

        this.onUpdate(newFieldValue);
        this.setState({
          inputString: '',
          capiTags: []
        });
      }

    } else if (e.keyCode === keyCodes.backspace) {
      if (this.state.inputString.length === 0) {
        const lastInput = this.state.tagValue[this.state.tagValue.length - 1];

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

  renderFieldValue(value, index) {
    if (value.webTitle) {
      return (
        <span key={`${value.id}-${index}`}>
          <span className="form__field__tag__display">{value.webTitle}</span>
          {' '}
        </span>
      );
    } else {
      return `${value} `;
    }
  }

  renderTags(tag) {
    const addTag = () => {
      const valueWithoutStringDupes = removeStringTagDuplicates(
        tag,
        this.state.tagValue
      );
      const newFieldValue = valueWithoutStringDupes.concat([tag]);

      this.setState({
        inputString: ''
      });

      this.onUpdate(newFieldValue);
      this.setState({
        capiTags: []
      });
    };

    return (
      <a
        className="form__field__tags"
        key={tag.id}
        title={tag.id}
        onClick={addTag}
      >
        {' '}{tag.webTitle}{' '}
      </a>
    );
  }

  renderValue = (field, i) => {
    const removeFn = () => {
      const newFieldValue = this.state.tagValue.filter(oldField => {
        return field.id !== oldField.id;
      });

      this.setState({
        inputString: ''
      });
      this.onUpdate(newFieldValue);
    };

    if (field.id) {
      return (
        <span
          className="form__field--multiselect__value"
          key={`${field.id}-${i}`}
          onClick={removeFn}
        >
          {field.webTitle}{' '}
        </span>
      );
    }
    return (
      <span
        className="form__field--multistring__value"
        key={`${field.id}-${i}`}
      >
        {' '}{field}{' '}
      </span>
    );
  };

  renderCapiUnavailable() {
    if (this.state.capiUnavailable) {
      return (
        <span className="form__field--external__error">
          Tags are currently unavailable
        </span>
      );
    }
  }

  renderTextInputElement(lastElement) {
    const getInputPlaceholder = () => {
      if (!this.props.fieldValue || this.props.fieldValue.length === 0) {
        return this.props.inputPlaceholder;
      }
      return '';
    };

    if (this.props.disableTextInput) {
      return (
        <span className="form__field__tag--container">
          {lastElement && this.renderValue(lastElement, 0)}
          <input
            type="text"
            className={
              'form__field__tag--input' +
                (getInputPlaceholder().length !== 0
                  ? ' form__field__tag--input--empty'
                  : '')
            }
            id={this.props.fieldName}
            ref={this.props.tagType + 'Input'}
            onChange={this.updateInput}
            value={this.state.inputString}
            placeholder={getInputPlaceholder()}
          />
        </span>
      );
    }

    return (
      <span className="form__field__tag--container">
        {lastElement && this.renderValue(lastElement, 0)}
        <input
          type="text"
          className="form__field__tag--input"
          id={this.props.fieldName}
          onKeyDown={this.processTagInput}
          onChange={this.updateInput}
          value={this.state.inputString}
          placeholder={getInputPlaceholder()}
        />
      </span>
    );
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

  onSort = (sortedList) => {

    const newTagValues = sortedList.reduce((newTagValues, sortedValue) => {

      //For each component in the list of dragged elements,
      //we have to extract the name of the tag it represents.
      const child = sortedValue.content.props.children[0];

      const tagTitle = typeof child === 'string' ? child : child.props.children[0];

      const tagValue = this.state.tagValue.find(value => value.webTitle === tagTitle);

      newTagValues.push(tagValue);
      return newTagValues;
    }, []);

    this.onUpdate(newTagValues);
  }

  renderInputElements() {

    const valueLength = this.state.tagValue.length;
    const lastElement = !valueLength || valueLength === 0
      ? null
      : this.state.tagValue[valueLength - 1];

    if (this.props.tagType !== TagTypes.keyword) {
      return (
        <div className="form__field__tag--selector">
          {valueLength
            ? this.state.tagValue.map((value, i) => {
                if (i < valueLength - 1) {
                  return this.renderValue(value, i);
                }
              })
            : ''}

          {this.renderTextInputElement(lastElement)}

        </div>

      );

    } else {
      const existingItems = this.state.tagValue.reduce((values, value, i) => {
        if (i < valueLength - 1) {
          values.push({ content: this.renderValue(value, i) });
        }
        return values;
      }, [])

      const items = existingItems.concat([{content: this.renderTextInputElement(lastElement)}]);

      return (
        <div className="form__field__tag--selector">
          <DragSortableList
            items={items}
            dropBackTransitionDuration={0.3}
            type="horizontal"
            onSort={this.onSort}
            placeholder={<span></span>}
          />
        </div>
      );

    }

  }
  render() {
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
          <p className="details-list__field ">
            {this.state.tagValue.map(this.renderFieldValue)}
          </p>
        </div>
      );
    }

    return (
      <div className="form__row">

        <div className="form__label__layout">
          <label className="form__label">{this.props.fieldName}</label>
          {this.renderBylineInstructions()}
        </div>
        {this.renderCapiUnavailable()}

        {this.renderInputElements()}

        {this.state.capiTags.length !== 0
          ? <div className="form__field__tags">
              {this.state.capiTags.map(tag => this.renderTags(tag))}
            </div>
          : ''}

        {this.state.tagValue.map(this.renderFieldValue)}

      </div>
    );
  }
}
