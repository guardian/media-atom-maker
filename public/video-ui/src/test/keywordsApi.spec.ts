import { addOrDropBundlingTags } from "../services/KeywordsApi";

describe('keywordsApi.addOrDropBundlingTags', () => {

  it('if no keywords or tags in, then nothing out', () => {
    expect(addOrDropBundlingTags([], [], false)).toEqual([]);
  });

  it('if a keyword matches one of the bundling specs, then add it and the bundle tag to output tags', () => {
    const keywords = ['world/africa'];
    expect(addOrDropBundlingTags(keywords, [], false)).toEqual(['world', 'gdnpfpnewsworld']);
  });

  it('if the second element of the keyword matches one of the bundling specs, add it and the bundle tag to the output tags', () => {
    const keywords = ['africa/world']; // note reversed from usual "world/africa"
    expect(addOrDropBundlingTags(keywords, [], false)).toEqual(['world', 'gdnpfpnewsworld']);
  });

  it('if both elements of the keyword match the bundling specs, only add the second (more specific) as bundling tag', () => {
    const keywords = ['sport/cycling'];
    expect(addOrDropBundlingTags(keywords, [], false)).toEqual(['cycling', 'gdnpfpsportcycling']);
  });

  it('if a keyword matches one of the bundling specs but the matching tag already exists, don\'t duplicate', () => {
    const keywords = ['world/africa'];
    const tags = ['world', 'gdnpfpnewsworld'];

    expect(addOrDropBundlingTags(keywords, tags, false)).toEqual(tags);
  });

  it('if a bundling tag exists but the matching keyword has been removed, remove it', () => {
    const keywords: string[] = [];
    const tags = ['world', 'gdnpfpnewsworld'];

    expect(addOrDropBundlingTags(keywords, tags, false)).toEqual(['world']);
  });

  it('if multiple keywords match a certain bundling spec, still only add one', () => {
    const keywords = ['world/africa', 'world/asia'];
    const tags: string[] = [];

    expect(addOrDropBundlingTags(keywords, tags, false)).toEqual(['world', 'gdnpfpnewsworld']);
  });

  it('if blockAds is on, do not add bundling tags', () => {
    const keywords = ['world/africa'];
    const tags: string[] = [];

    expect(addOrDropBundlingTags(keywords, tags, true)).toEqual([]);
  });

  it('if blockads is on and a bundling tag exists, remove it', () => {
    const keywords = ['world/africa'];
    const tags = ['world', 'gdnpfpnewsworld'];

    expect(addOrDropBundlingTags(keywords, tags, true)).toEqual(['world']);
  });

});
