import OrderedMap from "orderedmap";
import { MarkSpec, NodeSpec, Schema } from "prosemirror-model";
import { schema } from "prosemirror-schema-basic";
import { addListNodes, bulletList, listItem } from "prosemirror-schema-list";
import _ from 'lodash';
import { EditorConfig } from "./config";

const BaseInlineEditorNodeSpec = OrderedMap.from<NodeSpec>({
  doc: {
      content: "inline*",
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

export const basicSchemaWithLists = new Schema({
  nodes: addListNodes(schema.spec.nodes, "paragraph block*", "block"),
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

  const markSpec: MarkSpec = filterMarks(basicSchemaWithLists.spec.marks, allowedMarks);

  const schema = new Schema({
    nodes: filteredNodeSpec,
    marks: markSpec
  });

  return schema;
};
