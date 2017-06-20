import React from 'react';
import PropTypes from 'prop-types';
import Scribe from 'scribe';
import scribeKeyboardShortcutsPlugin from 'scribe-plugin-keyboard-shortcuts';
import scribePluginToolbar from 'scribe-plugin-toolbar';
import scribePluginLinkPromptCommand from 'scribe-plugin-link-prompt-command';
import scribePluginSanitizer from 'scribe-plugin-sanitizer';

export default class ScribeEditorField extends React.Component {
  state = {
    wordCount: 0,
    copiedValue: null
  };

  componentDidMount() {
    this.updateWordCount(this.props.fieldValue);
  }

  updateValueFromCopy = () => {
    this.props.onUpdateField(this.props.derivedFrom);
    this.setState({
      copiedValue: this.props.derivedFrom
    });
  };

  getWords = text => {
    return text
      .trim()
      .replace(/<(?:.|\n)*?>/gm, '')
      .split(/\s+/)
      .filter(_ => _.length !== 0);
  };

  updateWordCount = text => {
    const count = this.getWords(text).length;

    this.setState({
      wordCount: count,
      isTooLong: false
    });
  };

  isTooLong = value => {
    const wordLength = this.getWords(value).reduce((length, word) => {
      length += word.length;
      return length;
    }, 0);
    return (
      wordLength > this.props.maxLength ||
      value.length > this.props.maxCharLength
    );
  };

  updateFieldValue = value => {
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
      >
        <i className="icon">edit</i>
        <span data-tip="Copy trail text from description" />
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
    const hasWarning = this.state.wordCount === 0;

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
          className="details-list__field "
          dangerouslySetInnerHTML={{ __html: this.props.fieldValue }}
        />
      );
    }

    return (
      <div>
        <div
          className={(this.props.formRowClass || 'form__row') + ' scribe__row'}
        >
          <ScribeEditor
            fieldName={this.props.fieldName}
            value={this.props.fieldValue}
            onUpdate={this.updateFieldValue}
            copiedValue={this.state.copiedValue}
          />
          {this.renderLimitWarning()}
        </div>
        {hasWarning
          ? <p className="form__message form__message--warning">
              This field is recommended
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

export class ScribeEditor extends React.Component {
  static propTypes = {
    fieldName: PropTypes.string,
    value: PropTypes.string,
    onUpdate: PropTypes.func
  };

  state = {
    copiedValue: null
  };

  componentDidMount() {
    this.scribe = new Scribe(this.refs.editor);

    this.configureScribe();

    this.scribe.on('content-changed', this.onContentChange);
    this.refs.editor.innerHTML = this.props.value;
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.copiedValue !== this.state.copiedValue) {
      this.refs.editor.innerHTML = nextProps.copiedValue;
      this.setState({
        copiedValue: nextProps.copiedValue
      });
    }
  }

  configureScribe() {
    // Create an instance of the Scribe toolbar
    this.scribe.use(scribePluginToolbar(this.refs.toolbar));

    // Configure Scribe plugins
    this.scribe.use(scribePluginLinkPromptCommand());
    this.scribe.use(
      scribeKeyboardShortcutsPlugin({
        bold: function(event) {
          return event.metaKey && event.keyCode === 66;
        }, // b
        italic: function(event) {
          return event.metaKey && event.keyCode === 73;
        }, // i
        linkPrompt: function(event) {
          return event.metaKey && !event.shiftKey && event.keyCode === 75;
        }, // k
        unlink: function(event) {
          return event.metaKey && event.shiftKey && event.keyCode === 75;
        }, // shft + k
        insertUnorderedList: function(event) {
          return event.altKey && event.shiftKey && event.keyCode === 66;
        } // b
      })
    );

    this.scribe.use(
      scribePluginSanitizer({
        tags: {
          p: {},
          i: {},
          b: {},
          a: {
            href: true
          },
          ul: {},
          ol: {},
          li: {}
        }
      })
    );
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.value !== this.refs.editor.innerHTML;
  }

  onContentChange = () => {
    const newContent = this.refs.editor.innerHTML;
    if (newContent !== this.props.value) {
      this.props.onUpdate(newContent);
    }
  };

  render() {
    return (
      <div className="scribe__container">
        <div ref="toolbar" className="scribe__toolbar">
          <button
            type="button"
            data-command-name="bold"
            className="scribe__toolbar__item"
          >
            Bold
          </button>
          <button
            type="button"
            data-command-name="italic"
            className="scribe__toolbar__item"
          >
            Italic
          </button>
          <button
            type="button"
            data-command-name="linkPrompt"
            className="scribe__toolbar__item"
          >
            Link
          </button>
          <button
            type="button"
            data-command-name="unlink"
            className="scribe__toolbar__item"
          >
            Unlink
          </button>
          <button
            type="button"
            data-command-name="insertUnorderedList"
            className="scribe__toolbar__item"
          >
            Bullet point
          </button>
        </div>
        <div
          id={this.props.fieldName}
          ref="editor"
          className="scribe__editor"
        />
      </div>
    );
  }
}
