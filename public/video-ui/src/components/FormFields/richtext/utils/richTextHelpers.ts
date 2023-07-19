export const paragraphToWhitespace = (string: string): string => {
  // Only the first paragraph tag
  string = string.replace("<p>","");
  // Replace subsequent paragraph tags with hard breaks
  string = string.replace(/<p>/g,"<br>");
  // Remove all paragraph closing tags
  string = string.replace(/<\/p>/g,"");
  return string;
};

export const getWords = (text: string): string[] => {
  if (!text) {
    return [];
  }

  return text
    .trim()
    .replace(/<(?:.|\n)*?>/gm, '')
    .replace(/[^(\w| )]/g, '')
    .split(/\s+/)
    .filter(_ => _.length !== 0);
};

export const isTooLong = (value: string, maxWordLength: number): boolean => {
  const wordLength = getWords(value).reduce((length, word) => {
    length += word.length;
    return length;
  }, 0);
  return (
    wordLength > maxWordLength
  );
};
