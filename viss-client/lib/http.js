var popsicle = require('popsicle');
var XML = require('./xml');

var XML_MIME_TYPE_REXP = /^application\/(?:[\w!#\$%&\*`\-\.\^~]*\+)?xml$/;
var JSON_MIME_TYPE_REXP = /^application\/(?:[\w!#\$%&\*`\-\.\^~]*\+)?json$/;

var logger = (function() {
  function logRequest(id, request) {
    var prefix = '>' + id;
    console.log(prefix, request.method, request.url);
    logHeadersAndBody(prefix, request);
  }

  function logResponse(id, response) {
    var prefix = '<' + id;
    console.log(prefix, response.status, response.statusText);
    logHeadersAndBody(prefix, response);
  }

  function logHeadersAndBody(prefix, x) {
  var headers = x.headers;
    for (var header in headers) {
      console.log(prefix, header + ':', headers[header]);
    }
    console.log(prefix);
    if (x.body && typeof x.body == 'string') {
      x.body.split('\n').forEach(function(line) {
        console.log(prefix, line);
      });
    }
    console.log();

  }

  var count = 0;

  return function(request, next) {
    var id = count++;
    logRequest(id, request);
    return next().then(function(response) {
        logResponse(id, response);
        return response;
    });
  };
})();

function parse(request, next) {
  return next().then(function(response) {
    var responseType = response.type();
    if (response.body === '') {
      response.body = null;
    } else if (XML_MIME_TYPE_REXP.test(responseType)) {
        response.body = XML.read(response.body);
    } else if (JSON_MIME_TYPE_REXP.test(responseType)) {
        response.body = JSON.parse(response.body);
    }
    return response;
  });
}

function throwing(request, next) {
  return next().then(function(response) {
    if (response.status >= 400) {
      return Promise.reject(new Error(response.status + " " + response.statusText));
    }
    return response;
  });
}

function request(options) {
  options.headers = options.headers || {};
  options.headers['User-Agent'] = 'viss-client';
  return popsicle.request(options)
    .use(popsicle.plugins.stringify('json'))
    .use(parse)
    .use(logger)
    .use(throwing)
    .then(function(response) { return response.body; });
}

request.get = function get(options) {
  options.method = 'GET';
  return request(options);
};

request.del = function del(options) {
  options.method = 'DELETE';
  return request(options);
};

request.post = function post(options) {
  options.method = 'POST';
  return request(options);
};

module.exports = request;