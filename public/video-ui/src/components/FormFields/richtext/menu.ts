import { toggleMark } from "prosemirror-commands";
import { MenuElement, MenuItem, MenuItemSpec } from "prosemirror-menu";
import { MarkType, NodeType, Schema } from "prosemirror-model";
import { Command, EditorState } from "prosemirror-state";
import { isInNode, toggleBulletListCommand } from "./utils/listsHelpers";
import { linkItemCommand, unlinkItemCommand } from "./utils/command-helpers";

export const cmdItem = (cmd: Command, options: Partial<MenuItemSpec>) => {
  const passedOptions = {
    label: options.title as string,
    run: cmd,
    ...options
  };
  passedOptions[options.enable ? "enable" : "select"] = state => cmd(state);

  return new MenuItem(passedOptions);
};

export const markActive = (state: EditorState, type: MarkType) => {
  const {from, $from, to, empty} = state.selection;
  if (empty) return type.isInSet(state.storedMarks || $from.marks());
  else return state.doc.rangeHasMark(from, to, type);
};

export const markItem = (markType: MarkType, options: Partial<MenuItemSpec>) => {
  const passedOptions: Partial<MenuItemSpec> = {
    active: (state: EditorState) => { return !!markActive(state, markType); },
    enable: () => true,
    ...options
  };
  return cmdItem(toggleMark(markType), passedOptions);
};

const linkItem = (markType: MarkType, options: Partial<MenuItemSpec>) => {
  const passedOptions: MenuItemSpec = {
    active: (state) => { return !!markActive(state, markType); },
    enable: (state) => { return !state.selection.empty; },
    run: linkItemCommand(markType),
    label: options.title as string,
    ...options
  };
  return new MenuItem(passedOptions);
};

const unlinkItem = (markType: MarkType, options: Partial<MenuItemSpec>) => {
  const passedOptions: MenuItemSpec = {
    active: (state) => { return !!markActive(state, markType); },
    enable: (state) => { return !!markActive(state, markType); },
    run: unlinkItemCommand(markType),
    label: options.title as string,
    ...options
  };
  return new MenuItem(passedOptions);
};

const wrapListItem = (schema: Schema) => (nodeType: NodeType, options: Partial<MenuItemSpec>) => {
  const passedOptions: Partial<MenuItemSpec> = {
    active: (state) => { return isInNode(state, nodeType);},
    ...options
  };
  return cmdItem(toggleBulletListCommand(schema), passedOptions);
};

export const buildMenuItems = (schema: Schema) => {
  const markMenu: MenuElement[] = [];
  if (schema.marks.strong){
    markMenu.push(markItem(schema.marks.strong, {title: 'Bold', label: "format_bold" }));
  }
  if (schema.marks.em){
    markMenu.push(markItem(schema.marks.em, {title: 'Italic', label: "format_italic" }));
  }
  if (schema.marks.link){
    markMenu.push(linkItem(schema.marks.link, {title: 'Link', label: "link" }));
    markMenu.push(unlinkItem(schema.marks.link, {title: 'Unlink', label: "link_off" }));
  }
  if (schema.nodes.bullet_list && schema.nodes.list_item){
    markMenu.push(wrapListItem(schema)(schema.nodes.bullet_list, {
      title: "Bullet list",
      label: "format_list_bulleted",
      enable: () => true
    }));
  }

  return [markMenu];
};
