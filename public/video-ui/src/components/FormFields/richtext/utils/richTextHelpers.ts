export const paragraphToWhitespace = (string: string) => {
  // Only the first paragraph tag
  string = string.replace("<p>","");
  // Replace subsequent paragraph tags with hard breaks
  string = string.replace(/<p>/g,"<br>");
  // Remove all paragraph closing tags
  string = string.replace(/<\/p>/g,"");
  return `${string}`;
};
