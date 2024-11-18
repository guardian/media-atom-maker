import moment, { MomentInput } from 'moment';

export const isFutureDate = (date: MomentInput) => date && moment(date).isAfter(moment());

export const isSameOrAfter = (dateA: MomentInput, dateB: MomentInput) =>
  moment(dateA).isSameOrAfter(moment(dateB));

export const isAfter = (dateA: MomentInput, dateB: MomentInput) => moment(dateA).isAfter(moment(dateB));
