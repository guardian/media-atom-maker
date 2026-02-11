const contentBundlingMap: Record<string, string> = {
  "uk": "gdnpfpnewsuk",
  "us": "gdnpfpnewsus",
  "au": "gdnpfpnewsau",
  "world": "gdnpfpnewsworld",
  "politics": "gdnpfpnewspolitics",
  "opinion": "gdnpfpnewsopinion",
  "football": "gdnpfpsportfootball",
  "cricket": "gdnpfpsportcricket",
  "rugby-union": "gdnpfpsportrugbyunion",
  "rugby-league": "gdnpfpsportrugbyleague",
  "f1": "gdnpfpsportf1",
  "tennis": "gdnpfpsporttennis",
  "golf": "gdnpfpsportgolf",
  "cycling": "gdnpfpsportcycling",
  "boxing": "gdnpfpsportboxing",
  "racing": "gdnpfpsportracing",
  "us-sport": "gdnpfpsportus",
  "sport": "gdnpfpsportother",
  "culture": "gdnpfpculture",
  "film": "gdnpfpculturefilm",
  "music": "gdnpfpculturemusic",
  "lifestyle": "gdnpfplifestyle",
  "food": "gdnpfplifestylefood",
  "health-and-wellbeing": "gdnpfplifestylehealthfitness",
  "business": "gdnpfpbusiness",
  "money": "gdnpfpmoney",
  "fashion": "gdnpfpfashion",
  "environment": "gdnpfpenvironment",
  "technology": "gdnpfptechnology",
  "travel": "gdnpfptravel",
  "science": "gdnpfpscience",
  "athletics": "gdnpfpsportother",
  "basketball": "gdnpfpsportus",
  "sport-2-0": "gdnpfpsport20",
  "full-story-podcast": "gdnpfpausfullstorypodcast"
};

const contentBundlingTags = new Set(Object.values(contentBundlingMap));

export const addOrDropBundlingTags = (keywords: string[], tags: string[], blockAds: boolean) => {
  // strip all gdnpfp... tags. If the composer keyword which caused them to be added
  // is still around, they'll come back, but if it's been removed or blockAds has been
  // turned on, then they'll be stripped out.
  const tagsWithoutBundlingTags = tags.filter(tag => !contentBundlingTags.has(tag));
  const tagSet = new Set(tagsWithoutBundlingTags);

  if (blockAds) {
    return [...tagSet.values()];
  } else {
    // if block ads is off, then look up all composer tags (called "keywords" here), and add any matching content bundling tag
    // to the existing tags.
    for (const keyword of keywords) {
      // Try to add the most specific tag which matches the list above.
      // For example, say the `sport/cycling` Composer tag had been added to the video. Both `sport` and `cycling` are in
      // the list of bundle tags above, but `cycling` (the second) is the more specific so we choose to add
      // `gdnpfpsportcycling` (and `cycling`) to the list of youtube tags. Alternatively we might have added the
      // `politics/education` Composer tag - `education` isn't a recognised bundle tag, so we fall back to the first
      // element `politics` instead and add `gdnpfpnewspolitics`.
      const parts = keyword.split('/');
      const matchingBundlingTag = parts.reverse().find(part => contentBundlingMap[part]);
      if (matchingBundlingTag) {
        tagSet.add(matchingBundlingTag);
        tagSet.add(contentBundlingMap[matchingBundlingTag]);
      }
    }
    return [...tagSet.values()];
  }
};
