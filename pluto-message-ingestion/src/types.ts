import { z } from 'zod/v4';

const plutoMessageTypes = ['project-created', 'project-updated'];

const plutoMessageTypeSchema = z.enum(plutoMessageTypes);

export type MessageType = z.infer<typeof plutoMessageTypeSchema>;

export function isRecognisedMessageType(type: unknown): type is MessageType {
  return plutoMessageTypes.includes(type as MessageType);
}

export function hasRecognisedMessageType(data: unknown): boolean {
  if (!data || typeof data !== 'object' || data === null) {
    return false;
  }
  const { type } = data as { [key: string]: unknown };
  return isRecognisedMessageType(type);
}

const plutoUpsertMessageSchema = z.looseObject({
  type: plutoMessageTypeSchema,
  id: z.string(),
  title: z.string(),
  status: z.string(),
  commissionId: z.string(),
  commissionTitle: z.string(),
  productionOffice: z.string(),
  created: z.string()
});

export type PlutoUpsertMessage = z.infer<typeof plutoUpsertMessageSchema>;

export function isPlutoUpsertMessage(
  data: unknown
): data is PlutoUpsertMessage {
  const parsed = plutoUpsertMessageSchema.safeParse(data);
  return parsed.success;
}

const PlutoDeleteMessageSchema = z.looseObject({
  type: plutoMessageTypeSchema,
  commissionId: z.string(),
  commissionTitle: z.literal('(DELETE)')
});

export type PlutoDeleteMessage = z.infer<typeof PlutoDeleteMessageSchema>;

export function isPlutoDeleteMessage(
  data: unknown
): data is PlutoDeleteMessage {
  const parsed = PlutoDeleteMessageSchema.safeParse(data);
  return parsed.success;
}
