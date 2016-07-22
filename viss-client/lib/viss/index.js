var Client = require('./client');

module.exports = function(url) {
	return new Client(url);
};

module.exports.Client        = Client;
module.exports.Resource      = require('./resource');
module.exports.Dataset       = require('./dataset');
module.exports.Visualizer    = require('./visualizer');
module.exports.Visualization = require('./visualization');
module.exports.Style         = require('./style');
module.exports.Symbolizer    = require('./symbolizer');
