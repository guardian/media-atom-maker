import type { KinesisStreamRecord } from 'aws-lambda';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createHmacClient } from './hmac-request';
import { processRecord } from './process-record';

// Mock the entire module
vi.mock('./hmac-request');

// Create mock functions
const mockHmacPut = vi.fn();
const mockHmacDelete = vi.fn();

// Mock the factory function to return our mock functions
const mockCreateHmacClient = vi.mocked(createHmacClient);
mockCreateHmacClient.mockReturnValue({
  hmacPut: mockHmacPut,
  hmacDelete: mockHmacDelete
});

describe('processRecord', () => {
  beforeEach(() => {
    // Clear mock history between tests
    vi.clearAllMocks();
    mockHmacPut.mockReset();
    mockHmacDelete.mockReset();
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
    mockHmacDelete.mockResolvedValue(new Response('OK'));

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
    expect(mockHmacDelete).toHaveBeenCalledWith({
      url: 'https://example.com/api/pluto/commissions/123'
    });
    expect(result).toBe('success');
  });

  it('should send an upsert request for upsert messages', async () => {
    mockHmacPut.mockResolvedValue(new Response('OK'));
    const data = {
      type: 'project-created',
      id: 'proj-1',
      title: 'Project 1',
      status: 'active',
      commissionId: 'comm-1',
      commissionTitle: 'Commission 1',
      productionOffice: 'Office 1',
      created: '2023-10-01T00:00:00Z'
    };

    const record = {
      kinesis: {
        data: Buffer.from(JSON.stringify(data)).toString('base64')
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(mockHmacPut).toHaveBeenCalledWith({
      url: 'https://example.com/api/pluto/projects',
      data
    });
    expect(result).toBe('success');
  });

  it('should send an upsert request for project-updated messages', async () => {
    mockHmacPut.mockResolvedValue(new Response('OK'));
    const data = {
      type: 'project-updated',
      id: 'proj-2',
      title: 'Updated Project',
      status: 'completed',
      commissionId: 'comm-2',
      commissionTitle: 'Updated Commission',
      productionOffice: 'Office 2',
      created: '2023-10-02T00:00:00Z'
    };

    const record = {
      kinesis: {
        data: Buffer.from(JSON.stringify(data)).toString('base64')
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(mockHmacPut).toHaveBeenCalledWith({
      url: 'https://example.com/api/pluto/projects',
      data
    });
    expect(result).toBe('success');
  });

  it('should send an iconik upsert request for iconik-project-created messages', async () => {
    mockHmacPut.mockResolvedValue(new Response('OK'));
    const data = {
      type: 'iconik-project-created',
      id: 'iconik-proj-1',
      title: 'Iconik Project',
      status: 'active',
      commissionId: 'iconik-comm-1',
      commissionTitle: 'Iconik Commission',
      workingGroupId: 'wg-1',
      workingGroupTitle: 'Working Group 1',
      masterPlaceholderId: 'mp-1'
    };

    const record = {
      kinesis: {
        data: Buffer.from(JSON.stringify(data)).toString('base64')
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(mockHmacPut).toHaveBeenCalledWith({
      url: 'https://example.com/api/iconik/projects',
      data
    });
    expect(result).toBe('success');
  });

  it('should throw an error when delete request fails', async () => {
    mockHmacDelete.mockResolvedValue(
      new Response('Internal Server Error', {
        status: 500,
        statusText: 'Internal Server Error'
      })
    );

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

    await expect(
      processRecord(record, 'secret', 'https://example.com')
    ).rejects.toThrow();
  });

  it('should throw an error when upsert request fails', async () => {
    mockHmacPut.mockResolvedValue(
      new Response('Bad Request', { status: 400, statusText: 'Bad Request' })
    );

    const data = {
      type: 'project-created',
      id: 'proj-1',
      title: 'Project 1',
      status: 'active',
      commissionId: 'comm-1',
      commissionTitle: 'Commission 1',
      productionOffice: 'Office 1',
      created: '2023-10-01T00:00:00Z'
    };

    const record = {
      kinesis: {
        data: Buffer.from(JSON.stringify(data)).toString('base64')
      }
    } as unknown as KinesisStreamRecord;

    await expect(
      processRecord(record, 'secret', 'https://example.com')
    ).rejects.toThrow();
  });

  it('should fail quietly for messages with missing required fields', async () => {
    const incompleteUpsertMessage = {
      type: 'project-created',
      id: 'proj-1'
      // missing title, status, commissionId, etc.
    };

    const record = {
      kinesis: {
        data: Buffer.from(JSON.stringify(incompleteUpsertMessage)).toString(
          'base64'
        )
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(result).toBe('failure');
  });

  it('should fail quietly for delete messages with missing commissionId', async () => {
    const incompleteDeleteMessage = {
      type: 'project-created',
      commissionTitle: '(DELETE)'
      // missing commissionId
    };

    const record = {
      kinesis: {
        data: Buffer.from(JSON.stringify(incompleteDeleteMessage)).toString(
          'base64'
        )
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(result).toBe('failure');
  });

  it('should fail quietly for messages with unknown type', async () => {
    const unknownTypeMessage = {
      type: 'unknown-type',
      id: 'proj-1',
      title: 'Project 1'
    };

    const record = {
      kinesis: {
        data: Buffer.from(JSON.stringify(unknownTypeMessage)).toString('base64')
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(result).toBe('failure');
  });

  it('should fail quietly for valid JSON with wrong structure', async () => {
    const wrongStructureMessage = {
      someField: 'value',
      anotherField: 123
    };

    const record = {
      kinesis: {
        data: Buffer.from(JSON.stringify(wrongStructureMessage)).toString(
          'base64'
        )
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(result).toBe('failure');
  });

  it('should fail quietly for null/undefined messages', async () => {
    const record = {
      kinesis: {
        data: Buffer.from(JSON.stringify(null)).toString('base64')
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(result).toBe('failure');
  });

  it('should handle project-updated type for delete messages', async () => {
    mockHmacDelete.mockResolvedValue(new Response('OK'));

    const record = {
      kinesis: {
        data: Buffer.from(
          JSON.stringify({
            type: 'project-updated',
            commissionId: '456',
            commissionTitle: '(DELETE)'
          })
        ).toString('base64')
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(mockHmacDelete).toHaveBeenCalledWith({
      url: 'https://example.com/api/pluto/commissions/456'
    });
    expect(result).toBe('success');
  });

  it('should fail quietly for upsert messages with wrong field types', async () => {
    const wrongTypesMessage = {
      type: 'project-created',
      id: 123, // should be string
      title: 'Project 1',
      status: 'active',
      commissionId: 'comm-1',
      commissionTitle: 'Commission 1',
      productionOffice: 'Office 1',
      created: '2023-10-01T00:00:00Z'
    };

    const record = {
      kinesis: {
        data: Buffer.from(JSON.stringify(wrongTypesMessage)).toString('base64')
      }
    } as unknown as KinesisStreamRecord;

    const result = await processRecord(record, 'secret', 'https://example.com');
    expect(result).toBe('failure');
  });
});
