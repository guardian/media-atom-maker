#!/usr/bin/env node

const fs = require('fs/promises');
const parseHocon = require('hoconjs/build/hoconjs');
const HMACRequest = require('./hmac/hmac-request');

const configFile =
  process.env.ICONIK_CHECK_CONFIG_FILE ||
  '/etc/gu/media-atom-maker.private.conf';
const delaySeconds = Number(process.env.ICONIK_CHECK_DELAY_SECONDS || '1');
const retryLimit = Number(process.env.ICONIK_CHECK_RETRY_LIMIT || '5');
const retryableStatuses = new Set([429, 502, 503, 504]);

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function loadConfig() {
  const rawConfig = await fs.readFile(configFile, 'utf8');
  const config = parseHocon(rawConfig);
  if (!config.host || !config.secret) {
    throw new Error(`Missing host or secret in ${configFile}`);
  }
  return config;
}

async function main() {
  if (!Number.isFinite(delaySeconds) || delaySeconds < 0) {
    throw new Error('ICONIK_CHECK_DELAY_SECONDS must be a non-negative number');
  }
  if (!Number.isInteger(retryLimit) || retryLimit < 0) {
    throw new Error('ICONIK_CHECK_RETRY_LIMIT must be a non-negative integer');
  }

  const config = await loadConfig();
  const hmacRequest = new HMACRequest({secret: config.secret});
  const requestWithRetry = async path => {
    const url = `https://${config.host}/${path}`;
    for (let attempt = 0; ; attempt += 1) {
      try {
        return await hmacRequest.get(url);
      } catch (error) {
        if (!retryableStatuses.has(error.status) || attempt >= retryLimit) {
          throw error;
        }
        const backoffSeconds = 2 ** (attempt + 1);
        console.warn(
          `HTTP ${error.status}; retrying in ${backoffSeconds}s ` +
            `(attempt ${attempt + 1}/${retryLimit})`
        );
        await delay(backoffSeconds * 1000);
      }
    }
  };

  const entityIds = await requestWithRetry('api/iconik/validate-entities');
  if (!Array.isArray(entityIds)) {
    throw new Error('Expected an array of Iconik entity IDs');
  }

  for (const {entityType, id} of entityIds) {
    if (typeof entityType !== 'string' || typeof id !== 'string') {
      throw new Error('Iconik entity list contains an invalid entity');
    }

    console.error(`checking ${entityType} ${id}`);
    const result = await requestWithRetry(
      `api/iconik/validate/${encodeURIComponent(entityType)}/${encodeURIComponent(id)}`
    );
    if (!Array.isArray(result?.entities) || result.entities.length !== 1) {
      throw new Error(`Expected one validation result for ${entityType} ${id}`);
    }
    console.log(JSON.stringify(result));
    await delay(delaySeconds * 1000);
  }

  console.error(`Checked ${entityIds.length} Iconik objects.`);
}

main().catch(error => {
  console.error(error.message || error);
  process.exitCode = 1;
});
