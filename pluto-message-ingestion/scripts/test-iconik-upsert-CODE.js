#! /usr/bin/env node
// @ts-check

/**
 * @typedef {import('../src/types').IconikUpsertMessage} IconikUpsertMessage
 */

import { createKinesisMessageFor, getProject, invokeLambda } from './aws.js';

/** @type {IconikUpsertMessage} */
const upsertMessage = {
  type: 'iconik-project-created',
  id: `test-project-1-${new Date().getUTCMilliseconds()}`,
  title: 'Test Project 1',
  commissionId: 'commission-1',
  commissionTitle: 'Commission 1',
  workingGroupId: 'working-group-1',
  workingGroupTitle: 'Working Group 1',
  status: 'TEST_STATUS'
};

const plutoLambdaName = 'pluto-message-ingestion-CODE';
const iconikProjectTableName = 'media-atom-maker-CODE-iconik-projects-table';

const VERBOSE =
  process.argv.includes('--verbose') || process.argv.includes('-v');

console.log("Checking that the project doesn't already exist");
const maybeProject = await getProject(iconikProjectTableName, upsertMessage.id);
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

const item = await getProject(iconikProjectTableName, upsertMessage.id);
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
