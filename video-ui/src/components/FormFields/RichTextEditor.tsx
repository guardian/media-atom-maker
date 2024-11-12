import React, { useRef, useEffect, useState, useCallback } from 'react';
import { EditorView } from 'prosemirror-view';
import { createEditorView } from './richtext/setup';
import { createSchema } from './richtext/createSchema';
import { AllSelection, Transaction } from 'prosemirror-state';
import { DOMParser as parseDOM, Node } from 'prosemirror-model';
import { EditorConfig } from './richtext/config';
import { paragraphToWhitespace } from './richtext/utils/richTextHelpers';


interface RichTextEditorProps {
  value: string;
  onUpdate: (str: string) => void;
  config: EditorConfig;
  label?: string;
  shouldAcceptCopiedText: boolean;
}

export const RichTextEditor = ({ value, onUpdate, config, label, shouldAcceptCopiedText = false }: RichTextEditorProps) => {
  const schema = createSchema(config);

  const editorEl = useRef<HTMLDivElement>(null);
  const localOnUpdate = useCallback((str: string) => {
    setLocalValue(str);
    onUpdate(str);
  }, [onUpdate]);

  const [localValue, setLocalValue] = useState<string>(value);
  const [editorView, setEditorView] = useState<EditorView | undefined>(
    undefined
  );

  useEffect(() => {
    // Editor view takes an HTML Node therefore this string value needs to be converted into a node by placing in a div
    const contentNode = document.createElement('div');
    contentNode.innerHTML = value;
    const edView = createEditorView(localOnUpdate, editorEl, contentNode, schema, config);
    setEditorView(edView);
  }, []);

  useEffect(() => {
    const shouldUpdateFromIncomingValue = value !== localValue;
    if (!editorView || !shouldAcceptCopiedText || !shouldUpdateFromIncomingValue) {
      return;
    }

    // Clear the current document
    const selectAll = editorView.state.tr.setSelection(new AllSelection(editorView.state.doc));
    editorView.dispatch(selectAll);

    const valueWithParagraphsAsHardBreaks = paragraphToWhitespace(value);
    editorView.pasteHTML(valueWithParagraphsAsHardBreaks);
  }, [value]);

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
