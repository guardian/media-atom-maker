import React from 'react';
import ContentApi from '../../services/capi';
import { tagsFromStringList, tagsToStringList } from '../../util/tagParsers';
import removeStringTagDuplicates from '../../util/removeStringTagDuplicates';
import { keyCodes } from '../../constants/keyCodes';
import UserActions from '../../constants/UserActions';

export default class ComposerTagPicker extends React.Component {
  state = {
    addedTags: [],
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

  onUpdate = newValue => {
    this.setState({
      tagValue: newValue
    });
    this.props.onUpdateField(tagsToStringList(newValue));
  };

  updateInput = e => {
    if (this.state.lastAction === UserActions.space) {
      const newFieldValue = this.state.tagValue.concat([
        this.state.inputString
      ]);
      this.onUpdate(newFieldValue);
      this.setState({
        inputString: ''
      });

      // If the user did not add new text input, we update the tag search
    } else if (this.state.lastAction === UserActions.delete) {
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

      const searchText = e.target.value;

      ContentApi.getTagsByType(searchText, this.props.tagType)
        .then(capiResponse => {
          const tags = capiResponse.response.results.map(result => {
            return { id: result.id, webTitle: result.webTitle };
          });
          this.setState({
            addedTags: tags
          });
        })
        .catch(() => {
          this.setState({
            addedTags: [],
            capiUnavailable: true
          });
        });
    }
  };

  processTagInput = e => {
    if (e.keyCode === keyCodes.space) {
      this.setState({
        lastAction: UserActions.space
      });
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
        addedTags: []
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

    const valueLength = this.state.tagValue.length;
    const lastElement = !valueLength || valueLength === 0
      ? null
      : this.state.tagValue[valueLength - 1];
    return (
      <div className="form__row">

        <div className="form__label__layout">
          <label className="form__label">{this.props.fieldName}</label>
        </div>
        {this.renderCapiUnavailable()}

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

        {this.state.addedTags.length !== 0
          ? <div className="form__field__tags">
              {this.state.addedTags.map(tag => this.renderTags(tag))}
            </div>
          : ''}

        {this.state.tagValue.map(this.renderFieldValue)}

      </div>
    );
  }
}
