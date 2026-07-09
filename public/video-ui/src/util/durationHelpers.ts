type DurationInMinsAndSecondsObject = {
  mins: number;
  secs: number;
};

function padNumber(num: number, minLength: number = 2): string {
  const str = `${num}`;
  const leadingZeros = minLength - str.length;
  if (leadingZeros < 1) {
    return str;
  }
  return `${'0'.repeat(leadingZeros)}${str}`;
}

function durationToMinAndSecs(num: string): DurationInMinsAndSecondsObject {
  // During refactor we were unsure if this might sometimes come through as a string
  const definitelyANumber = Math.floor(parseInt(num || '0', 10));
  return {
    mins: definetlyANumber / 60,
    secs: definetlyANumber % 60
  };
}

function secondsToDurationStr(dur: string): string {
  const { mins, secs } = durationToMinAndSecs(dur);
  return `${mins}:${padNumber(secs, 2)}`;
}

export { secondsToDurationStr, durationToMinAndSecs };
