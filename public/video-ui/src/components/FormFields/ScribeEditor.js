import React from 'react';
import PropTypes from 'prop-types';
import Scribe from 'scribe-editor';
import scribeKeyboardShortcutsPlugin from 'scribe-plugin-keyboard-shortcuts';
import scribePluginToolbar from 'scribe-plugin-toolbar';
import scribePluginLinkPromptCommand from 'scribe-plugin-link-prompt-command';
import scribePluginSanitizer from 'scribe-plugin-sanitizer';
import {keyCodes} from '../../constants/keyCodes';
import {requiredForComposerWarning} from '../../constants/requiredForComposerWarning';
import ReactTooltip from 'react-tooltip';

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
    if (!text) {
      return [];
    }

    return text
      .trim()
      .replace(/<(?:.|\n)*?>/gm, '')
      .split(/\s+/)
      .filter(_ => _.length !== 0);
  };

  updateWordCount = text => {
    const count = text ? this.getWords(text).length : 0;

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
    const hasWarning = this.state.wordCount === 0  && this.props.fieldLocation === 'trailText';

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
          className="details-list__field details-list__field--scribe"
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
            allowedEdits={this.props.allowedEdits}
            copiedValue={this.state.copiedValue}
          />
          {this.renderLimitWarning()}
        </div>
        {hasWarning
          ? <p className="form__message form__message--warning">
            {requiredForComposerWarning}
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
    ReactTooltip.rebuild();
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

    const allKeyboardShortcuts = {
      bold: function(event) {
        return event.metaKey && event.keyCode === keyCodes.b
      },
      italic: function(event) {
        return event.metaKey && event.keyCode === keyCodes.i;
      },
      linkPrompt: function(event) {
        return event.metaKey && !event.shiftKey && event.keyCode === keyCodes.k;
      },
      unlink: function(event) {
        return event.metaKey && event.shiftKey && event.keyCode === keyCodes.k;
      },
      insertUnorderedList: function(event) {
        return event.altKey && event.shiftKey && event.keyCode === keyCodes.b;
      }
    }

    // Create an instance of the Scribe toolbar
    this.scribe.use(scribePluginToolbar(this.refs.toolbar));

    const keyboardShortcuts = this.props.allowedEdits.reduce((shortcuts, edit) => {
      shortcuts[edit] = allKeyboardShortcuts[edit];
      return shortcuts;
    }, {});

    // Configure Scribe plugins
    this.scribe.use(scribePluginLinkPromptCommand());
    this.scribe.use(
      scribeKeyboardShortcutsPlugin(keyboardShortcuts)
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

    const getEditButtonText = (name) => {
      switch(name) {
        case 'bold':
          return 'Bold';
        case 'italic':
          return 'Italic';
        case 'linkPrompt':
          return 'Link';
        case 'unlink':
          return 'Unlink';
        case 'insertUnorderedList':
          return 'Bullet point';
      }
    }


    return (
      <div className="scribe__container">
        <div ref="toolbar" className="scribe__toolbar">
          {this.props.allowedEdits.map(edit => {
            return (
              <button
                type="button"
                data-command-name={edit}
                className="scribe__toolbar__item"
                key={edit}
              >
                {getEditButtonText(edit)}
              </button>
            );
          })}
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
