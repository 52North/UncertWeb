var http = require('./http');
var Style = require('./style');

function Visualization(parent, options) {
  this.id = options.id;
  this.minValue = options.minValue;
  this.maxValue = options.maxValue;
  this.params = options.params;
  this.uom = options.uom;
  this.visualizer = parent;
  this.dataset = parent.dataset;
  this.reference = options.reference;
  this.href = this.dataset.href + '/visualizations/' + this.id;
  this.dataset.addVisualization(this);
}

Visualization.prototype.getId = function() {
  return this.id;
};

Visualization.prototype.getMinValue = function() {
  return this.minValue;
};

Visualization.prototype.getMaxValue = function() {
  return this.maxValue;
};

Visualization.prototype.getParameters = function() {
  return this.params;
};

Visualization.prototype.getUnitOfMeasurement = function() {
  return this.uom;
};

Visualization.prototype.getVisualizer = function() {
  return this.visualizer;
};

Visualization.prototype.getDataset = function() {
  return this.dataset;
};

Visualization.prototype.getReference = function() {
  return this.reference;
};

Visualization.prototype.getResource = function() {
  return this.dataset.getResource();
};

Visualization.prototype.getStyles = function() {
  return http.get({
    url: this.href + '/styles',
    headers: {
      'Accept': 'application/vnd.org.uncertweb.viss.visualization-style.list+json'
    }
  }).then(function(response) {
    return response.styles;
  }).then((function(styles) {
    return Promise.all(styles.map((function(style) {
      return http.get({
        url: style.href,
        headers: {
          'Accept': 'application/vnd.org.uncertweb.viss.visualization-style+json'
        }
      }).then((function(style) {
        return http.get({
          url: style.href,
          headers: { 'Accept': 'application/vnd.ogc.sld+xml' },
          type: 'text'
        }).then((function(sld) {
          return new Style(this, style.id, sld);
        }).bind(this));
      }).bind(this));
    }).bind(this)));
  }).bind(this));
};

Visualization.prototype.addStyle = function(sld) {
  return http.post({
    url: this.href + '/styles',
    headers: {
      'Accept': 'application/vnd.org.uncertweb.viss.visualization-style+json',
      'Content-Type': 'application/vnd.ogc.sld+xml'
    },
    body: sld
  }).then((function(response) {
    return new Style(this, response.id, sld);
  }).bind(this));
};

Visualization.prototype.deleteAllStyles = function() {
  return this.getStyles().then(function(styles) {
    return styles.map(function(style) {
      return style.delete();
    });
  }).then(Promise.all);
};

Visualization.prototype.delete = function() {
  return http.del({url: this.href})
    .then((function(){ return this; }).bind(this));
};

module.exports = Visualization;