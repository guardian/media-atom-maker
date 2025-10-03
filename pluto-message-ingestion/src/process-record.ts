import { getErrorMessage } from '@guardian/libs';
import type { KinesisStreamRecord } from 'aws-lambda';
import { createHmacClient } from './hmac-request';
import { createLogger, Logger } from './logging';
import {
  hasRecognisedMessageType,
  isDeleteMessage,
  isUpsertMessage
} from './types';

export async function processRecord(
  record: KinesisStreamRecord,
  secret: string,
  baseUrl: string
): Promise<'success' | 'failure'> {
  const logger = createLogger({ kinesisEventId: record.eventID });
  const payload = Buffer.from(record.kinesis.data, 'base64').toString('utf8');

  const { hmacPut, hmacDelete } = createHmacClient(logger, secret);

  const data = safeParseJson(payload, logger);

  if (data === undefined) {
    return 'failure'; // if the message is not valid there isn't any point in retrying
  }

  if (isDeleteMessage(data)) {
    const result = await hmacDelete({
      url: `${baseUrl}/api/pluto/commissions/${data.commissionId}`
    });
    if (!result.ok) {
      logger.error({
        message: `Error deleting commission ${data.commissionId}: ${result.status} ${result.statusText}`
      });
      throw new Error(
        `Error deleting commission ${data.commissionId}: ${result.status} ${result.statusText}`
      );
    }
  } else if (isUpsertMessage(data)) {
    const result = await hmacPut({
      url: `${baseUrl}/api/pluto/projects`,
      data
    });
    if (!result.ok) {
      logger.error({
        message: `Error upserting commission ${data.commissionId}: ${result.status} ${result.statusText}`
      });
      throw new Error(
        `Error upserting commission ${data.commissionId}: ${result.status} ${result.statusText}`
      );
    }
  } else if (hasRecognisedMessageType(data)) {
    logger.error({ message: `Message is missing required fields`, data });
    return 'failure';
  } else {
    const maybeMessageType =
      typeof data === 'object' && data !== null && 'type' in data
        ? data['type']
        : 'unknown';

    logger.error({
      message: `Message has unknown type ${maybeMessageType}`,
      data
    });

    return 'failure'; // if the message is not valid there isn't any point in retrying
  }
  return 'success';
}

function safeParseJson(json: string, logger: Logger): unknown | undefined {
  try {
    return JSON.parse(json);
  } catch (e) {
    logger.error({
      message: `Error parsing JSON: ${getErrorMessage(e)}`,
      payload: json
    });
    return undefined;
  }
}
