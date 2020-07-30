var ExtractTextPlugin = require("extract-text-webpack-plugin");

module.exports = {
  devtool: "source-map",
  module: {
    loaders: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: "babel-loader?cacheDirectory=true"
      },
      {
        test: /\.scss$/,
        use: ["style-loader", "css-loader?sourceMap!sass-loader?sourceMap"]
      },
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader?sourceMap"]
      },
      {
        test: /\.woff(2)?(\?v=[0-9].[0-9].[0-9])?$/,
        loader: "url-loader?mimetype=application/font-woff"
      },
      {
        test: /\.(ttf|eot|svg|gif)(\?v=[0-9].[0-9].[0-9])?$/,
        loader: "file-loader?name=[name].[ext]"
      }
    ],
    // http://andrewhfarmer.com/aws-sdk-with-webpack/
    noParse: [/aws\-sdk/]
  },
  resolveLoader: {},
  resolve: {
    extensions: [".js", ".jsx", ".json", ".scss"]
  },

  plugins: [new ExtractTextPlugin("main.css")],
  node: {
    fs: "empty",
    net: "empty",
    tls: "empty"
  }
};
