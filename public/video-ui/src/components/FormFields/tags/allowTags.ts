import TagTypes from "../../../constants/TagTypes";
import type { StandTagPickerFilter, VideoTag } from "../StandTagPicker";

export const isTagAllowed = (tag: VideoTag): boolean => {
  if (tag.type === 'Keyword') {
    return true;
  }
  if (tag.type === 'Tracking' && tag.subtype === 'video') {
    return true;
  }
  const allowedTags = ['podcasts', 'tone/news', 'tone/features'];
  return allowedTags.includes(tag.path);
};

export const supportedTagFilters: StandTagPickerFilter[] = [
  {
    displayLabel: 'All',
    tagTypes: [TagTypes.keyword, TagTypes.tracking, TagTypes.tone, TagTypes.contentType]
  },
  {
    displayLabel: 'Keywords',
    tagTypes: [TagTypes.keyword]
  },
  {
    displayLabel: 'Tracking - video',
    tagTypes: [TagTypes.tracking],
    tagSubType: 'video'
  },
  {
    displayLabel: 'Tone',
    tagTypes: [TagTypes.tone]
  },
  {
    displayLabel: 'Content type',
    tagTypes: [TagTypes.contentType]
  }
];
