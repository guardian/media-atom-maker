import { chainCommands, createParagraphNear, liftEmptyBlock, newlineInCode, splitBlockKeepMarks, toggleMark } from 'prosemirror-commands';
import { linkItemCommand, unlinkItemCommand } from './command-helpers';
import { Schema } from 'prosemirror-model';
import { undo, redo } from 'prosemirror-history';
import { EditorState, Transaction } from 'prosemirror-state';
import { EditorConfig } from '../create-schema';
import { toggleBulletListCommand } from '../lists';
import { splitListItem } from 'prosemirror-schema-list';

// These prosemirror-helper functions are a simplified version of what we use in Composer, and have been lifted and shifted from that repo

interface MapObject {
  [key: string]: any;
}
const mac =
  typeof navigator !== undefined ? /Mac/.test(navigator.platform) : false;

const createAddHardBreak = (schema: Schema) => (
  state: EditorState,
  dispatch?: (tr: Transaction) => void
) => {
  if (dispatch) {
    dispatch(
      state.tr
        .replaceSelectionWith(schema.nodes.hard_break.create())
        .scrollIntoView()
    );
  }
  return true;
};

const addParagraph = chainCommands(
      newlineInCode,
      createParagraphNear,
      liftEmptyBlock,
      splitBlockKeepMarks
  );

export const buildKeymap = (schema: Schema, init = {}, mapKeys: MapObject, config: EditorConfig) => {
  const keys: MapObject = init;
  const bind = (key: string, cmd: any) => {
    if (mapKeys) {
      const mapped = mapKeys[key];
      if (mapped === false) {
        return;
      }
      if (mapped) {
        key = mapped;
      }
    }
    keys[key] = cmd;
  };

  bind('Mod-z', undo);
  bind('Shift-Mod-z', redo);

  if (!mac) {
    bind('Mod-y', redo);
  }

  if (config.inlineOnly === true){
    bind('Enter', createAddHardBreak(schema));
  } else {
    bind(
      "Enter",
      splitListItem(schema.nodes.list_item)
  );
  }

  if (schema.marks.strong) {
    bind('Mod-b', toggleMark(schema.marks.strong));
  }

  if (schema.marks.bullet_list) {
    bind('Mod-m', toggleBulletListCommand(schema));
  }

  if (schema.marks.em) {
    bind('Mod-i', toggleMark(schema.marks.em));
  }

  if (schema.marks.link) {
    bind('Mod-k', linkItemCommand(schema.marks.link));
    bind('Shift-Mod-k', unlinkItemCommand(schema.marks.link));
  }

  return keys;
};
