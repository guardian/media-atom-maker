#! /usr/bin/env node
// @ts-check

/**
 * @typedef {import("../src/types").UpsertMessage} UpsertMessage
 * @typedef {import("../src/types").DeleteMessage} DeleteMessage
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

/** @type {DeleteMessage} */
const deleteMessage = {
  type: 'project-created',
  commissionId: '0987654321',
  commissionTitle: '(DELETE)'
};

/** @type {UpsertMessage} */
const upsertMessage = {
  type: 'project-created',
  id: `0000${new Date().toString()}`,
  title: `Test Project ${new Date().toLocaleString()}`,
  status: 'active',
  commissionId: '0987654321',
  commissionTitle: 'Test Commission',
  productionOffice: 'Test Office',
  created: new Date().toISOString()
};

const plutoLambdaName = 'pluto-message-ingestion-CODE';
const plutoProjectTableName = 'media-atom-maker-CODE-pluto-projects-table';
const VERBOSE =
  process.argv.includes('--verbose') || process.argv.includes('-v');

invokeLambda(plutoLambdaName, createKinesisMessageFor(upsertMessage)).then(
  ({ logs, result }) => {
    if (VERBOSE) {
      console.log('Lambda invocation logs:');
      console.log(logs);
      console.log('Lambda invocation result:');
      console.log(result);
    }
  }
);

// wait a few seconds then
setTimeout(
  () =>
    getProject(plutoProjectTableName, upsertMessage.id).then(item => {
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
    }),
  2000
);
