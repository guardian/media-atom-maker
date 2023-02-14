import OrderedMap from "orderedmap";
import { MarkSpec, NodeSpec, Schema } from "prosemirror-model";
import { schema } from "prosemirror-schema-basic";
import { addListNodes, bulletList, listItem } from "prosemirror-schema-list";
import _ from 'lodash';

export type EditorConfig = {
  inlineOnly: boolean;
  // The names of all the basic node types permitted in the schema.
  allowedNodes: string[];
  // The names of all the mark types permitted in the schema.
  allowedMarks: string[];
};

export const trailTextConfig = {
  allowedNodes: [ "text", "hard_break"],
  allowedMarks: [
      "strong",
      "strike",
      "subscript",
      "superscript",
      "em"
  ],
  inlineOnly: true
};

export const standfirstConfig = {
  allowedNodes: [ "text", "paragraph", "hard_break", "bullet_list", "list_item"],
  allowedMarks: [
      "strong",
      "strike",
      "subscript",
      "superscript",
      "em",
      "link"
  ],
  inlineOnly: false
};

const BaseInlineEditorNodeSpec = OrderedMap.from<NodeSpec>({
  doc: {
      content: "inline+",
      toDOM: () => ["div", 0]
  },
  text: schema.nodes.text
});

const BaseBlockEditorNodeSpec = OrderedMap.from<NodeSpec>({
  doc: {
      content: "block+",
      toDOM: () => ["div", 0]
  },
  text: schema.nodes.text
});

const nodeMap = OrderedMap.from({
  doc: {
    content: '(text | hard_break)+'
  },
  text: schema.nodes.text,
  hard_break: schema.nodes.hard_break
});

const listNodeSpecs = OrderedMap.from({
  list_item: listItem,
  bullet_list: bulletList
});

export const basicSchemaWithLists = new Schema({
  nodes: addListNodes(schema.spec.nodes, "paragraph *", "paragraph"),
  marks: schema.spec.marks
});

const filterNodes = (
  nodes: OrderedMap<NodeSpec>,
  allowedNodes: string[]
): OrderedMap<NodeSpec> => {
  let map = OrderedMap.from<NodeSpec>({});
  nodes.forEach((key, node) => {
      if (allowedNodes.includes(key)) {
          map = map.addToEnd(key, node);
      }
  });
  return map;
};

const filterMarks = (
  marks: OrderedMap<MarkSpec>,
  allowedMarks: string[]
): OrderedMap<MarkSpec> => {
  let map = OrderedMap.from<MarkSpec>({});
  marks.forEach((key, mark) => {
      if (allowedMarks.includes(key)) {
          map = map.addToEnd(key, mark);
      }
  });
  return map;
};

export const createSchema = <Config extends EditorConfig>(
  config: Config
): Schema => {
  const { allowedNodes, allowedMarks, inlineOnly } = config;

  const nodes: OrderedMap<NodeSpec> = inlineOnly
    ? BaseInlineEditorNodeSpec
    : BaseBlockEditorNodeSpec;

  const filteredNodeSpec = nodes.append(filterNodes(basicSchemaWithLists.spec.nodes, allowedNodes));

  const markSpec: MarkSpec = filterMarks(basicSchemaWithLists.spec.marks, allowedMarks)

  const schema = new Schema({
    nodes: filteredNodeSpec,
    marks: markSpec
  });

  return schema;
};
