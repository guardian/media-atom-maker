import { z } from 'zod/v4';

const plutoMessageTypes = ['project-created', 'project-updated'];

const iconikMessageTypes = ['iconik-project-created'];
const plutoMessageTypeSchema = z.enum(plutoMessageTypes);

export const recognisedMessageTypes = [
  ...plutoMessageTypes,
  ...iconikMessageTypes
] as const;

const MessageTypeSchema = z.enum(recognisedMessageTypes);

export type MessageType = z.infer<typeof MessageTypeSchema>;

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

const iconikUpsertMessageSchema = z.looseObject({
  type: z.literal('iconik-project-created'),
  id: z.string(),
  title: z.string(),
  status: z.string(),
  commissionId: z.string(),
  commissionTitle: z.string(),
  workingGroupId: z.string(),
  workingGroupTitle: z.string(),
  masterPlaceholderId: z.string().optional()
});

export type IconikUpsertMessage = z.infer<typeof iconikUpsertMessageSchema>;

export function isIconikUpsertMessage(
  data: unknown
): data is IconikUpsertMessage {
  const parsed = iconikUpsertMessageSchema.safeParse(data);
  return parsed.success;
}

const PlutoDeleteMessageSchema = z.looseObject({
  type: MessageTypeSchema,
  commissionId: z.string(),
  commissionTitle: z.literal('(DELETE)')
});

export type PlutoDeleteMessage = z.infer<typeof PlutoDeleteMessageSchema>;

export function isDeleteMessage(data: unknown): data is PlutoDeleteMessage {
  const parsed = PlutoDeleteMessageSchema.safeParse(data);
  return parsed.success;
}
