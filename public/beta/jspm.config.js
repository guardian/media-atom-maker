SystemJS.config({
  paths: {
    "npm:": "beta/jspm_packages/npm/",
    "github:": "beta/jspm_packages/github/",
    "beta/": "beta/"
  },
  browserConfig: {
    "baseURL": "/assets"
  },
  devConfig: {
    "map": {
      "babel-plugin-transform-react-jsx": "npm:babel-plugin-transform-react-jsx@6.8.0",
      "core-js": "npm:core-js@2.4.1"
    },
    "packages": {
      "npm:babel-plugin-transform-react-jsx@6.8.0": {
        "map": {
          "babel-plugin-syntax-jsx": "npm:babel-plugin-syntax-jsx@6.13.0",
          "babel-runtime": "npm:babel-runtime@6.11.6",
          "babel-helper-builder-react-jsx": "npm:babel-helper-builder-react-jsx@6.9.0"
        }
      },
      "npm:babel-helper-builder-react-jsx@6.9.0": {
        "map": {
          "babel-runtime": "npm:babel-runtime@6.11.6",
          "esutils": "npm:esutils@2.0.2",
          "babel-types": "npm:babel-types@6.16.0",
          "lodash": "npm:lodash@4.16.4"
        }
      }
    }
  },
  transpiler: "plugin-babel",
  packages: {
    "beta": {
      "main": "app.js",
      "format": "esm",
      "meta": {
        "*.js": {
          "loader": "plugin-babel",
          "babelOptions": {
            "plugins": [
              "babel-plugin-transform-react-jsx"
            ]
          }
        }
      }
    }
  }
});

SystemJS.config({
  packageConfigPaths: [
    "npm:@*/*.json",
    "npm:*.json",
    "github:*/*.json"
  ],
  map: {
    "assert": "github:jspm/nodelibs-assert@0.2.0-alpha",
    "babel-preset-es2015": "npm:babel-preset-es2015@6.16.0",
    "buffer": "github:jspm/nodelibs-buffer@0.2.0-alpha",
    "child_process": "github:jspm/nodelibs-child_process@0.2.0-alpha",
    "constants": "github:jspm/nodelibs-constants@0.2.0-alpha",
    "crypto": "github:jspm/nodelibs-crypto@0.2.0-alpha",
    "domain": "github:jspm/nodelibs-domain@0.2.0-alpha",
    "events": "github:jspm/nodelibs-events@0.2.0-alpha",
    "fs": "github:jspm/nodelibs-fs@0.2.0-alpha",
    "http": "github:jspm/nodelibs-http@0.2.0-alpha",
    "https": "github:jspm/nodelibs-https@0.2.0-alpha",
    "os": "github:jspm/nodelibs-os@0.2.0-alpha",
    "panda-session": "npm:panda-session@0.1.6",
    "path": "github:jspm/nodelibs-path@0.2.0-alpha",
    "plugin-babel": "npm:systemjs-plugin-babel@0.0.16",
    "process": "github:jspm/nodelibs-process@0.2.0-alpha",
    "q": "npm:q@1.4.1",
    "react": "npm:react@15.3.2",
    "react-dom": "npm:react-dom@15.3.2",
    "react-router": "npm:react-router@2.8.1",
    "reqwest": "github:ded/reqwest@2.0.5",
    "stream": "github:jspm/nodelibs-stream@0.2.0-alpha",
    "string_decoder": "github:jspm/nodelibs-string_decoder@0.2.0-alpha",
    "url": "github:jspm/nodelibs-url@0.2.0-alpha",
    "util": "github:jspm/nodelibs-util@0.2.0-alpha",
    "vm": "github:jspm/nodelibs-vm@0.2.0-alpha",
    "zlib": "github:jspm/nodelibs-zlib@0.2.0-alpha"
  },
  packages: {
    "npm:react@15.3.2": {
      "map": {
        "loose-envify": "npm:loose-envify@1.2.0",
        "object-assign": "npm:object-assign@4.1.0",
        "fbjs": "npm:fbjs@0.8.5"
      }
    },
    "npm:loose-envify@1.2.0": {
      "map": {
        "js-tokens": "npm:js-tokens@1.0.3"
      }
    },
    "npm:fbjs@0.8.5": {
      "map": {
        "loose-envify": "npm:loose-envify@1.2.0",
        "object-assign": "npm:object-assign@4.1.0",
        "core-js": "npm:core-js@1.2.7",
        "ua-parser-js": "npm:ua-parser-js@0.7.10",
        "immutable": "npm:immutable@3.8.1",
        "promise": "npm:promise@7.1.1",
        "isomorphic-fetch": "npm:isomorphic-fetch@2.2.1"
      }
    },
    "npm:isomorphic-fetch@2.2.1": {
      "map": {
        "whatwg-fetch": "npm:whatwg-fetch@1.0.0",
        "node-fetch": "npm:node-fetch@1.6.3"
      }
    },
    "npm:promise@7.1.1": {
      "map": {
        "asap": "npm:asap@2.0.5"
      }
    },
    "npm:node-fetch@1.6.3": {
      "map": {
        "encoding": "npm:encoding@0.1.12",
        "is-stream": "npm:is-stream@1.1.0"
      }
    },
    "npm:encoding@0.1.12": {
      "map": {
        "iconv-lite": "npm:iconv-lite@0.4.13"
      }
    },
    "github:jspm/nodelibs-stream@0.2.0-alpha": {
      "map": {
        "stream-browserify": "npm:stream-browserify@2.0.1"
      }
    },
    "npm:stream-browserify@2.0.1": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "readable-stream": "npm:readable-stream@2.1.5"
      }
    },
    "npm:readable-stream@2.1.5": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "process-nextick-args": "npm:process-nextick-args@1.0.7",
        "buffer-shims": "npm:buffer-shims@1.0.0",
        "util-deprecate": "npm:util-deprecate@1.0.2",
        "core-util-is": "npm:core-util-is@1.0.2",
        "isarray": "npm:isarray@1.0.0",
        "string_decoder": "npm:string_decoder@0.10.31"
      }
    },
    "github:jspm/nodelibs-http@0.2.0-alpha": {
      "map": {
        "http-browserify": "npm:stream-http@2.4.0"
      }
    },
    "github:jspm/nodelibs-domain@0.2.0-alpha": {
      "map": {
        "domain-browserify": "npm:domain-browser@1.1.7"
      }
    },
    "npm:stream-http@2.4.0": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "readable-stream": "npm:readable-stream@2.1.5",
        "builtin-status-codes": "npm:builtin-status-codes@2.0.0",
        "to-arraybuffer": "npm:to-arraybuffer@1.0.1",
        "xtend": "npm:xtend@4.0.1"
      }
    },
    "github:jspm/nodelibs-url@0.2.0-alpha": {
      "map": {
        "url-browserify": "npm:url@0.11.0"
      }
    },
    "github:jspm/nodelibs-buffer@0.2.0-alpha": {
      "map": {
        "buffer-browserify": "npm:buffer@4.9.1"
      }
    },
    "npm:buffer@4.9.1": {
      "map": {
        "isarray": "npm:isarray@1.0.0",
        "ieee754": "npm:ieee754@1.1.8",
        "base64-js": "npm:base64-js@1.2.0"
      }
    },
    "npm:url@0.11.0": {
      "map": {
        "punycode": "npm:punycode@1.3.2",
        "querystring": "npm:querystring@0.2.0"
      }
    },
    "github:jspm/nodelibs-zlib@0.2.0-alpha": {
      "map": {
        "zlib-browserify": "npm:browserify-zlib@0.1.4"
      }
    },
    "npm:browserify-zlib@0.1.4": {
      "map": {
        "readable-stream": "npm:readable-stream@2.1.5",
        "pako": "npm:pako@0.2.9"
      }
    },
    "github:jspm/nodelibs-string_decoder@0.2.0-alpha": {
      "map": {
        "string_decoder-browserify": "npm:string_decoder@0.10.31"
      }
    },
    "github:jspm/nodelibs-os@0.2.0-alpha": {
      "map": {
        "os-browserify": "npm:os-browserify@0.2.1"
      }
    },
    "github:jspm/nodelibs-crypto@0.2.0-alpha": {
      "map": {
        "crypto-browserify": "npm:crypto-browserify@3.11.0"
      }
    },
    "npm:crypto-browserify@3.11.0": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "create-hmac": "npm:create-hmac@1.1.4",
        "create-hash": "npm:create-hash@1.1.2",
        "browserify-sign": "npm:browserify-sign@4.0.0",
        "browserify-cipher": "npm:browserify-cipher@1.0.0",
        "randombytes": "npm:randombytes@2.0.3",
        "create-ecdh": "npm:create-ecdh@4.0.0",
        "public-encrypt": "npm:public-encrypt@4.0.0",
        "diffie-hellman": "npm:diffie-hellman@5.0.2",
        "pbkdf2": "npm:pbkdf2@3.0.9"
      }
    },
    "npm:create-hmac@1.1.4": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "create-hash": "npm:create-hash@1.1.2"
      }
    },
    "npm:browserify-sign@4.0.0": {
      "map": {
        "create-hash": "npm:create-hash@1.1.2",
        "create-hmac": "npm:create-hmac@1.1.4",
        "inherits": "npm:inherits@2.0.3",
        "elliptic": "npm:elliptic@6.3.2",
        "parse-asn1": "npm:parse-asn1@5.0.0",
        "bn.js": "npm:bn.js@4.11.6",
        "browserify-rsa": "npm:browserify-rsa@4.0.1"
      }
    },
    "npm:create-hash@1.1.2": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "ripemd160": "npm:ripemd160@1.0.1",
        "sha.js": "npm:sha.js@2.4.5",
        "cipher-base": "npm:cipher-base@1.0.3"
      }
    },
    "npm:create-ecdh@4.0.0": {
      "map": {
        "elliptic": "npm:elliptic@6.3.2",
        "bn.js": "npm:bn.js@4.11.6"
      }
    },
    "npm:public-encrypt@4.0.0": {
      "map": {
        "parse-asn1": "npm:parse-asn1@5.0.0",
        "create-hash": "npm:create-hash@1.1.2",
        "randombytes": "npm:randombytes@2.0.3",
        "bn.js": "npm:bn.js@4.11.6",
        "browserify-rsa": "npm:browserify-rsa@4.0.1"
      }
    },
    "npm:pbkdf2@3.0.9": {
      "map": {
        "create-hmac": "npm:create-hmac@1.1.4"
      }
    },
    "npm:diffie-hellman@5.0.2": {
      "map": {
        "randombytes": "npm:randombytes@2.0.3",
        "bn.js": "npm:bn.js@4.11.6",
        "miller-rabin": "npm:miller-rabin@4.0.0"
      }
    },
    "npm:browserify-cipher@1.0.0": {
      "map": {
        "evp_bytestokey": "npm:evp_bytestokey@1.0.0",
        "browserify-des": "npm:browserify-des@1.0.0",
        "browserify-aes": "npm:browserify-aes@1.0.6"
      }
    },
    "npm:elliptic@6.3.2": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "bn.js": "npm:bn.js@4.11.6",
        "hash.js": "npm:hash.js@1.0.3",
        "brorand": "npm:brorand@1.0.6"
      }
    },
    "npm:parse-asn1@5.0.0": {
      "map": {
        "create-hash": "npm:create-hash@1.1.2",
        "pbkdf2": "npm:pbkdf2@3.0.9",
        "evp_bytestokey": "npm:evp_bytestokey@1.0.0",
        "browserify-aes": "npm:browserify-aes@1.0.6",
        "asn1.js": "npm:asn1.js@4.8.1"
      }
    },
    "npm:sha.js@2.4.5": {
      "map": {
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:evp_bytestokey@1.0.0": {
      "map": {
        "create-hash": "npm:create-hash@1.1.2"
      }
    },
    "npm:browserify-des@1.0.0": {
      "map": {
        "cipher-base": "npm:cipher-base@1.0.3",
        "inherits": "npm:inherits@2.0.3",
        "des.js": "npm:des.js@1.0.0"
      }
    },
    "npm:cipher-base@1.0.3": {
      "map": {
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:browserify-rsa@4.0.1": {
      "map": {
        "bn.js": "npm:bn.js@4.11.6",
        "randombytes": "npm:randombytes@2.0.3"
      }
    },
    "npm:miller-rabin@4.0.0": {
      "map": {
        "brorand": "npm:brorand@1.0.6",
        "bn.js": "npm:bn.js@4.11.6"
      }
    },
    "npm:browserify-aes@1.0.6": {
      "map": {
        "cipher-base": "npm:cipher-base@1.0.3",
        "create-hash": "npm:create-hash@1.1.2",
        "evp_bytestokey": "npm:evp_bytestokey@1.0.0",
        "inherits": "npm:inherits@2.0.3",
        "buffer-xor": "npm:buffer-xor@1.0.3"
      }
    },
    "npm:hash.js@1.0.3": {
      "map": {
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:asn1.js@4.8.1": {
      "map": {
        "bn.js": "npm:bn.js@4.11.6",
        "inherits": "npm:inherits@2.0.3",
        "minimalistic-assert": "npm:minimalistic-assert@1.0.0"
      }
    },
    "npm:des.js@1.0.0": {
      "map": {
        "minimalistic-assert": "npm:minimalistic-assert@1.0.0",
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:react-router@2.8.1": {
      "map": {
        "loose-envify": "npm:loose-envify@1.2.0",
        "invariant": "npm:invariant@2.2.1",
        "warning": "npm:warning@3.0.0",
        "hoist-non-react-statics": "npm:hoist-non-react-statics@1.2.0",
        "history": "npm:history@2.1.2"
      }
    },
    "npm:warning@3.0.0": {
      "map": {
        "loose-envify": "npm:loose-envify@1.2.0"
      }
    },
    "npm:invariant@2.2.1": {
      "map": {
        "loose-envify": "npm:loose-envify@1.2.0"
      }
    },
    "npm:history@2.1.2": {
      "map": {
        "warning": "npm:warning@2.1.0",
        "invariant": "npm:invariant@2.2.1",
        "deep-equal": "npm:deep-equal@1.0.1",
        "query-string": "npm:query-string@3.0.3"
      }
    },
    "npm:warning@2.1.0": {
      "map": {
        "loose-envify": "npm:loose-envify@1.2.0"
      }
    },
    "npm:query-string@3.0.3": {
      "map": {
        "strict-uri-encode": "npm:strict-uri-encode@1.1.0"
      }
    },
    "npm:babel-runtime@6.11.6": {
      "map": {
        "core-js": "npm:core-js@2.4.1",
        "regenerator-runtime": "npm:regenerator-runtime@0.9.5"
      }
    },
    "npm:babel-types@6.16.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "esutils": "npm:esutils@2.0.2",
        "lodash": "npm:lodash@4.16.4",
        "to-fast-properties": "npm:to-fast-properties@1.0.2"
      }
    },
    "npm:babel-preset-es2015@6.16.0": {
      "map": {
        "babel-plugin-transform-es2015-duplicate-keys": "npm:babel-plugin-transform-es2015-duplicate-keys@6.8.0",
        "babel-plugin-transform-es2015-block-scoped-functions": "npm:babel-plugin-transform-es2015-block-scoped-functions@6.8.0",
        "babel-plugin-transform-es2015-sticky-regex": "npm:babel-plugin-transform-es2015-sticky-regex@6.8.0",
        "babel-plugin-transform-es2015-shorthand-properties": "npm:babel-plugin-transform-es2015-shorthand-properties@6.8.0",
        "babel-plugin-transform-es2015-block-scoping": "npm:babel-plugin-transform-es2015-block-scoping@6.15.0",
        "babel-plugin-transform-es2015-classes": "npm:babel-plugin-transform-es2015-classes@6.14.0",
        "babel-plugin-transform-es2015-modules-amd": "npm:babel-plugin-transform-es2015-modules-amd@6.8.0",
        "babel-plugin-transform-es2015-computed-properties": "npm:babel-plugin-transform-es2015-computed-properties@6.8.0",
        "babel-plugin-check-es2015-constants": "npm:babel-plugin-check-es2015-constants@6.8.0",
        "babel-plugin-transform-es2015-for-of": "npm:babel-plugin-transform-es2015-for-of@6.8.0",
        "babel-plugin-transform-es2015-modules-umd": "npm:babel-plugin-transform-es2015-modules-umd@6.12.0",
        "babel-plugin-transform-es2015-modules-commonjs": "npm:babel-plugin-transform-es2015-modules-commonjs@6.16.0",
        "babel-plugin-transform-es2015-object-super": "npm:babel-plugin-transform-es2015-object-super@6.8.0",
        "babel-plugin-transform-es2015-modules-systemjs": "npm:babel-plugin-transform-es2015-modules-systemjs@6.14.0",
        "babel-plugin-transform-es2015-literals": "npm:babel-plugin-transform-es2015-literals@6.8.0",
        "babel-plugin-transform-es2015-spread": "npm:babel-plugin-transform-es2015-spread@6.8.0",
        "babel-plugin-transform-es2015-template-literals": "npm:babel-plugin-transform-es2015-template-literals@6.8.0",
        "babel-plugin-transform-es2015-typeof-symbol": "npm:babel-plugin-transform-es2015-typeof-symbol@6.8.0",
        "babel-plugin-transform-es2015-parameters": "npm:babel-plugin-transform-es2015-parameters@6.17.0",
        "babel-plugin-transform-es2015-unicode-regex": "npm:babel-plugin-transform-es2015-unicode-regex@6.11.0",
        "babel-plugin-transform-es2015-function-name": "npm:babel-plugin-transform-es2015-function-name@6.9.0",
        "babel-plugin-transform-es2015-destructuring": "npm:babel-plugin-transform-es2015-destructuring@6.16.0",
        "babel-plugin-transform-regenerator": "npm:babel-plugin-transform-regenerator@6.16.1",
        "babel-plugin-transform-es2015-arrow-functions": "npm:babel-plugin-transform-es2015-arrow-functions@6.8.0"
      }
    },
    "npm:babel-plugin-transform-es2015-modules-amd@6.8.0": {
      "map": {
        "babel-plugin-transform-es2015-modules-commonjs": "npm:babel-plugin-transform-es2015-modules-commonjs@6.16.0",
        "babel-template": "npm:babel-template@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-modules-umd@6.12.0": {
      "map": {
        "babel-plugin-transform-es2015-modules-amd": "npm:babel-plugin-transform-es2015-modules-amd@6.8.0",
        "babel-template": "npm:babel-template@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-block-scoping@6.15.0": {
      "map": {
        "babel-template": "npm:babel-template@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-types": "npm:babel-types@6.16.0",
        "babel-traverse": "npm:babel-traverse@6.16.0",
        "lodash": "npm:lodash@4.16.4"
      }
    },
    "npm:babel-plugin-transform-es2015-classes@6.14.0": {
      "map": {
        "babel-template": "npm:babel-template@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-types": "npm:babel-types@6.16.0",
        "babel-traverse": "npm:babel-traverse@6.16.0",
        "babel-messages": "npm:babel-messages@6.8.0",
        "babel-helper-optimise-call-expression": "npm:babel-helper-optimise-call-expression@6.8.0",
        "babel-helper-function-name": "npm:babel-helper-function-name@6.8.0",
        "babel-helper-replace-supers": "npm:babel-helper-replace-supers@6.16.0",
        "babel-helper-define-map": "npm:babel-helper-define-map@6.9.0"
      }
    },
    "npm:babel-plugin-transform-es2015-computed-properties@6.8.0": {
      "map": {
        "babel-template": "npm:babel-template@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-helper-define-map": "npm:babel-helper-define-map@6.9.0"
      }
    },
    "npm:babel-plugin-transform-es2015-modules-commonjs@6.16.0": {
      "map": {
        "babel-template": "npm:babel-template@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-types": "npm:babel-types@6.16.0",
        "babel-plugin-transform-strict-mode": "npm:babel-plugin-transform-strict-mode@6.11.3"
      }
    },
    "npm:babel-plugin-transform-es2015-modules-systemjs@6.14.0": {
      "map": {
        "babel-template": "npm:babel-template@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-helper-hoist-variables": "npm:babel-helper-hoist-variables@6.8.0"
      }
    },
    "npm:babel-plugin-transform-es2015-sticky-regex@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-types": "npm:babel-types@6.16.0",
        "babel-helper-regex": "npm:babel-helper-regex@6.9.0"
      }
    },
    "npm:babel-plugin-transform-es2015-shorthand-properties@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-types": "npm:babel-types@6.16.0"
      }
    },
    "npm:babel-plugin-transform-es2015-block-scoped-functions@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-duplicate-keys@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-types": "npm:babel-types@6.16.0"
      }
    },
    "npm:babel-plugin-transform-es2015-for-of@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-check-es2015-constants@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-object-super@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-helper-replace-supers": "npm:babel-helper-replace-supers@6.16.0"
      }
    },
    "npm:babel-helper-optimise-call-expression@6.8.0": {
      "map": {
        "babel-types": "npm:babel-types@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-helper-regex@6.9.0": {
      "map": {
        "babel-types": "npm:babel-types@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "lodash": "npm:lodash@4.16.4"
      }
    },
    "npm:babel-helper-replace-supers@6.16.0": {
      "map": {
        "babel-types": "npm:babel-types@6.16.0",
        "babel-helper-optimise-call-expression": "npm:babel-helper-optimise-call-expression@6.8.0",
        "babel-traverse": "npm:babel-traverse@6.16.0",
        "babel-messages": "npm:babel-messages@6.8.0",
        "babel-template": "npm:babel-template@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-helper-function-name@6.8.0": {
      "map": {
        "babel-types": "npm:babel-types@6.16.0",
        "babel-traverse": "npm:babel-traverse@6.16.0",
        "babel-template": "npm:babel-template@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-helper-get-function-arity": "npm:babel-helper-get-function-arity@6.8.0"
      }
    },
    "npm:babel-helper-define-map@6.9.0": {
      "map": {
        "babel-types": "npm:babel-types@6.16.0",
        "babel-helper-function-name": "npm:babel-helper-function-name@6.8.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "lodash": "npm:lodash@4.16.4"
      }
    },
    "npm:babel-template@6.16.0": {
      "map": {
        "babel-traverse": "npm:babel-traverse@6.16.0",
        "babel-types": "npm:babel-types@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "lodash": "npm:lodash@4.16.4",
        "babylon": "npm:babylon@6.11.4"
      }
    },
    "npm:babel-traverse@6.16.0": {
      "map": {
        "babel-types": "npm:babel-types@6.16.0",
        "babel-messages": "npm:babel-messages@6.8.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "lodash": "npm:lodash@4.16.4",
        "debug": "npm:debug@2.2.0",
        "invariant": "npm:invariant@2.2.1",
        "globals": "npm:globals@8.18.0",
        "babylon": "npm:babylon@6.11.4",
        "babel-code-frame": "npm:babel-code-frame@6.16.0"
      }
    },
    "npm:babel-plugin-transform-es2015-parameters@6.17.0": {
      "map": {
        "babel-traverse": "npm:babel-traverse@6.16.0",
        "babel-template": "npm:babel-template@6.16.0",
        "babel-types": "npm:babel-types@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-helper-get-function-arity": "npm:babel-helper-get-function-arity@6.8.0",
        "babel-helper-call-delegate": "npm:babel-helper-call-delegate@6.8.0"
      }
    },
    "npm:babel-plugin-transform-es2015-function-name@6.9.0": {
      "map": {
        "babel-types": "npm:babel-types@6.16.0",
        "babel-helper-function-name": "npm:babel-helper-function-name@6.8.0",
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-regenerator@6.16.1": {
      "map": {
        "babel-types": "npm:babel-types@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "private": "npm:private@0.1.6"
      }
    },
    "npm:babel-plugin-transform-strict-mode@6.11.3": {
      "map": {
        "babel-types": "npm:babel-types@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-unicode-regex@6.11.0": {
      "map": {
        "babel-helper-regex": "npm:babel-helper-regex@6.9.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "regexpu-core": "npm:regexpu-core@2.0.0"
      }
    },
    "npm:babel-helper-hoist-variables@6.8.0": {
      "map": {
        "babel-types": "npm:babel-types@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-spread@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-template-literals@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-literals@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-typeof-symbol@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-messages@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-arrow-functions@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-plugin-transform-es2015-destructuring@6.16.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6"
      }
    },
    "npm:babel-helper-get-function-arity@6.8.0": {
      "map": {
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-types": "npm:babel-types@6.16.0"
      }
    },
    "npm:babel-helper-call-delegate@6.8.0": {
      "map": {
        "babel-traverse": "npm:babel-traverse@6.16.0",
        "babel-runtime": "npm:babel-runtime@6.11.6",
        "babel-types": "npm:babel-types@6.16.0",
        "babel-helper-hoist-variables": "npm:babel-helper-hoist-variables@6.8.0"
      }
    },
    "npm:regexpu-core@2.0.0": {
      "map": {
        "regjsparser": "npm:regjsparser@0.1.5",
        "regjsgen": "npm:regjsgen@0.2.0",
        "regenerate": "npm:regenerate@1.3.1"
      }
    },
    "npm:debug@2.2.0": {
      "map": {
        "ms": "npm:ms@0.7.1"
      }
    },
    "npm:regjsparser@0.1.5": {
      "map": {
        "jsesc": "npm:jsesc@0.5.0"
      }
    },
    "npm:babel-code-frame@6.16.0": {
      "map": {
        "esutils": "npm:esutils@2.0.2",
        "chalk": "npm:chalk@1.1.3",
        "js-tokens": "npm:js-tokens@2.0.0"
      }
    },
    "npm:chalk@1.1.3": {
      "map": {
        "ansi-styles": "npm:ansi-styles@2.2.1",
        "has-ansi": "npm:has-ansi@2.0.0",
        "strip-ansi": "npm:strip-ansi@3.0.1",
        "supports-color": "npm:supports-color@2.0.0",
        "escape-string-regexp": "npm:escape-string-regexp@1.0.5"
      }
    },
    "npm:strip-ansi@3.0.1": {
      "map": {
        "ansi-regex": "npm:ansi-regex@2.0.0"
      }
    },
    "npm:has-ansi@2.0.0": {
      "map": {
        "ansi-regex": "npm:ansi-regex@2.0.0"
      }
    }
  }
});
