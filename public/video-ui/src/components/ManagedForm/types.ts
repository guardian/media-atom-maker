/**
 * @fileoverview A temporary file for type definitions for props used in ManagedForm before we move this over fully to TypeScript.
 */

// @TODO: More strictly type errors object when moving ManagedForm to ts
export type UpdateErrors = (errors: Record<string, any>) => void;
export type UpdateWarnings = (warnings: Record<string, boolean>) => void;
