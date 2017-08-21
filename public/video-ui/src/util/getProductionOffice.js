import moment from 'moment';

function getTimezoneOffset() {
  const offset = moment().utcOffset();

  if (offset <= -480 && offset >= -660) {
    return 'SYD';
  } else if (offset <= 560 && offset >= 400) {
    return 'SFO';
  } else if (offset < 400 && offset >= 240) {
    return 'NYC';
  } else {
    return 'LON';
  }
}

export default function getProductionOffice() {
  const timezoneProductionOfficeMap = {
    'SYD': 'AUS',
    'SFO': 'US',
    'NYC': 'US',
    'LON': 'UK'
  };

  return timezoneProductionOfficeMap[getTimezoneOffset()];
}
