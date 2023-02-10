import { EditorView } from 'prosemirror-view';
import { toggleMark } from 'prosemirror-commands';
import React from 'react';
import { icons } from './icons';
import {
  linkItemCommand,
  removeAllMarksFromSelection,
  unlinkItemCommand
} from './utils/command-helpers';
import { Command, EditorState, Transaction } from 'prosemirror-state';
import { undo, redo } from 'prosemirror-history';
import { NodeSpec, NodeType, Schema } from 'prosemirror-model';
import { bulletList, listItem, wrapInList } from 'prosemirror-schema-list';


const wrapListItemCommand = (nodeType: NodeType, options: Record<string, string>) => {
  return wrapInList(nodeType, (options as any).attrs);
};

const wrapListCommand = (nodeType: NodeType) => wrapListItemCommand(nodeType, {
  title: "Wrap in bullet list",
  icon: icons.removeFormatting
});

export const MenuView = ({ edView, schema }: { edView: EditorView, schema: Schema }) => {
  const linkEl = document.createElement('i');
  linkEl.innerHTML = icons.link;
  const compulsaryMenuItems = [
    {
      command: undo,
      dom: icons.undo,
      title: 'undo'
    },
    {
      command: redo,
      dom: icons.redo,
      title: 'redo'
    },
    {
      command: (state: EditorState, dispatch: (tr: Transaction) => void) =>
        removeAllMarksFromSelection(state, dispatch),
      dom: icons.removeFormatting,
      title: 'remove-formatting'
    }
  ];
  const optionalMenuItems = [
    {
      command: toggleMark(schema.marks.strong),
      dom: icons.strong,
      title: 'bold',
      markName: 'strong'
    },
    {
      command: toggleMark(schema.marks.em),
      dom: icons.emphasis,
      title: 'italic',
      markName: 'em'
    },
    {
      command: linkItemCommand(schema.marks.link)(),
      dom: icons.link,
      title: 'add-link',
      markName: 'link'
    },
    {
      command: unlinkItemCommand(schema.marks.link),
      dom: icons.unlink,
      title: 'remove-link',
      markName: 'link'
    },
    {
      command: wrapListCommand(schema.nodes.bullet_list),
      dom: icons.unlink,
      title: 'bullet-list',
      nodeName: 'bullet_list'
    }
  ];
  const filteredMarkItems = optionalMenuItems.filter(menuItem => menuItem.markName && Object.keys(schema.marks).includes(menuItem.markName));
  const filteredNodeItems = optionalMenuItems.filter(menuItem => menuItem.nodeName && Object.keys(schema.nodes).includes(menuItem.nodeName));
  const menuItems = [...filteredMarkItems, ...filteredNodeItems, ...compulsaryMenuItems];
  console.log({...schema})
  return (
    <div className="prosemirror__menuBar">
      {menuItems.map((item, i) => {
        return (
          <div
            className="prosemirror__iconBox"
            onMouseDown={(e) => {
              e.preventDefault();
              item.command(edView.state, edView.dispatch);
            }}
            key={i}
            data-testid={item.title}
            dangerouslySetInnerHTML={{ __html: item.dom }}
          />
        );
      })}
    </div>
  );
};
