//
// Imports
//

var path    = require('path');
var webpack = require('webpack');

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
    port:  port,
    addr:  addr,
    host:  host,
    devServer: { inline: true },
    entry: {
        'assets/video-ui/build/app': [
            'webpack-dev-server/client?' + host,
            'webpack/hot/dev-server',
            path.join(__dirname, '..', 'public', 'video-ui', 'src', 'app.js')
        ]
    },
    output: {
        path:       path.join(__dirname, '..', 'public', 'video-ui', 'build'),
        publicPath: host + '/assets/video-ui/build/',
        filename:   'app.js'
    },
    resolveLoader: {
        modulesDirectories: ['node_modules']
    },
    module: {
        loaders: [
            {
                test:    /\.jsx?$/,
                exclude: /node_modules/,
                loaders: ['babel?presets[]=es2015&presets[]=react&plugins[]=transform-object-assign&plugins[]=transform-class-properties&plugins[]=react-hot-loader/babel']
            },
            {
                test:   require.resolve('react'),
                loader: 'expose?React'
            },
            {
                test: /\.scss$/,
                loaders: ['style', 'css', 'sass']
            },
            {
                test: /\.css$/,
                loaders: ['style', 'css']
            },
            {
                test: /\.woff(2)?(\?v=[0-9].[0-9].[0-9])?$/,
                loader: "url-loader?mimetype=application/font-woff"
            },
            {
                test: /\.(ttf|eot|svg|gif)(\?v=[0-9].[0-9].[0-9])?$/,
                loader: "file-loader?name=[name].[ext]"
            }
        ]
    },
    plugins: [
        new webpack.HotModuleReplacementPlugin()
    ],
    resolve: {
        // Allows require('file') instead of require('file.js|x')
        extensions: ['', '.js', '.jsx', '.json']
    }
};
