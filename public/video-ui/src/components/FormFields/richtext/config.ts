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
