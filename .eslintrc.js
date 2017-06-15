module.exports = {
  "env": {
    "browser": true,
    "es6": true,
    "jest": true
  },
  "extends": [
    "eslint:recommended",
    "plugin:react/recommended",
    "prettier",
    "prettier/react"
  ],
  "plugins": [
    "standard",
    "promise",
    "react",
    "prettier"
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
    // prettier settings
    "prettier/prettier": [
      "error",
      {
        singleQuote: true,
        bracketSpacing: true,
        tabWidth: 2
      }
    ],
    "react/prop-types": "off",
    "jsx-quotes": ["error", "prefer-double"],
    "prefer-const": "error",
    "semi": ["error", "always"],
    "comma-dangle": ["error", "never"]
  }
};
