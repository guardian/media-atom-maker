import { KinesisStreamRecord } from 'aws-lambda';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { hmacDelete, hmacPut } from './hmac-request';
import { processRecord } from './process-record';

// Mock the entire module
vi.mock('./hmac-request');

describe('processRecord', () => {
  beforeEach(() => {
    // Clear mock history between tests
    vi.clearAllMocks();
  });

  it('should fail quietly if it receives invalid JSON', async () => {
    const record = {
      kinesis: {
        data: Buffer.from('not json').toString('base64')
      }
    } as unknown as KinesisStreamRecord;
    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(result).toBe('failure');
  });

  it('should send a delete request for delete messages', async () => {
    vi.mocked(hmacDelete).mockResolvedValue(new Response('OK'));

    const record = {
      kinesis: {
        data: Buffer.from(
          JSON.stringify({
            type: 'project-created',
            commissionId: '123',
            commissionTitle: '(DELETE)'
          })
        ).toString('base64')
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(hmacDelete).toHaveBeenCalledWith({
      url: 'https://example.com/api/pluto/commissions/123',
      secret: 'secret'
    });
    expect(result).toBe('success');
  });

  it('should send an upsert request for upsert messages', async () => {
    vi.mocked(hmacPut).mockResolvedValue(new Response('OK'));
    const record = {
      kinesis: {
        data: Buffer.from(
          JSON.stringify({
            type: 'project-created',
            id: 'proj-1',
            title: 'Project 1',
            status: 'active',
            commissionId: 'comm-1',
            commissionTitle: 'Commission 1',
            productionOffice: 'Office 1',
            created: '2023-10-01T00:00:00Z'
          })
        ).toString('base64')
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(hmacPut).toHaveBeenCalledWith({
      url: 'https://example.com/api/pluto/commissions/comm-1',
      secret: 'secret',
      data: {
        type: 'project-created',
        id: 'proj-1',
        title: 'Project 1',
        status: 'active',
        commissionId: 'comm-1',
        commissionTitle: 'Commission 1',
        productionOffice: 'Office 1',
        created: '2023-10-01T00:00:00Z'
      }
    });
    expect(result).toBe('success');
  });
});
