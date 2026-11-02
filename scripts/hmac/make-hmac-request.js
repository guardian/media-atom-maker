#!/usr/bin/env node

const fs = require('fs');

const ArgumentParser = require('argparse').ArgumentParser;
const parseHocon = require('hoconjs/build/hoconjs');
const chalk = require('chalk');

const HMACRequest = require('./hmac-request');

const parser = new ArgumentParser();

parser.addArgument('--path', {
  dest: 'path',
  required: true,
  help: 'path to make a request to'
});

parser.addArgument('--method', {
  dest: 'method',
  choices: ['get', 'post', 'put', 'delete'],
  defaultValue: 'get',
  help: 'HTTP method to use'
});

parser.addArgument('--data', {
  dest: 'data',
  help: 'data to send in request payload'
});

parser.addArgument('--data-file', {
  dest: 'dataFile',
  help: 'file path to read json from to send in request payload'
});

parser.addArgument('--config-file', {
  dest: 'configFile',
  defaultValue: '/etc/gu/media-atom-maker.private.conf',
  help: 'path to config file containing host and secret values'
});

const args = parser.parseArgs();

function getConfig() {
  return new Promise((resolve, reject) => {
    fs.readFile(args.configFile, 'utf8', (err, rawFile) => {
      if (err) {
        reject(err);
      } else {
        const config = parseHocon(rawFile);
        resolve(config);
      }
    })
  });
}

function getData() {
  return new Promise((resolve, reject) => {
    function tryParse(obj) {
      try {
        const jsonData = JSON.parse(obj);
        resolve(jsonData);
      }
      catch (e) {
        reject(`Failed to parse as JSON: ${obj}`);
      }
    }

    if (! args.data && ! args.dataFile) {
      // no data
      resolve();
    }

    if (args.data) {
      tryParse(args.data);
    }

    if (args.dataFile) {
      fs.readFile(args.dataFile, 'utf8', (err, rawData) => {
        if (err) {
          reject(err);
        }

        tryParse(rawData);
      });
    }
  });
}

Promise.all([
  getConfig(),
  getData()
]).then(results => {
  const config = results[0];
  const payloadData = results[1];

  const host = config.host;
  const secret = config.secret;
  const url = `https://${host}/${args.path}`;
  const method = args.method;

  const hmacRequest = new HMACRequest({secret: secret});

  hmacRequest[method](url, payloadData)
    .then(response => console.log(response))
    .catch(err => {
      console.log(chalk.red(`ERROR! ${err.status} (${err.statusText}) ${method.toUpperCase()} ${url}`));
    });
}).catch(err => {
  console.log(chalk.red(err));
});
