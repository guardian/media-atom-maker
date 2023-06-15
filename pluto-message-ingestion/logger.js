const logForElk = (object, level) => {
  // eslint-disable-next-line no-console
  const logger = level === 'error' ? console.error : console.log;
  logger(JSON.stringify(object));
};

module.exports = logForElk;
