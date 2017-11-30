import moment from 'moment';

function isBetween({ value, lower, upper }) {
  return value >= lower && value <= upper;
}

function getTimezoneOffset() {
  const offset = moment().utcOffset();

  if (isBetween({ value: offset, lower: 480, upper: 660 })) {
    return 'SYD';
  }

  if (isBetween({ value: offset, lower: -560, upper: -400 })) {
    return 'SFO';
  }

  if (isBetween({ value: offset, lower: -400, upper: -240 })) {
    return 'NYC';
  }

  return 'LON';
}

export default function getProductionOffice() {
  const timezoneProductionOfficeMap = {
    SYD: 'AU',
    SFO: 'US',
    NYC: 'US',
    LON: 'UK'
  };

  return timezoneProductionOfficeMap[getTimezoneOffset()];
}
