export function getTextFromHtml(html) {

  const string = html.replace(/<\/?[^>]+(>|$)/g, "");
  return string;

}

