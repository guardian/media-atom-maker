import { buildKeymap } from './utils/keymap';
import { keymap } from 'prosemirror-keymap';
import { Schema, DOMSerializer, DOMParser } from 'prosemirror-model';
import { EditorView } from 'prosemirror-view';
import { EditorState, Transaction } from 'prosemirror-state';
import { RefObject } from 'react';
import { history } from 'prosemirror-history';
import { menuBar } from 'prosemirror-menu';
import { buildMenuItems } from './menu';
import { baseKeymap } from 'prosemirror-commands';
import { EditorConfig } from './config';

const createBasePlugins = (schema: Schema, config: EditorConfig) => {
  const plugins = [
    keymap(buildKeymap(schema, {}, {}, config)),
    keymap(baseKeymap),
    history({ depth: 100, newGroupDelay: 500 }),
    menuBar({content: buildMenuItems(schema)})
  ];
  return plugins;
};

export const createEditorView = (
  onChange: (str: string) => void,
  editorEl: RefObject<HTMLDivElement>,
  contentEl: HTMLDivElement,
  schema: Schema,
  config: EditorConfig
) => {
  if (!editorEl.current) {
    return;
  }
  const ed: EditorView = new EditorView(editorEl.current, {
    state: EditorState.create({
      doc: DOMParser.fromSchema(schema).parse(contentEl, {
        preserveWhitespace: true
      }),
      plugins: createBasePlugins(schema, config)
    }),
    dispatchTransaction: (transaction: Transaction) => {
      const { state, transactions } = ed.state.applyTransaction(transaction);
      ed.updateState(state);

      if (transactions.some((tr: Transaction) => tr.docChanged)) {
        const serializer = DOMSerializer.fromSchema(schema);
        const outputHtml = serializer.serializeFragment(state.doc.content);
        // to format the outputHtml as an html string rather than a document fragment, we are creating a temporary div, adding it as a child, then using innerHTML which returns an html string
        const tmp = document.createElement('div');
        tmp.appendChild(outputHtml);
        if (onChange) {
          onChange(tmp.innerHTML);
        }
      }
    }
  });
  return ed;
};
