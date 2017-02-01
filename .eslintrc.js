module.exports = {
  "env": {
    "browser": true,
    "es6": true
  },
  "extends": [
    "eslint:recommended",
    "plugin:react/recommended"
  ],
  "plugins": [
    "standard",
    "promise",
    "react"
  ],
  "parserOptions": {
    "ecmaVersion": 6,
    "sourceType": "module",
    "ecmaFeatures": {
      "jsx": true
    }
  },
  "parser": "babel-eslint",
  "rules": {
    "react/prop-types": "warn",
    "jsx-quotes": ["error", "prefer-double"],
    "prefer-const": "error",
    "semi": ["error", "always"]
  }
};
