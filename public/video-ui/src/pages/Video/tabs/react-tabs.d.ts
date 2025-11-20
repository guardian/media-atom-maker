/**
 * I can't find any type definitions for react-tabs, so declare the module here.
 * It's in use in lots of JS files already, so I don't feel too worried about
 * lacking the actual types for the single TS file that uses it at the moment.
 *
 * Longer term, we should probably migrate away from react-tabs anyway, as it's
 * not well maintained.
 */
declare module 'react-tabs';
