export type DisplayTag = {
  id: string;
  webTitle: string;
};

export type CapiTagResponse = {
  response?: {
    tag?: DisplayTag;
  };
};

export type CapiTagNotFoundResponse = {
  response?: {
    status?: string;
    message?: string;
  };
};

export type ParsedTag = string | DisplayTag;
