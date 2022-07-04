//
// Imports
//

var path = require('path');
var webpack = require('webpack');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');

//
// Config
//

var port = 9002;
var addr = 'video-assets.local.dev-gutools.co.uk';
var host = 'https://' + addr;

//
// Exports
//

module.exports = {
  mode: 'development',
  devServer: { inline: true, port: port, address: addr, host: host },
  entry: {
    'assets/video-ui/build/app': [
      'webpack-dev-server/client?' + host,
      'webpack/hot/dev-server',
      path.join(__dirname, '..', 'public', 'video-ui', 'src', 'app.js')
    ]
  },
  output: {
    path: path.join(__dirname, '..', 'public', 'video-ui', 'build'),
    publicPath: host + '/assets/video-ui/build/',
    filename: 'app.js'
  },
  module: {
    rules: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: 'babel-loader?cacheDirectory=true',
        options: {
          plugins: ['react-hot-loader/babel']
        }
      },
      {
        test: /\.tsx?$/,
        use: 'ts-loader?transpileOnly=true',
        exclude: /node_modules/
      },
      {
        test: require.resolve('react'),
        loader: 'expose-loader?React'
      },
      {
        test: /\.scss$/,
        use: ['style-loader', 'css-loader', 'sass-loader']
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.woff(2)?(\?v=[0-9].[0-9].[0-9])?$/,
        loader: 'url-loader?mimetype=application/font-woff'
      },
      {
        test: /\.(ttf|eot|svg|gif|png)(\?v=[0-9].[0-9].[0-9])?$/,
        loader: 'file-loader?name=[name].[ext]'
      }
    ],
    // http://andrewhfarmer.com/aws-sdk-with-webpack/
    noParse: [/aws\-sdk/]
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new ForkTsCheckerWebpackPlugin({
      compilerOptions: {
          noEmit: true
      }
  })],
  resolve: {
    modules: ['node_modules'],
    // Allows require('file') instead of require('file.js|x')
    extensions: ['.js', '.jsx', '.json', '.ts', '.tsx']
  }
};
