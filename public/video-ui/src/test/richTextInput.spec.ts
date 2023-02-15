import { paragraphToWhitespace } from "../components/FormFields/richtext/utils/richTextHelpers";

describe('paragraphToWhitespace', () => {
  it('converts opening paragraphs to line breaks, ignoring the first paragraph tag, and removing closing tags', () => {
    const htmlString = "<p>Hello,</p><p>world</p>";

    const result = paragraphToWhitespace(htmlString);

    expect(result).toBe("Hello,<br>world");
  });
});
