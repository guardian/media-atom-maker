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
  static: {
    directory: wpConfig.output.path
  },
  hot: true,
  headers: {
    'X-Custom-Header': 'yes',
    'Access-Control-Allow-Origin' : '*'
  }
});

//
// Exports
//

//wpServer.use('/public', express.static('public'));

wpServer.listen(wpConfig.devServer.port, wpConfig.devServer.address, function() {
    console.log('WebpackDevServer listening on port %d', wpConfig.devServer.port);
});
