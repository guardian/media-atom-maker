import React from 'react';
import RequiredForComposer from '../../constants/requiredForComposer';
import { EditorConfig } from './richtext/config';
import { RichTextInput } from './RichTextInput';

type EditorState = {
  wordCount: number;
  copiedValue: {
    text: string | null;
    seed: number | null;
  }
  isTooLong?: boolean;
}
type EditorProps = {
  fieldValue: string;
  derivedFrom: string;
  onUpdateField: (string: string) => any;
  maxLength: number;
  maxCharLength: number;
  editable: boolean;
  fieldLocation: string;
  fieldName: string;
  config: EditorConfig;
  placeholder: string;
  isDesired?: boolean;
  isRequired?: boolean;
}

export default class EditorField extends React.Component<EditorProps, EditorState> {
  state: EditorState = {
    wordCount: 0,
    copiedValue: {
      text: null,
      seed: null
    }
  };

  componentDidMount() {
    this.updateWordCount(this.props.fieldValue);
  }

  updateValueFromCopy = () => {
    this.props.onUpdateField(this.props.derivedFrom);
    this.setState({
      copiedValue: {text: this.props.derivedFrom, seed: Math.random()}
    });
  };

  getWords = (text: string) => {
    if (!text) {
      return [];
    }

    return text
      .trim()
      .replace(/<(?:.|\n)*?>/gm, '')
      .split(/\s+/)
      .filter(_ => _.length !== 0);
  };

  updateWordCount = (text: string) => {
    const count = text ? this.getWords(text).length : 0;

    this.setState({
      wordCount: count,
      isTooLong: false
    });
  };

  isTooLong = (value: string) => {
    const wordLength = this.getWords(value).reduce((length, word) => {
      length += word.length;
      return length;
    }, 0);
    return (
      wordLength > this.props.maxLength ||
      value.length > this.props.maxCharLength
    );
  };

  updateFieldValue = (value: string) => {
    if (!this.isTooLong(value)) {
      this.setState({
        isTooLong: false
      });

      this.updateWordCount(value);
      this.props.onUpdateField(value);
    } else {
      this.setState({
        isTooLong: true
      });
    }
  };

  renderCopyButton = () => {
    if (this.props.derivedFrom === undefined || !this.props.editable) {
      return null;
    }

    return (
        <button
          type="button"
          disabled={!this.props.derivedFrom}
          className="btn form__label__button"
          onClick={this.updateValueFromCopy}
          data-tip="Copy trail text from description"
          data-place="top"
        >
          <i className="icon">edit</i>
        </button>
        );
  };

  renderLimitWarning = () => {
    return (
      <div>
        {this.state.isTooLong
          ? <span className="form__message__text--error">
              {' '}(This text is too long: updates will not get saved)
            </span>
          : false}
      </div>
    );
  };

  renderField() {
    const requiresValidation = this.state.wordCount === 0  && this.props.fieldLocation === 'trailText';
    const hasWarning = requiresValidation && this.props.isDesired;
    const hasError = requiresValidation && this.props.isRequired;
    if (!this.props.editable) {
      if (this.state.wordCount === 0) {
        return (
          <div className="details-list__field details-list__empty">
            {this.props.placeholder}
          </div>
        );
      }
      return (
        <div
          className="details-list__field details-list__field-with-content"
          dangerouslySetInnerHTML={{ __html: this.props.fieldValue }}
        />
      );
    }

    return (
      <div>
        <div
          className={'form__row editor__row'}
        >
          <RichTextInput
            value={this.props.fieldValue}
            onUpdate={this.updateFieldValue}
            copiedValue={this.state.copiedValue}
            config={this.props.config}
            shouldAcceptCopiedText={!!this.props.derivedFrom}
          />
          {this.renderLimitWarning()}
        </div>
        {hasWarning
          ? <p className="form__message form__message--warning">
            {RequiredForComposer.warning}
            </p>
          : ''}
        {hasError
          ? <p className="form__message form__message--error">
            {RequiredForComposer.error}
            </p>
          : ''}
      </div>
    );
  }

  render() {
    return (
      <div className="form__row">
        <div className="form__label__layout">
          <label className="form__label">{this.props.fieldName}</label>
          {this.renderCopyButton()}
        </div>
        {this.renderField()}
      </div>
    );
  }
}
