import { NodeType } from "prosemirror-model";
import { liftListItem, wrapInList } from "prosemirror-schema-list";
import { Command, EditorState, Transaction } from "prosemirror-state";
import { EditorView } from "prosemirror-view";

export function isInNode(
  state: EditorState,
  nodeType: NodeType
) {
  const { $from, $to } = state.selection;
  const types = [nodeType];
  const range = $from.blockRange(
      $to,
      (node) => types.indexOf(node.type) !== -1
  );
  return !!range && range.parent.type === nodeType;
}

export function createListToggleCommand(
  listItemNodeType: NodeType,
  listNodeType: NodeType
): Command {
  const wrapCommand = wrapInList(listNodeType);
  const unwrapCommand = liftListItem(listItemNodeType);
  return (state: EditorState,  dispatch: (tr: Transaction) => void, view: EditorView) => {
      const shouldUnwrap = isInNode(state, listNodeType);

      if(shouldUnwrap){
          return unwrapCommand(state, dispatch);
      }
      else{
          const unwrap = unwrapCommand(state, dispatch);
          const wrap = wrapCommand(view && view.state || state, dispatch);
          return unwrap || wrap;
      }
  }
}
