SystemJS.config({
  paths: {
    "npm:": "jspm_packages/npm/",
    "github:": "jspm_packages/github/"
  },
  browserConfig: {
    "baseURL": "/assets",
    "paths": {
      "media-atom-maker/": "beta/"
    },
    "bundles": {
      "build.js": [
        "media-atom-maker/app.js",
        "npm:react-dom@15.3.2/index.js",
        "npm:react-dom@15.3.2.json",
        "npm:react@15.3.2/lib/ReactDOM.js",
        "npm:react@15.3.2.json",
        "github:jspm/nodelibs-process@0.2.0-alpha/process.js",
        "github:jspm/nodelibs-process@0.2.0-alpha.json",
        "npm:react@15.3.2/lib/ReactDOMNullInputValuePropHook.js",
        "npm:fbjs@0.8.5/lib/warning.js",
        "npm:fbjs@0.8.5.json",
        "npm:fbjs@0.8.5/lib/emptyFunction.js",
        "npm:react@15.3.2/lib/ReactComponentTreeHook.js",
        "npm:fbjs@0.8.5/lib/invariant.js",
        "npm:react@15.3.2/lib/ReactCurrentOwner.js",
        "npm:react@15.3.2/lib/reactProdInvariant.js",
        "npm:react@15.3.2/lib/ReactDOMUnknownPropertyHook.js",
        "npm:react@15.3.2/lib/EventPluginRegistry.js",
        "npm:react@15.3.2/lib/DOMProperty.js",
        "npm:react@15.3.2/lib/ReactInstrumentation.js",
        "npm:react@15.3.2/lib/ReactDebugTool.js",
        "npm:fbjs@0.8.5/lib/performanceNow.js",
        "npm:fbjs@0.8.5/lib/performance.js",
        "npm:fbjs@0.8.5/lib/ExecutionEnvironment.js",
        "npm:react@15.3.2/lib/ReactChildrenMutationWarningHook.js",
        "npm:react@15.3.2/lib/ReactHostOperationHistoryHook.js",
        "npm:react@15.3.2/lib/ReactInvalidSetStateWarningHook.js",
        "npm:react@15.3.2/lib/renderSubtreeIntoContainer.js",
        "npm:react@15.3.2/lib/ReactMount.js",
        "npm:react@15.3.2/lib/shouldUpdateReactComponent.js",
        "npm:react@15.3.2/lib/setInnerHTML.js",
        "npm:react@15.3.2/lib/createMicrosoftUnsafeLocalFunction.js",
        "npm:react@15.3.2/lib/DOMNamespaces.js",
        "npm:react@15.3.2/lib/instantiateReactComponent.js",
        "npm:react@15.3.2/lib/ReactHostComponent.js",
        "npm:object-assign@4.1.0/index.js",
        "npm:object-assign@4.1.0.json",
        "npm:react@15.3.2/lib/ReactEmptyComponent.js",
        "npm:react@15.3.2/lib/ReactCompositeComponent.js",
        "npm:fbjs@0.8.5/lib/shallowEqual.js",
        "npm:fbjs@0.8.5/lib/emptyObject.js",
        "npm:react@15.3.2/lib/checkReactTypeSpec.js",
        "npm:react@15.3.2/lib/ReactPropTypesSecret.js",
        "npm:react@15.3.2/lib/ReactPropTypeLocationNames.js",
        "npm:react@15.3.2/lib/ReactReconciler.js",
        "npm:react@15.3.2/lib/ReactRef.js",
        "npm:react@15.3.2/lib/ReactOwner.js",
        "npm:react@15.3.2/lib/ReactPropTypeLocations.js",
        "npm:fbjs@0.8.5/lib/keyMirror.js",
        "npm:react@15.3.2/lib/ReactNodeTypes.js",
        "npm:react@15.3.2/lib/ReactElement.js",
        "npm:react@15.3.2/lib/canDefineProperty.js",
        "npm:react@15.3.2/lib/ReactInstanceMap.js",
        "npm:react@15.3.2/lib/ReactErrorUtils.js",
        "npm:react@15.3.2/lib/ReactComponentEnvironment.js",
        "npm:react@15.3.2/lib/ReactUpdates.js",
        "npm:react@15.3.2/lib/Transaction.js",
        "npm:react@15.3.2/lib/ReactFeatureFlags.js",
        "npm:react@15.3.2/lib/PooledClass.js",
        "npm:react@15.3.2/lib/CallbackQueue.js",
        "npm:react@15.3.2/lib/ReactUpdateQueue.js",
        "npm:react@15.3.2/lib/ReactMarkupChecksum.js",
        "npm:react@15.3.2/lib/adler32.js",
        "npm:react@15.3.2/lib/ReactDOMFeatureFlags.js",
        "npm:react@15.3.2/lib/ReactDOMContainerInfo.js",
        "npm:react@15.3.2/lib/validateDOMNesting.js",
        "npm:react@15.3.2/lib/ReactDOMComponentTree.js",
        "npm:react@15.3.2/lib/ReactDOMComponentFlags.js",
        "npm:react@15.3.2/lib/ReactBrowserEventEmitter.js",
        "npm:react@15.3.2/lib/isEventSupported.js",
        "npm:react@15.3.2/lib/getVendorPrefixedEventName.js",
        "npm:react@15.3.2/lib/ViewportMetrics.js",
        "npm:react@15.3.2/lib/ReactEventEmitterMixin.js",
        "npm:react@15.3.2/lib/EventPluginHub.js",
        "npm:react@15.3.2/lib/forEachAccumulated.js",
        "npm:react@15.3.2/lib/accumulateInto.js",
        "npm:react@15.3.2/lib/EventPluginUtils.js",
        "npm:react@15.3.2/lib/EventConstants.js",
        "npm:react@15.3.2/lib/DOMLazyTree.js",
        "npm:react@15.3.2/lib/setTextContent.js",
        "npm:react@15.3.2/lib/escapeTextContentForBrowser.js",
        "npm:react@15.3.2/lib/getHostComponentFromComposite.js",
        "npm:react@15.3.2/lib/findDOMNode.js",
        "npm:react@15.3.2/lib/ReactVersion.js",
        "npm:react@15.3.2/lib/ReactDefaultInjection.js",
        "npm:react@15.3.2/lib/SimpleEventPlugin.js",
        "npm:fbjs@0.8.5/lib/keyOf.js",
        "npm:react@15.3.2/lib/getEventCharCode.js",
        "npm:react@15.3.2/lib/SyntheticWheelEvent.js",
        "npm:react@15.3.2/lib/SyntheticMouseEvent.js",
        "npm:react@15.3.2/lib/getEventModifierState.js",
        "npm:react@15.3.2/lib/SyntheticUIEvent.js",
        "npm:react@15.3.2/lib/getEventTarget.js",
        "npm:react@15.3.2/lib/SyntheticEvent.js",
        "npm:react@15.3.2/lib/SyntheticTransitionEvent.js",
        "npm:react@15.3.2/lib/SyntheticTouchEvent.js",
        "npm:react@15.3.2/lib/SyntheticDragEvent.js",
        "npm:react@15.3.2/lib/SyntheticKeyboardEvent.js",
        "npm:react@15.3.2/lib/getEventKey.js",
        "npm:react@15.3.2/lib/SyntheticFocusEvent.js",
        "npm:react@15.3.2/lib/SyntheticClipboardEvent.js",
        "npm:react@15.3.2/lib/SyntheticAnimationEvent.js",
        "npm:react@15.3.2/lib/EventPropagators.js",
        "npm:fbjs@0.8.5/lib/EventListener.js",
        "npm:react@15.3.2/lib/SelectEventPlugin.js",
        "npm:react@15.3.2/lib/isTextInputElement.js",
        "npm:fbjs@0.8.5/lib/getActiveElement.js",
        "npm:react@15.3.2/lib/ReactInputSelection.js",
        "npm:fbjs@0.8.5/lib/focusNode.js",
        "npm:fbjs@0.8.5/lib/containsNode.js",
        "npm:fbjs@0.8.5/lib/isTextNode.js",
        "npm:fbjs@0.8.5/lib/isNode.js",
        "npm:react@15.3.2/lib/ReactDOMSelection.js",
        "npm:react@15.3.2/lib/getTextContentAccessor.js",
        "npm:react@15.3.2/lib/getNodeForCharacterOffset.js",
        "npm:react@15.3.2/lib/SVGDOMPropertyConfig.js",
        "npm:react@15.3.2/lib/ReactReconcileTransaction.js",
        "npm:react@15.3.2/lib/ReactInjection.js",
        "npm:react@15.3.2/lib/ReactClass.js",
        "npm:react@15.3.2/lib/ReactNoopUpdateQueue.js",
        "npm:react@15.3.2/lib/ReactComponent.js",
        "npm:react@15.3.2/lib/ReactEventListener.js",
        "npm:fbjs@0.8.5/lib/getUnboundedScrollPosition.js",
        "npm:react@15.3.2/lib/ReactDefaultBatchingStrategy.js",
        "npm:react@15.3.2/lib/ReactDOMTextComponent.js",
        "npm:react@15.3.2/lib/DOMChildrenOperations.js",
        "npm:react@15.3.2/lib/ReactMultiChildUpdateTypes.js",
        "npm:react@15.3.2/lib/Danger.js",
        "npm:fbjs@0.8.5/lib/createNodesFromMarkup.js",
        "npm:fbjs@0.8.5/lib/getMarkupWrap.js",
        "npm:fbjs@0.8.5/lib/createArrayFromMixed.js",
        "npm:react@15.3.2/lib/ReactDOMTreeTraversal.js",
        "npm:react@15.3.2/lib/ReactDOMEmptyComponent.js",
        "npm:react@15.3.2/lib/ReactDOMComponent.js",
        "npm:react@15.3.2/lib/ReactServerRenderingTransaction.js",
        "npm:react@15.3.2/lib/ReactServerUpdateQueue.js",
        "npm:react@15.3.2/lib/ReactMultiChild.js",
        "npm:react@15.3.2/lib/flattenChildren.js",
        "npm:react@15.3.2/lib/traverseAllChildren.js",
        "npm:react@15.3.2/lib/KeyEscapeUtils.js",
        "npm:react@15.3.2/lib/getIteratorFn.js",
        "npm:react@15.3.2/lib/ReactChildReconciler.js",
        "npm:react@15.3.2/lib/ReactDOMTextarea.js",
        "npm:react@15.3.2/lib/LinkedValueUtils.js",
        "npm:react@15.3.2/lib/ReactPropTypes.js",
        "npm:react@15.3.2/lib/DisabledInputUtils.js",
        "npm:react@15.3.2/lib/ReactDOMSelect.js",
        "npm:react@15.3.2/lib/ReactDOMOption.js",
        "npm:react@15.3.2/lib/ReactChildren.js",
        "npm:react@15.3.2/lib/ReactDOMInput.js",
        "npm:react@15.3.2/lib/DOMPropertyOperations.js",
        "npm:react@15.3.2/lib/quoteAttributeValueForBrowser.js",
        "npm:react@15.3.2/lib/ReactDOMButton.js",
        "npm:react@15.3.2/lib/CSSPropertyOperations.js",
        "npm:fbjs@0.8.5/lib/memoizeStringOnly.js",
        "npm:fbjs@0.8.5/lib/hyphenateStyleName.js",
        "npm:fbjs@0.8.5/lib/hyphenate.js",
        "npm:react@15.3.2/lib/dangerousStyleValue.js",
        "npm:react@15.3.2/lib/CSSProperty.js",
        "npm:fbjs@0.8.5/lib/camelizeStyleName.js",
        "npm:fbjs@0.8.5/lib/camelize.js",
        "npm:react@15.3.2/lib/AutoFocusUtils.js",
        "npm:react@15.3.2/lib/ReactComponentBrowserEnvironment.js",
        "npm:react@15.3.2/lib/ReactDOMIDOperations.js",
        "npm:react@15.3.2/lib/HTMLDOMPropertyConfig.js",
        "npm:react@15.3.2/lib/EnterLeaveEventPlugin.js",
        "npm:react@15.3.2/lib/DefaultEventPluginOrder.js",
        "npm:react@15.3.2/lib/ChangeEventPlugin.js",
        "npm:react@15.3.2/lib/BeforeInputEventPlugin.js",
        "npm:react@15.3.2/lib/SyntheticInputEvent.js",
        "npm:react@15.3.2/lib/SyntheticCompositionEvent.js",
        "npm:react@15.3.2/lib/FallbackCompositionState.js",
        "npm:react@15.3.2/react.js",
        "npm:react@15.3.2/lib/React.js",
        "npm:react@15.3.2/lib/ReactElementValidator.js",
        "npm:react@15.3.2/lib/onlyChild.js",
        "npm:react@15.3.2/lib/ReactDOMFactories.js",
        "npm:react@15.3.2/lib/ReactPureComponent.js"
      ]
    }
  },
  nodeConfig: {
    "paths": {
      "media-atom-maker/": "beta/"
    }
  },
  devConfig: {
    "map": {
      "plugin-babel": "npm:systemjs-plugin-babel@0.0.16",
      "babel-plugin-transform-react-jsx": "npm:babel-plugin-transform-react-jsx@6.8.0",
      "core-js": "npm:core-js@2.4.1"
    },
    "packages": {
      "npm:babel-plugin-transform-react-jsx@6.8.0": {
        "map": {
          "babel-runtime": "npm:babel-runtime@6.11.6",
          "babel-plugin-syntax-jsx": "npm:babel-plugin-syntax-jsx@6.13.0",
          "babel-helper-builder-react-jsx": "npm:babel-helper-builder-react-jsx@6.9.0"
        }
      },
      "npm:babel-runtime@6.11.6": {
        "map": {
          "core-js": "npm:core-js@2.4.1",
          "regenerator-runtime": "npm:regenerator-runtime@0.9.5"
        }
      },
      "npm:babel-helper-builder-react-jsx@6.9.0": {
        "map": {
          "babel-runtime": "npm:babel-runtime@6.11.6",
          "esutils": "npm:esutils@2.0.2",
          "babel-types": "npm:babel-types@6.16.0",
          "lodash": "npm:lodash@4.16.4"
        }
      },
      "npm:babel-types@6.16.0": {
        "map": {
          "babel-runtime": "npm:babel-runtime@6.11.6",
          "esutils": "npm:esutils@2.0.2",
          "lodash": "npm:lodash@4.16.4",
          "to-fast-properties": "npm:to-fast-properties@1.0.2"
        }
      }
    }
  },
  transpiler: "plugin-babel",
  packages: {
    "media-atom-maker": {
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
    "path": "github:jspm/nodelibs-path@0.2.0-alpha",
    "process": "github:jspm/nodelibs-process@0.2.0-alpha",
    "react": "npm:react@15.3.2",
    "react-dom": "npm:react-dom@15.3.2",
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
        "fbjs": "npm:fbjs@0.8.5",
        "loose-envify": "npm:loose-envify@1.2.0",
        "object-assign": "npm:object-assign@4.1.0"
      }
    },
    "npm:fbjs@0.8.5": {
      "map": {
        "loose-envify": "npm:loose-envify@1.2.0",
        "object-assign": "npm:object-assign@4.1.0",
        "core-js": "npm:core-js@1.2.7",
        "immutable": "npm:immutable@3.8.1",
        "promise": "npm:promise@7.1.1",
        "ua-parser-js": "npm:ua-parser-js@0.7.10",
        "isomorphic-fetch": "npm:isomorphic-fetch@2.2.1"
      }
    },
    "npm:loose-envify@1.2.0": {
      "map": {
        "js-tokens": "npm:js-tokens@1.0.3"
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
    "github:jspm/nodelibs-domain@0.2.0-alpha": {
      "map": {
        "domain-browserify": "npm:domain-browser@1.1.7"
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
        "isarray": "npm:isarray@1.0.0",
        "core-util-is": "npm:core-util-is@1.0.2",
        "buffer-shims": "npm:buffer-shims@1.0.0",
        "util-deprecate": "npm:util-deprecate@1.0.2",
        "process-nextick-args": "npm:process-nextick-args@1.0.7",
        "string_decoder": "npm:string_decoder@0.10.31"
      }
    },
    "github:jspm/nodelibs-string_decoder@0.2.0-alpha": {
      "map": {
        "string_decoder-browserify": "npm:string_decoder@0.10.31"
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
        "base64-js": "npm:base64-js@1.2.0",
        "ieee754": "npm:ieee754@1.1.8"
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
        "create-hash": "npm:create-hash@1.1.2",
        "browserify-cipher": "npm:browserify-cipher@1.0.0",
        "create-hmac": "npm:create-hmac@1.1.4",
        "create-ecdh": "npm:create-ecdh@4.0.0",
        "public-encrypt": "npm:public-encrypt@4.0.0",
        "diffie-hellman": "npm:diffie-hellman@5.0.2",
        "browserify-sign": "npm:browserify-sign@4.0.0",
        "randombytes": "npm:randombytes@2.0.3",
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
        "inherits": "npm:inherits@2.0.3",
        "create-hmac": "npm:create-hmac@1.1.4",
        "create-hash": "npm:create-hash@1.1.2",
        "parse-asn1": "npm:parse-asn1@5.0.0",
        "elliptic": "npm:elliptic@6.3.2",
        "bn.js": "npm:bn.js@4.11.6",
        "browserify-rsa": "npm:browserify-rsa@4.0.1"
      }
    },
    "npm:create-hash@1.1.2": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "ripemd160": "npm:ripemd160@1.0.1",
        "cipher-base": "npm:cipher-base@1.0.3",
        "sha.js": "npm:sha.js@2.4.5"
      }
    },
    "npm:public-encrypt@4.0.0": {
      "map": {
        "create-hash": "npm:create-hash@1.1.2",
        "randombytes": "npm:randombytes@2.0.3",
        "parse-asn1": "npm:parse-asn1@5.0.0",
        "bn.js": "npm:bn.js@4.11.6",
        "browserify-rsa": "npm:browserify-rsa@4.0.1"
      }
    },
    "npm:diffie-hellman@5.0.2": {
      "map": {
        "randombytes": "npm:randombytes@2.0.3",
        "miller-rabin": "npm:miller-rabin@4.0.0",
        "bn.js": "npm:bn.js@4.11.6"
      }
    },
    "npm:browserify-cipher@1.0.0": {
      "map": {
        "browserify-des": "npm:browserify-des@1.0.0",
        "evp_bytestokey": "npm:evp_bytestokey@1.0.0",
        "browserify-aes": "npm:browserify-aes@1.0.6"
      }
    },
    "npm:create-ecdh@4.0.0": {
      "map": {
        "elliptic": "npm:elliptic@6.3.2",
        "bn.js": "npm:bn.js@4.11.6"
      }
    },
    "npm:pbkdf2@3.0.9": {
      "map": {
        "create-hmac": "npm:create-hmac@1.1.4"
      }
    },
    "npm:browserify-des@1.0.0": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "cipher-base": "npm:cipher-base@1.0.3",
        "des.js": "npm:des.js@1.0.0"
      }
    },
    "npm:elliptic@6.3.2": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "bn.js": "npm:bn.js@4.11.6",
        "brorand": "npm:brorand@1.0.6",
        "hash.js": "npm:hash.js@1.0.3"
      }
    },
    "npm:parse-asn1@5.0.0": {
      "map": {
        "create-hash": "npm:create-hash@1.1.2",
        "evp_bytestokey": "npm:evp_bytestokey@1.0.0",
        "pbkdf2": "npm:pbkdf2@3.0.9",
        "browserify-aes": "npm:browserify-aes@1.0.6",
        "asn1.js": "npm:asn1.js@4.8.1"
      }
    },
    "npm:miller-rabin@4.0.0": {
      "map": {
        "bn.js": "npm:bn.js@4.11.6",
        "brorand": "npm:brorand@1.0.6"
      }
    },
    "npm:browserify-rsa@4.0.1": {
      "map": {
        "bn.js": "npm:bn.js@4.11.6",
        "randombytes": "npm:randombytes@2.0.3"
      }
    },
    "npm:cipher-base@1.0.3": {
      "map": {
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:evp_bytestokey@1.0.0": {
      "map": {
        "create-hash": "npm:create-hash@1.1.2"
      }
    },
    "npm:browserify-aes@1.0.6": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "cipher-base": "npm:cipher-base@1.0.3",
        "create-hash": "npm:create-hash@1.1.2",
        "evp_bytestokey": "npm:evp_bytestokey@1.0.0",
        "buffer-xor": "npm:buffer-xor@1.0.3"
      }
    },
    "npm:sha.js@2.4.5": {
      "map": {
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:asn1.js@4.8.1": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "bn.js": "npm:bn.js@4.11.6",
        "minimalistic-assert": "npm:minimalistic-assert@1.0.0"
      }
    },
    "npm:des.js@1.0.0": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "minimalistic-assert": "npm:minimalistic-assert@1.0.0"
      }
    },
    "npm:hash.js@1.0.3": {
      "map": {
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "github:jspm/nodelibs-os@0.2.0-alpha": {
      "map": {
        "os-browserify": "npm:os-browserify@0.2.1"
      }
    },
    "github:jspm/nodelibs-zlib@0.2.0-alpha": {
      "map": {
        "zlib-browserify": "npm:browserify-zlib@0.1.4"
      }
    },
    "github:jspm/nodelibs-url@0.2.0-alpha": {
      "map": {
        "url-browserify": "npm:url@0.11.0"
      }
    },
    "npm:browserify-zlib@0.1.4": {
      "map": {
        "readable-stream": "npm:readable-stream@2.1.5",
        "pako": "npm:pako@0.2.9"
      }
    },
    "npm:url@0.11.0": {
      "map": {
        "punycode": "npm:punycode@1.3.2",
        "querystring": "npm:querystring@0.2.0"
      }
    },
    "github:jspm/nodelibs-http@0.2.0-alpha": {
      "map": {
        "http-browserify": "npm:stream-http@2.4.0"
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
    }
  }
});
