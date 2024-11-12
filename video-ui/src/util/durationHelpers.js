function padNumber(num, minLength = 2) {
  const str = `${num}`;
  const leadingZeros = minLength - str.length;
  if (leadingZeros < 1) {
    return str;
  }
  return `${'0'.repeat(leadingZeros)}${str}`;
}

function durationToMinAndSecs(num) {
  return {
    mins: Math.floor(parseInt(num || '0', 10) / 60),
    secs: num % 60
  };
}

function secondsToDurationStr(dur) {
  const { mins, secs } = durationToMinAndSecs(dur);
  return `${mins}:${padNumber(secs, 2)}`;
}

export { secondsToDurationStr, durationToMinAndSecs };
