//
// Imports
//
var path             = require('path');
var webpack          = require('webpack');
var WebpackDevServer = require('webpack-dev-server');
var wpConfig         = require('./build_config/webpack.devserver.conf.js');

//
// Webpack
//

var wpServer = new WebpackDevServer(webpack(wpConfig), {
  contentBase:  wpConfig.output.path,
  publicPath:   '/assets/video-ui/build/',
  hot:          true,
  progress:     true,
  noInfo:       true,
  clientLogLevel: "info",
  watchOptions: {
    aggregateTimeout: 300,
    poll:             1000
  },
  quiet:   false,
  headers: {
    'X-Custom-Header': 'yes',
    'Access-Control-Allow-Origin' : '*'
  },
  stats:   {
    colors: true
  }
});

//
// Exports
//

//wpServer.use('/public', express.static('public'));

wpServer.listen(wpConfig.devServer.port, wpConfig.devServer.address, function() {
    console.log('WebpackDevServer listening on port %d', wpConfig.devServer.port);
});
