import moment from 'moment';

export const isFutureDate = date => date && moment(date).isAfter(moment());

export const isSameOrAfter = (dateA, dateB) =>
  moment(dateA).isSameOrAfter(moment(dateB));

export const isAfter = (dateA, dateB) => moment(dateA).isAfter(moment(dateB));
