#!/usr/bin/env node

// Example usage: ./make-hmac-request.js --method post --path api2/atoms --data-file $PWD/new-atom.json

const fs = require('fs');

const ArgumentParser = require('argparse').ArgumentParser;
const PropertiesReader = require('properties-reader');
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
  defaultValue: {},
  help: 'data to send in request payload'
});

parser.addArgument('--data-file', {
  dest: 'dataFile',
  defaultValue: false,
  help: 'file path to read json from to send in request payload'
});

parser.addArgument('--config-file', {
  dest: 'configFile',
  defaultValue: '/etc/gu/media-atom-maker.private.conf',
  help: 'path to config file containing host and secret values'
});

const args = parser.parseArgs();

const configuration = new PropertiesReader(args.configFile).getAllProperties();

const host = configuration.host.replace(/"/g, '');
const secret = configuration.secret.replace(/"/g, '');
const url = `https://${host}/${args.path}`;
const method = args.method;

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

    if (! args.dataFile) {
      tryParse(args.data);
    }

    fs.readFile(args.dataFile, 'utf8', (err, rawData) => {
      if (err) {
        reject(err);
      }

      tryParse(rawData);
    });
  });
}


getData().then(data => {
  const hmacRequest = new HMACRequest({secret: secret});

  hmacRequest[method](url, data)
    .then(response => console.log(response))
    .catch(err => {
      console.log(chalk.red(`ERROR! ${err.status} (${err.statusText}) ${method.toUpperCase()} ${url}`));
    });
}).catch(err => {
  console.log(chalk.red(err));
});
