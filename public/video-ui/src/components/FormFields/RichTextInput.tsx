import React, { useRef, useEffect, useState } from 'react';
import { EditorView } from 'prosemirror-view';
import { createEditorView } from './richtext/setup';
import { MenuView } from './richtext/MenuView';
import { createSchema, EditorConfig } from './richtext/create-schema';

interface RichTextInputProps {
  value: string;
  onUpdate: (str: string) => void;
  config: EditorConfig;
  label?: string;
}

const RichTextInput = ({ value, onUpdate, config, label }: RichTextInputProps) => {
  // const config = {
  //     allowedNodes: [ "text", "hard_break"],
  //     allowedMarks: [
  //         "strong",
  //         "strike",
  //         "subscript",
  //         "superscript",
  //         "em",
  //         "link"
  //     ],
  //     inlineOnly: true
  // };
  const schema = createSchema(config);

  const editorEl = useRef<HTMLDivElement>(null);
  const [editorView, setEditorView] = useState<EditorView | undefined>(
    undefined
  );

  useEffect(() => {
    // Editor view takes an HTML Node therefore this string value needs to be converted into a node by placing in a div
    const contentNode = document.createElement('div');
    contentNode.innerHTML = value;
    const edView = createEditorView(onUpdate, editorEl, contentNode, schema);
    setEditorView(edView);
  }, []);

  return (
    <>
      {label && (
        <label className="prosemirror__label" htmlFor={label}>
          <span>{label}</span>
        </label>
      )}
      <div className="prosemirror__input">
        {editorView && <MenuView edView={editorView} schema={schema} />}
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
