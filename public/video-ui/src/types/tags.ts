export type DisplayTag = {
  id: string;
  webTitle: string;
};

export type CapiTagResponse = {
  response?: {
    tag?: DisplayTag;
  };
};

export type ParsedTag = string | DisplayTag;
