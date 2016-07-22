
var http = require('./http');
var util = require('./util');

function Style(visualization, id, sld) {
  this.visualization = visualization;
  this.id = id;
  this.sld = sld;
  this.href = this.visualization.href + '/styles/' + this.id;
}

Style.prototype.getId = function() {
  return this.id;
};

Style.prototype.getVisualizer = function() {
  return this.visualization.getVisualizer();
};

Style.prototype.getVisualization = function() {
  return this.visualization;
};
Style.prototype.getDataset = function() {
  return this.visualization.getDataset();
};

Style.prototype.getResource = function() {
  return this.visualization.getResource();
};

Style.prototype.delete = function() {
  return http.del({ url: this.href }).then(util.constant(this));
};

Style.prototype.update = function(sld) {
  return http.put({
    url: this.href,
    headers: {
      'Accept': 'application/vnd.org.uncertweb.viss.visualization-style+json',
      'Content-Type': 'application/vnd.ogc.sld+xml'
    },
    body: sld
  }).then(util.constant(this));
};

module.exports = Style;