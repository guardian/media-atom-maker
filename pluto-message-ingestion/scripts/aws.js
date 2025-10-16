// @ts-check

import { DynamoDBClient } from '@aws-sdk/client-dynamodb';
import { InvokeCommand, LambdaClient, LogType } from '@aws-sdk/client-lambda';
import { fromNodeProviderChain } from '@aws-sdk/credential-providers';
import { DynamoDBDocumentClient, GetCommand } from '@aws-sdk/lib-dynamodb';

const config = {
  region: 'eu-west-1',
  credentials: fromNodeProviderChain({
    profile: 'media-service'
  })
};

/**
 *
 * @param {string} funcName
 * @param {object} payload
 * @returns
 */
export const invokeLambda = async (funcName, payload) => {
  const client = new LambdaClient(config);
  const command = new InvokeCommand({
    FunctionName: funcName,
    Payload: JSON.stringify(payload),
    LogType: LogType.Tail
  });

  const { Payload, LogResult } = await client.send(command);
  const result = Payload ? Buffer.from(Payload).toString() : undefined;
  const logs = LogResult
    ? Buffer.from(LogResult, 'base64').toString()
    : undefined;
  return { logs, result };
};

const dynamoClient = new DynamoDBClient(config);

const docClient = DynamoDBDocumentClient.from(dynamoClient);

/**
 *
 * @param {string} tableName
 * @param {string} id
 * @return {Promise<Record<string, any> | undefined>}
 */
export async function getProject(tableName, id) {
  const params = {
    TableName: tableName,
    Key: {
      id
    }
  };

  try {
    const data = await docClient.send(new GetCommand(params));
    console.log('result : ' + JSON.stringify(data));
    return data.Item;
  } catch (error) {
    console.error('Error:', error);
  }
}

/**
 * @param {object} data
 * @returns
 */
export function createKinesisMessageFor(data) {
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
