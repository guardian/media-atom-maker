#! /usr/bin/env node
// @ts-check

/**
 * @typedef {import("../src/types").PlutoUpsertMessage} UpsertMessage
 * @typedef {import("../src/types").PlutoDeleteMessage} DeleteMessage
 */

import { getProject, invokeLambda } from './aws.js';

/**
 * @param {object} data
 * @returns
 */
function createKinesisMessageFor(data) {
  return {
    Records: [
      {
        kinesis: {
          data: Buffer.from(JSON.stringify(data)).toString('base64')
        }
      }
    ]
  };
}

const commissionId = `9999-test-pluto-lambda-CODE`;

/** @type {DeleteMessage} */
const deleteMessage = {
  type: 'project-created',
  commissionId,
  commissionTitle: '(DELETE)'
};

/** @type {UpsertMessage} */
const upsertMessage = {
  type: 'project-created',
  id: `0000${new Date().getUTCMilliseconds()}`,
  title: `Test Project ${new Date().toLocaleString()}`,
  status: 'active',
  commissionId,
  commissionTitle: 'Test Commission',
  productionOffice: 'Test Office',
  created: new Date().toISOString()
};

const plutoLambdaName = 'pluto-message-ingestion-CODE';
const plutoProjectTableName = 'media-atom-maker-CODE-pluto-projects-table';
const VERBOSE =
  process.argv.includes('--verbose') || process.argv.includes('-v');

console.log("Checking that the project doesn't already exist");
const maybeProject = await getProject(plutoProjectTableName, upsertMessage.id);
if (maybeProject) {
  throw new Error(
    `Project with id ${upsertMessage.id} already exists! Aborting test.`
  );
}
console.log('Project does not exist, proceeding with test.');

const { logs, result } = await invokeLambda(
  plutoLambdaName,
  createKinesisMessageFor(upsertMessage)
);

if (VERBOSE) {
  console.log('Lambda invocation logs:');
  console.log(logs);
  console.log('Lambda invocation result:');
  console.log(result);
}

// blocking wait for the lambda to process the message
await new Promise(resolve => setTimeout(resolve, 1000));

const item = await getProject(plutoProjectTableName, upsertMessage.id);
if (!item || !('title' in item)) {
  throw new Error('No item found, or unexpected item', item);
}
if (item.title === upsertMessage.title) {
  console.log('Item found, title matches:', item.title);
} else {
  throw new Error(
    `Item found but title does not match: expected "${upsertMessage.title}", got "${item.title}"`
  );
}

console.log('Upsert test passed, proceeding to delete test.');

const { logs: deleteLogs, result: deleteResult } = await invokeLambda(
  plutoLambdaName,
  createKinesisMessageFor(deleteMessage)
);

if (VERBOSE) {
  console.log('Lambda invocation logs:');
  console.log(deleteLogs);
  console.log('Lambda invocation result:');
  console.log(deleteResult);
}

// blocking wait for the lambda to process the message
await new Promise(resolve => setTimeout(resolve, 1000));

const maybeDeletedItem = await getProject(
  plutoProjectTableName,
  upsertMessage.id
);
if (maybeDeletedItem) {
  console.error('Item was not deleted, found:', maybeDeletedItem);
  throw new Error('Item was not deleted, test failed.');
}

console.log('Delete test passed, item was deleted successfully.');
console.log('All tests passed successfully.');
