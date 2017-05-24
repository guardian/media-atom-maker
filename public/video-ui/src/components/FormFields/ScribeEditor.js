import React, { PropTypes } from 'react';
import Scribe from 'scribe';
import scribeKeyboardShortcutsPlugin from 'scribe-plugin-keyboard-shortcuts';
import scribePluginToolbar from 'scribe-plugin-toolbar';
import scribePluginLinkPromptCommand from 'scribe-plugin-link-prompt-command';
import scribePluginSanitizer from 'scribe-plugin-sanitizer';
import CopyTrailButton from '../FormComponents/CopyTrailButton';

export default class ScribeEditorField extends React.Component {
  state = {
    wordCount: 0
  };

  renderCopyButton = () => {
    if (this.props.derivedFrom === undefined) {
      return null;
    }

    return (
      <CopyTrailButton
        onUpdateField={this.props.onUpdateField}
        derivedFrom={this.props.derivedFrom}
      />
    );
  };

  wordCount = text =>
    text
      .trim()
      .replace(/<(?:.|\n)*?>/gm, '')
      .split(/\s+/)
      .filter(_ => _.length !== 0).length;

  renderWordCount = () => {
    const wordCount = this.wordCount(this.props.fieldValue);
    const tooLong =
      this.props.suggestedLength && wordCount > this.props.suggestedLength;
    return (
      <div>
        <span className="form__message__text">{wordCount} words</span>
        {tooLong
          ? <span className="form__message__text--error"> (too long)</span>
          : false}
      </div>
    );
  };

  renderField() {
    const hasWarning = this.state.wordCount === 0;

    function getClassName() {
      if (hasWarning) {
        return 'form__field form__field--warning';
      }
      return '';
    }

    if (!this.props.editable) {
      return (
        <div
          className={
            'details-list__field ' +
              (this.props.displayPlaceholder(
                this.props.placeholder,
                this.props.fieldValue
              )
                ? 'details-list__empty'
                : '')
          }
          dangerouslySetInnerHTML={{ __html: this.props.fieldValue }}
        />
      );
    }

    return (
      <div className={getClassName()}>
        {this.props.fieldLabel
          ? <label htmlFor={this.props.fieldName} className="form__label">
              {this.props.fieldLabel}
            </label>
          : false}
        <ScribeEditor
          fieldName={this.props.fieldName}
          value={this.props.fieldValue}
          onUpdate={this.props.onUpdateField}
        />
        {this.renderWordCount()}
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
        <p className="details-list__title">{this.props.fieldName}</p>
        {this.renderField()}
        {this.renderCopyButton()}
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
      <div>
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
            Insert list
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
