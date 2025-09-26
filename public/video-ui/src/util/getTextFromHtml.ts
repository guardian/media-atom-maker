export const getTextFromHtml = (html: string) => {
  const string = html.replace(/<\/?[^>]+(>|$)/g, '');
  return string;
};
