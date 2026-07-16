import { DisplayTag } from '../types/tags';

type ExistingTagValue = string | { id: string };

export function removeStringTagDuplicates(
  tag: DisplayTag,
  tagValue: ExistingTagValue[]
): ExistingTagValue[] {
  // Remove string input that appears in the last words of the tag input that is being added
  // If a word Mary has already been added and a user adds a tag with title `Mary Smith`, `Mary`
  // gets removed from the input. Instead of the byline reading `Mary Mary Smith`, it only
  // reads `Mary Smith`.

  const tagWords = tag.webTitle
    .split(' ')
    .map((word: string) => word.toLowerCase())
    .reverse();

  const addedValues = tagValue.slice(0).map((word: ExistingTagValue) => {
    if (typeof word === 'string') {
      return word.toLowerCase();
    }

    return word;
  });

  const valuesLength = addedValues.length;
  let numberOfMatches = 0;

  const firstTagMatch = tagWords.findIndex((tagWord: string) => {
    return addedValues[valuesLength - 1] === tagWord;
  });

  if (firstTagMatch !== -1) {
    numberOfMatches = 1;
    for (let i = firstTagMatch + 1; i < tagWords.length; i++) {
      if (tagWords[i] === addedValues[valuesLength - 1 - numberOfMatches]) {
        numberOfMatches++;
      } else {
        break;
      }
    }
  }

  const slicedTagValue = tagValue.slice(0, tagValue.length - numberOfMatches);

  return slicedTagValue;
}
