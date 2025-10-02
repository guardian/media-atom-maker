import { DynamoDBClient } from '@aws-sdk/client-dynamodb';
import { DynamoDBDocumentClient, GetCommand } from '@aws-sdk/lib-dynamodb';

const client = new DynamoDBClient({});

const docClient = DynamoDBDocumentClient.from(client);

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
