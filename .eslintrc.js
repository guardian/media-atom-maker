module.exports = {
  env: {
    browser: true,
    es6: true,
    jest: true
  },
  extends: ['eslint:recommended', 'plugin:react/recommended', 'prettier'],
  plugins: ['react', 'prettier'],
  parserOptions: {
    ecmaVersion: 6,
    sourceType: 'module',
    ecmaFeatures: {
      jsx: true
    }
  },
  parser: 'babel-eslint',
  rules: {
    'react/prop-types': 'off',
    'jsx-quotes': ['error', 'prefer-double'],
    'prefer-const': 'error',
    semi: ['warn', 'always'],
    'comma-dangle': ['error', 'never']
  }
};
