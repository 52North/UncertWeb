var http = require('./http');
var util =require('./util');
var Dataset = require('./dataset');
var Promise = require('any-promise');

function Resource(parent, options) {
  this.id = options.id;
  this.mimeType = options.mimeType;
  this.href = parent.href + '/' + this.id;

  this.datasets = this.requestDatasets()
    .then(function(datasets) {
      return util.byProperty('id', datasets);
    });
}

Resource.prototype.getMimeType = function() {
  return this.mimeType;
};

Resource.prototype.getId = function() {
  return this.id;
};

Resource.prototype.getDataset = function(id) {
  return this.datasets.then(function(datasets) {
    return datasets[id];
  });
};

Resource.prototype.getDatasets = function() {
  return this.datasets.then(util.toArray);
};

Resource.prototype.requestDatasets = function() {
  return http.get({
    url: this.href + '/datasets',
    headers: { 'Accept': 'application/vnd.org.uncertweb.viss.dataset.list+json' }
  }).then(function(response) {
    return response.dataSets.map(function(dataset) {
      return this.requestDataset(dataset.id);
    }.bind(this));
  }.bind(this))
  .then(Promise.all.bind(Promise));
};

Resource.prototype.requestDataset = function(id) {
  return http.get({
    url: this.href + '/datasets/' + id,
    headers: { 'Accept': 'application/vnd.org.uncertweb.viss.dataset+json' }
  }).then(function(response) {
    return new Dataset(this, response);
  }.bind(this));
};

Resource.prototype.delete = function() {
  return http.del({ url: this.href })
    .then(util.f.constant(this));
};


module.exports = Resource;