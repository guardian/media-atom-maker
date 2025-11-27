import { apiRequest } from './apiRequest';

export default {
  composerTagToYouTube: (tagId: string) => {
    const encodedId = encodeURIComponent(tagId);
    return apiRequest<string[]>({
      url: '/api/youtube/content-bundle/' + encodedId,
      method: 'get'
    });
  }
};

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
    // to the existing tags
    for (const keyword of keywords) {
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
