//
// Imports
//
const path = require('path');
const webpack = require('webpack');
const WebpackDevServer = require('webpack-dev-server');
const wpConfig = require('./build_config/webpack.devserver.conf.js');

//
// Webpack
//

const options = {
  hot: true,
  allowedHosts: "all",
  headers: {
    "X-Custom-Header": "yes",
    "Access-Control-Allow-Origin" : "*"
  },
  devMiddleware: {
    stats:   {
      colors: true
    },
    publicPath: "/assets/video-ui/build/"
  },
  client: {
    logging: "info",
    progress: true,
    webSocketURL: "ws://video-assets.local.dev-gutools.co.uk:80"
  },
  static: {
    watch: true,
    directory: path.join(__dirname, '..', 'static', 'video-ui', 'build')
  },
  port: wpConfig.devServer.port
};

const wpServer = new WebpackDevServer(options, webpack(wpConfig));

(async () => {
  await wpServer.start();
  console.log(`Dev server is listening on port ${wpConfig.devServer.port}`);
})();
