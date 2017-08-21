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

function detectProductionOffice() {
  const timezoneProductionOfficeMap = {
    'SYD': 'AUS',
    'SFO': 'US',
    'NYC': 'US',
    'LON': 'UK'
  };

  return timezoneProductionOfficeMap[getTimezoneOffset()];
}

export default function workflow(state = { sections: [], status: {} }, action) {
  switch (action.type) {
    case 'WORKFLOW_SECTIONS_GET_RECEIVE':
      return Object.assign({}, state, {
        sections: action.sections || []
      });
    case 'WORKFLOW_STATUS_GET_RECEIVE':
      return Object.assign({}, state, {
        status: action.status || {}
      });
    case 'WORKFLOW_STATUS_NOT_FOUND':
      return Object.assign({}, state, {
        status: action.status
      });
    case 'WORKFLOW_GET_PRODUCTION_OFFICE':
      return Object.assign({}, state, {
        productionOffice: detectProductionOffice()
      });
    default:
      return state;
  }
}
