import React from 'react';
import RequiredForComposer from '../../constants/requiredForComposer';
import { EditorConfig } from './richtext/config';
import { getWords, isTooLong } from './richtext/utils/richTextHelpers';
import { RichTextEditor } from './RichTextEditor';

type EditorState = {
  wordCount: number;
  isTooLong?: boolean;
}
type EditorProps = {
  fieldValue: string;
  derivedFrom: string;
  onUpdateField: (string: string) => any;
  maxWordLength: number;
  editable: boolean;
  fieldLocation: string;
  fieldName: string;
  config: EditorConfig;
  placeholder: string;
  isDesired?: boolean;
  isRequired?: boolean;
}

export default class RichTextField extends React.Component<EditorProps, EditorState> {
  state: EditorState = {
    wordCount: 0
  };

  componentDidMount() {
    this.updateWordCount(this.props.fieldValue);
  }

  updateValueFromCopy = () => {
    this.props.onUpdateField(this.props.derivedFrom);
  };

  updateWordCount = (text: string) => {
    const count = text ? getWords(text).length : 0;

    this.setState({
      wordCount: count,
      isTooLong: false
    });
  };

  updateFieldValue = (value: string) => {
    if (!isTooLong(value, this.props.maxWordLength)) {
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
          className="btn form__label__button form__label__copy-button"
          onClick={this.updateValueFromCopy}
          data-tip="Copy trail text from description"
          data-place="top"
        >
          Copy from standfirst
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
          <RichTextEditor
            value={this.props.fieldValue}
            onUpdate={this.updateFieldValue}
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
        <div className="form__label__layout form__label__layout__rich-text">
          <label className="form__label">{this.props.fieldName}</label>
          {this.renderCopyButton()}
        </div>
        {this.renderField()}
      </div>
    );
  }
}
