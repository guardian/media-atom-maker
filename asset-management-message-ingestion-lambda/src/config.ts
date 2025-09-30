import { fromNodeProviderChain } from '@aws-sdk/credential-providers';

/**
 * Is this application running locally, or in AWS?
 * LAMBDA_TASK_ROOT & AWS_EXECUTION_ENV are set when running in AWS
 * See: https://docs.aws.amazon.com/lambda/latest/dg/configuration-envvars.html
 */
export const isRunningLocally =
  !process.env.LAMBDA_TASK_ROOT && !process.env.AWS_EXECUTION_ENV;

export function getFromEnv(key: string): string {
  const value = process.env[key];
  if (!value) {
    throw new Error(`Missing required environment variable ${key}`);
  }
  return value;
}

export function getOptionalFromEnv(key: string): string | undefined {
  return process.env[key];
}

export const awsConfig = isRunningLocally
  ? {
      region: 'eu-west-1',
      credentials: fromNodeProviderChain({
        profile: 'media-service'
      })
    }
  : {};

const stack = getOptionalFromEnv('STACK') ?? 'media-service';

const stage = getOptionalFromEnv('STAGE') ?? 'DEV';

export const hostSecretName = `${stack}/${stage}/media-atom-maker/hostname`;
export const hmacSecretName = `${stack}/${stage}/media-atom-maker/hmac-secret`;
