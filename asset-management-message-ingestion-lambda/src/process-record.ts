import { KinesisStreamRecord } from 'aws-lambda';
import { hmacDelete, hmacPut } from './hmac-request';

type UpsertMessage = {
  type: 'project-created' | 'project-updated';
  id: string;
  title: string;
  status: string;
  commissionId: string;
  commissionTitle: string;
  productionOffice: string;
  created: string;
};

function isUpsertMessage(data: any): data is UpsertMessage {
  return (
    data &&
    (data.type === 'project-created' || data.type === 'project-updated') &&
    typeof data.id === 'string' &&
    typeof data.title === 'string' &&
    typeof data.status === 'string' &&
    typeof data.commissionId === 'string' &&
    typeof data.commissionTitle === 'string' &&
    typeof data.productionOffice === 'string' &&
    typeof data.created === 'string'
  );
}

type DeleteMessage = {
  type: 'project-created' | 'project-updated';
  commissionId: string;
  commissionTitle: '(DELETE)';
};

function isDeleteMessage(data: any): data is DeleteMessage {
  return (
    data &&
    (data.type === 'project-created' || data.type === 'project-updated') &&
    typeof data.commissionId === 'string' &&
    data.commissionTitle === '(DELETE)'
  );
}

export async function processRecord(
  record: KinesisStreamRecord,
  secret: string,
  baseUrl: string
): Promise<'success' | 'failure'> {
  const payload = Buffer.from(record.kinesis.data, 'base64').toString('utf8');

  const data = safeParseJson(payload);

  if (data === undefined) {
    return 'failure'; // if the message is not valid there isn't any point in retrying
  }

  if (isDeleteMessage(data)) {
    const result = await hmacDelete({
      url: `${baseUrl}/api/pluto/commissions/${data.commissionId}`,
      secret
    });
    if (!result.ok) {
      console.error(
        `Error deleting commission ${data.commissionId}: ${result.status} ${result.statusText}`
      );
      throw new Error(
        `Error deleting commission ${data.commissionId}: ${result.status} ${result.statusText}`
      );
    }
  } else if (isUpsertMessage(data)) {
    const result = await hmacPut({
      url: `${baseUrl}/api/pluto/commissions/${data.commissionId}`,
      secret,
      data
    });
    if (!result.ok) {
      console.error(
        `Error deleting commission ${data.commissionId}: ${result.status} ${result.statusText}`
      );
      throw new Error(
        `Error deleting commission ${data.commissionId}: ${result.status} ${result.statusText}`
      );
    }
  } else {
    console.error('Unknown message type', data);
    return 'failure'; // if the message is not valid there isn't any point in retrying
  }
  return 'success';
}

function safeParseJson(json: string): unknown | undefined {
  try {
    return JSON.parse(json);
  } catch {
    console.error('Error parsing JSON:', json);
    return undefined;
  }
}
