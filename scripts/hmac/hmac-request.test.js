const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const { mkdtempSync, rmSync, rmdirSync, writeFileSync } = require('node:fs');
const { tmpdir } = require('node:os');
const path = require('node:path');
const { test } = require('node:test');

function runRequest(status) {
  const directory = mkdtempSync(path.join(tmpdir(), 'mam-hmac-test-'));
  const configFile = path.join(directory, 'test.conf');
  const script = path.join(__dirname, 'make-hmac-request.js');
  writeFileSync(configFile, `
    host = "mam.example.test"
    secret = "synthetic-test-secret"
    s3-uploader.url = "https://uploads.example.test"
    upload-url = \${s3-uploader.url}
  `);

  try {
    return spawnSync(process.execPath, ['-e', `
      const assert = require('node:assert/strict');
      const { createHmac } = require('node:crypto');
      global.fetch = async (url, options) => {
        assert.equal(url, 'https://mam.example.test/api/atoms');
        assert.equal(options.method, 'POST');
        assert.equal(options.body, '{"title":"hmac probe"}');
        assert.equal(options.headers['Content-Type'], 'application/json');
        const date = options.headers['X-Gu-Tools-HMAC-Date'];
        const signature = createHmac('sha256', 'synthetic-test-secret')
          .update(date + '\\n/api/atoms').digest('base64');
        assert.equal(options.headers['X-Gu-Tools-HMAC-Token'], 'HMAC ' + signature);
        return {
          ok: ${status} === 200,
          status: ${status},
          statusText: 'Bad Request',
          text: async () => 'synthetic response'
        };
      };
      process.argv = ['node', ${JSON.stringify(script)},
        '--config-file', ${JSON.stringify(configFile)},
        '--path', 'api/atoms', '--method', 'post',
        '--data', '{"title":"hmac probe"}'];
      require(${JSON.stringify(script)});
    `], { encoding: 'utf8' });
  } finally {
    rmSync(configFile);
    rmdirSync(directory);
  }
}

test('reads a full config with hyphenated substitutions and signs the request', () => {
  const result = runRequest(200);
  assert.equal(result.status, 0, result.stderr);
  assert.equal(result.stderr, '');
  assert.equal(result.stdout.trim(), 'synthetic response');
});

test('reports HTTP rejection after authentication without treating it as success', () => {
  const result = runRequest(400);
  assert.equal(result.status, 0, result.stderr);
  assert.equal(result.stderr, '');
  assert.match(result.stdout, /ERROR! 400 \(Bad Request\) POST https:\/\/mam.example.test\/api\/atoms/);
});
