var http = require('./http');
var util = require('./util');
var Resource = require('./resource');
var Promise = require('any-promise');
var fs = require('fs');

function Client(endpoint) {
  this.href = endpoint + '/resources';
}

Client.prototype.createResourceFromReference = function(reference, mimeType) {
  var body = {
    responseMediaType: mimeType,
    url: reference.url,

  };
  if (reference.request) {
    body.request = reference.request.content;
    body.requestMediaType = reference.request.contentType;
  }
  if (reference.method) {
    body.method = reference.method;
  } else if (reference.request) {
    body.method = 'POST';
  } else {
    body.method = 'GET';
  }

  return http.post({
    url: this.href,
    headers: {
      'Accept':'application/vnd.org.uncertweb.viss.resource+json',
      'Content-Type': 'application/vnd.org.uncertweb.viss.request+json'
    },
    body: body
  }).then(function(response) {
    return this.getResource(response.id);
  }.bind(this));
};

Client.prototype.createResourceFromFile = function(file, mimeType) {
  return this.createResource(fs.createReadStream(file), mimeType);
};

Client.prototype.createResource = function(content, mimeType) {
  return http.post({
    url: this.href,
    headers: {
      'Accept':'application/vnd.org.uncertweb.viss.resource+json',
      'Content-Type': mimeType
    },
    body: content
  }).then(function(response) {
    return this.getResource(response.id);
  }.bind(this));
};

Client.prototype.getResource = function(id) {
  return http.get({
    url: this.href + '/' + id,
    headers: { 'Accept': 'application/vnd.org.uncertweb.viss.resource+json' }
  }).then(function(response) {
    return new Resource(this, response);
  }.bind(this));
};

Client.prototype.getResources = function() {
  return http.get({
    url: this.href,
    headers: { 'Accept': 'application/vnd.org.uncertweb.viss.resource.list+json' }
  })
  .then(util.f.property('resources'))
  .then(function(response) {
    return resources.map(function(resource) {
      return this.getResource(resource.id);
    }.bind(this));
  }.bind(this))
  .then(Promise.all.bind(Promise));
};

module.exports = Client;