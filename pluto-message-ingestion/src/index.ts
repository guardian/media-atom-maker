import type { KinesisStreamEvent } from 'aws-lambda';
import { hmacSecretName, hostSecretName } from './config';
import { processRecord } from './process-record';
import { readSecretValue } from './secrets-manager';

export async function handler(event: KinesisStreamEvent) {
  const host = await readSecretValue(hostSecretName);
  const hmacSecret = await readSecretValue(hmacSecretName);

  if (!host) {
    throw new Error('Host secret is empty');
  }
  if (!hmacSecret) {
    throw new Error('HMAC secret is empty');
  }

  const baseUrl = `https://${host}`;

  await Promise.all(
    event.Records.map(record => processRecord(record, hmacSecret, baseUrl))
  );
}
