const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = {
  entry: './public/video-ui/src/app.js',
  mode: 'development',
  devtool: 'source-map',
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'babel-loader?cacheDirectory=true'
      },
      {
        test: /\.(sa|sc|c)ss$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader
          },
          'css-loader',
          'sass-loader'
        ]
      },
      {
        test: /\.woff(2)?(\?v=[0-9].[0-9].[0-9])?$/,
        loader: 'url-loader',
        options: {
          mimetype: 'application/font-woff'
        }
      },
      {
        test: /\.(ttf|eot|svg|gif|png)(\?v=[0-9].[0-9].[0-9])?$/,
        loader: 'file-loader',
        options: {
          name: '[name].[ext]'
        }
      }
    ],
    // http://andrewhfarmer.com/aws-sdk-with-webpack/
    noParse: [/aws\-sdk/]
  },
  resolve: { extensions: ['.js', '.jsx', '.json', '.scss'] },
  plugins: [
    new MiniCssExtractPlugin({
      filename: '[name].css',
      chunkFilename: '[id].css',
      ignoreOrder: false // Enable to remove warnings about conflicting order
    })
  ]
};
