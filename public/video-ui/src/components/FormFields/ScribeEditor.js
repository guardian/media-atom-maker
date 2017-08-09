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
  };

  componentDidMount() {
    this.updateWordCount(this.props.fieldValue);
  }

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

  componentDidMount() {
    this.scribe = new Scribe(this.refs.editor);

    this.configureScribe();

    this.scribe.on('content-changed', this.onContentChange);
    this.refs.editor.innerHTML = this.props.value;
  }

  configureScribe() {

    const allKeyboardShortcuts = {
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
