module.exports = {
  "root": true,
  'env': {
    'es6': true,
    'node': true
  },
  'extends': [
    'eslint:recommended',
    'prettier'
  ],
  'plugins': [
    'prettier'
  ],
  'parserOptions': {
    'ecmaVersion': 2017,
    'sourceType': 'module'
  },
  rules: {
    // prettier settings
    'prettier/prettier': [
      'error',
      {
        singleQuote: true,
        bracketSpacing: true,
        tabWidth: 2
      }
    ],
    'prefer-const': 'error',
    semi: ['error', 'always'],
    'comma-dangle': ['error', 'never']
  }
};
