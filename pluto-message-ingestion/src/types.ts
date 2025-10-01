const recognisedMessageTypes = ['project-created', 'project-updated'];
type MessageType = (typeof recognisedMessageTypes)[number];

export type UpsertMessage = {
  type: MessageType;
  id: string;
  title: string;
  status: string;
  commissionId: string;
  commissionTitle: string;
  productionOffice: string;
  created: string;
};

export function isRecognisedMessageType(type: unknown): type is MessageType {
  return recognisedMessageTypes.includes(type as MessageType);
}

export function hasRecognisedMessageType(data: unknown): boolean {
  if (!data || typeof data !== 'object' || data === null) {
    return false;
  }
  const { type } = data as { [key: string]: unknown };
  return isRecognisedMessageType(type);
}

export function isUpsertMessage(data: unknown): data is UpsertMessage {
  if (!data || typeof data !== 'object' || data === null) {
    return false;
  }
  const {
    type,
    id,
    title,
    status,
    commissionId,
    commissionTitle,
    productionOffice,
    created
  } = data as {
    [key: string]: unknown;
  };
  return (
    isRecognisedMessageType(type) &&
    typeof id === 'string' &&
    typeof title === 'string' &&
    typeof status === 'string' &&
    typeof commissionId === 'string' &&
    typeof commissionTitle === 'string' &&
    typeof productionOffice === 'string' &&
    typeof created === 'string'
  );
}

export type DeleteMessage = {
  type: MessageType;
  commissionId: string;
  commissionTitle: '(DELETE)';
};

export function isDeleteMessage(data: unknown): data is DeleteMessage {
  if (!data || typeof data !== 'object' || data === null) {
    return false;
  }
  const { commissionId, commissionTitle, type } = data as {
    [key: string]: unknown;
  };
  return (
    isRecognisedMessageType(type) &&
    typeof commissionId === 'string' &&
    commissionTitle === '(DELETE)'
  );
}
