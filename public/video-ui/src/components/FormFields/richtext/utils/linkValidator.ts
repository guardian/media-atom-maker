import { isUri } from 'valid-url';

const FUZZY_MATCHERS = [
  {
    // For emails we just look for a `@` symbol as it is easier.
    regexp: /@/,
    message:
      'The URL you entered appears to be an email address. ' +
      'Do you want to add the required “mailto:” prefix?',
    action: (link: string) => `mailto:${link}`
  },
  {
    // For tel numbers check for + and numerical values
    regexp: /^[+, 0-9]*$/,
    message:
      'The URL you entered appears to be a telephone number. ' +
      'Do you want to add the required “tel:” prefix?',
    action: (link: string) => `tel:${link}`
  },
  {
    regexp: /.+/,
    message:
      'The URL you entered appears to be a link. ' +
      'Do you want to add the required “https://” prefix?',
    action: (link: string) => `https://${link.trim()}`
  }
];

export const parseURL = (rawLink: string, confirm = window.confirm) => {
  try {
    const parsedUrl = new URL(rawLink);
    if (!parsedUrl.protocol) {
      throw false; // try fuzzy instead
    }

    return parsedUrl.href;
  } catch (e) {
    for (const { regexp, message, action } of FUZZY_MATCHERS) {
      if (regexp.test(rawLink)) {
        if (confirm(message)) {
          return action(rawLink);
        } else {
          return null;
        }
      }
    }

    return rawLink;
  }
};

const isEdToolsDomain = (rawLink: string) => {
  const edToolsDomains = [
    'composer.gutools.co.uk',
    'preview.gutools.co.uk',
    'viewer.gutools.co.uk'
  ];

  return (
    edToolsDomains.some(domain => rawLink.includes(domain))
  );
};

export const linkValidator = (rawLink: string) => {
  if (!rawLink){
    return {valid: false, message: "Empty URL provided", link: rawLink};
  }
  if (isEdToolsDomain(rawLink)) {
    return {
      valid: false,
      message:
        'This is a preview link, which can only be accessed by Guardian staff, not readers.',
      link: rawLink
    };
  }

  if (rawLink === 'dashboard.ophan.co.uk') {
    return {
      valid: false,
      message:
        'This link can only be accessed by Guardian staff, not readers.',
      link: rawLink
    };
  }
  if (isUri(rawLink)){
    return {valid: true};
  } else {
    if (isUri(`https://${rawLink}`)) return {valid: false, message: `A valid URL should start with 'https://' or 'http://'.`, link: rawLink};
    return {valid: false, message: `The link you entered was invalid.`, link: rawLink};
  }
};
