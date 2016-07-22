var http = require('./http');
var Visualization = require('./visualization');

function Visualizer(parent, options) {
  this.dataset = parent;
  this.id = options.id;
  this.options = options.options;
  this.supportedUncertainties = options.supportedUncertainties;
  this.description = options.description;
  this.href = this.dataset.href  + '/visualizers/' + this.id;
}

Visualizer.prototype.getDataset = function() {
  return this.dataset;
};

Visualizer.prototype.getResource = function() {
  return this.dataset.getResource();
};

Visualizer.prototype.getOptions = function() {
  return this.options;
};

Visualizer.prototype.getDescription = function() {
  return this.description;
};

Visualizer.prototype.getSupportedUncertainties = function() {
  return this.supportedUncertainties;
};

Visualizer.prototype.getId = function() {
  return this.id;
};

Visualizer.prototype.execute = function(parameters) {
  try {
    this.checkParameters(parameters);
  } catch (err) {
    return Promise.reject(err);
  }

  return http.post({
    url: this.href,
    headers : {
      'Accept': 'application/vnd.org.uncertweb.viss.visualization+json',
      'Content-Type' : 'application/vnd.org.uncertweb.viss.create+json'
    },
    body: parameters
  }).then((function(response) {
    return new Visualization(this, response);
  }).bind(this));
};

Visualizer.prototype.checkParameters = function(p) {
  var k;
  var option;

  for (k in this.options) {
    option = this.options[k];
    if (p[k] === null || p[k] === undefined) {
      if (option.default !== null && option.default !== undefined) {
        p[k] = option.default;
      } else if (option.required) {
        throw new Error('Missing parameter ' + k);
      }
    }
  }

  for (k in p) {
    if (!(k in this.options)) {
      throw new Error('Unknown parameter ' + k);
    }
  }
};

module.exports = Visualizer;

