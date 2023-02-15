import React, { useRef, useEffect, useState } from 'react';
import { EditorView } from 'prosemirror-view';
import { createEditorView } from './richtext/setup';
import { createSchema } from './richtext/create-schema';
import { AllSelection, Transaction } from 'prosemirror-state';
import { DOMParser as parseDOM, Node } from 'prosemirror-model';
import { EditorConfig } from './richtext/config';


interface RichTextInputProps {
  value: string;
  onUpdate: (str: string) => void;
  config: EditorConfig;
  label?: string;
  copiedValue?: {
    text: string,
    seed: number
  }
  shouldAcceptCopiedText: boolean;
}

const RichTextInput = ({ value, onUpdate, config, label, copiedValue, shouldAcceptCopiedText = false }: RichTextInputProps) => {
  const schema = createSchema(config);

  const editorEl = useRef<HTMLDivElement>(null);
  const [editorView, setEditorView] = useState<EditorView | undefined>(
    undefined
  );

  useEffect(() => {
    // Editor view takes an HTML Node therefore this string value needs to be converted into a node by placing in a div
    const contentNode = document.createElement('div');
    contentNode.innerHTML = value;
    const edView = createEditorView(onUpdate, editorEl, contentNode, schema, config);
    setEditorView(edView);
  }, []);

  const paragraphToWhitespace = (string: string) => {
    // Only the first paragraph tag
    string = string.replace("<p>","");
    // Replace subsequent paragraph tags with hard breaks
    string = string.replace(/<p>/g,"<br>");
    // Remove all paragraph closing tags
    string = string.replace(/<\/p>/g,"");
    return `${string}`;
  };

  const setContent = (tr: Transaction, node: Node): Transaction => {
    const sel = new AllSelection(tr.doc);
    sel.replaceWith(tr, node);
    return tr;
  };

  useEffect(() => {
    if (copiedValue.text && editorView && shouldAcceptCopiedText){
      const parser = parseDOM.fromSchema(schema);
      const domParser = new DOMParser();
      const dom = domParser.parseFromString("", "text/html").body;
      const doc = parser.parse(dom);
      // Empty the text content of the derived field (trail text)
      editorView.dispatch(
          setContent(editorView.state.tr, doc)
      );
      // Add the new content to that field
      const valueWithParagraphsAsHardBreaks = paragraphToWhitespace(copiedValue.text);
      editorView.pasteHTML(valueWithParagraphsAsHardBreaks);
    }
  }, [copiedValue.text, copiedValue.seed]);

  return (
    <>
      {label && (
        <label className="prosemirror__label" htmlFor={label}>
          <span>{label}</span>
        </label>
      )}
      <div className="prosemirror__input">
        <div
          className="ProseMirror-example-setup-style"
          ref={editorEl}
          data-testid="edit-form-rich-text"
        />
      </div>
    </>
  );
};

export { RichTextInput };
