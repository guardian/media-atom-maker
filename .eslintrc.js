module.exports = {
  parser: '@typescript-eslint/parser',
  extends: [
    'plugin:react/recommended',
  ],
  parserOptions: {
    ecmaVersion: 2020,
    sourceType: 'module',
    ecmaFeatures: {
      jsx: true
    },
  },
  plugins: ['react', 'prettier'],
  rules: {
    'react/prop-types': 'off',
    'jsx-quotes': ['error', 'prefer-double'],
    'prefer-const': 'error',
    semi: ['warn', 'always'],
    'comma-dangle': ['error', 'never']
  },
  settings: {
    react: {
      version: 'detect'
    }
  }
};
