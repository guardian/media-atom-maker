import { getWords, isTooLong, paragraphToWhitespace } from "../components/FormFields/richtext/utils/richTextHelpers";

describe('paragraphToWhitespace', () => {
  it('Converts opening paragraphs to line breaks, ignoring the first paragraph tag, and removing closing tags', () => {
    const htmlString = "<p>Hello,</p><p>world</p>";

    const result = paragraphToWhitespace(htmlString);

    expect(result).toBe("Hello,<br>world");
  });
});

describe("getWords", () => {
  it('Returns the words in an html string', () => {
    const html = "<p>Hello world, or rather, good morning.</p>";

    const words = getWords(html);

    expect(words).toStrictEqual(["Hello","world","or","rather","good","morning"]);
  });
});

describe("isTooLong", () => {
  it('Should return false for phrases less than the character and word limit', () => {
    const tooLongString = "So it goes";
    const maxWords = 100;
    const maxChars = 10;

    const isTooLongResult = isTooLong(tooLongString, maxWords, maxChars);

    expect(isTooLongResult).toBe(false);
  });

  it('Should return true for phrases over the character limit', () => {
    const stringWithTooManyCharacters = "abcdefghijklmnop";
    const maxWords = 100;
    const maxChars = 10;

    const isTooLongResult = isTooLong(stringWithTooManyCharacters, maxWords, maxChars);

    expect(isTooLongResult).toBe(true);
  });

  it('Should return true for phrases over the word limit', () => {
    const stringWithTooManyWords = " All statements are true in some sense, false in some sense, meaningless in some sense, true and false in some sense, true and meaningless in some sense, false and meaningless in some sense, and true and false and meaningless in some sense.";
    const maxWords = 5;
    const maxChars = 10000;

    const isTooLongResult = isTooLong(stringWithTooManyWords, maxWords, maxChars);

    expect(isTooLongResult).toBe(true);
  });
});
