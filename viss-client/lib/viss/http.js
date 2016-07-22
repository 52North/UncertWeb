var popsicle = require('popsicle');
var XML = require('./xml');

var XML_MIME_TYPE_REXP = /^application\/(?:[\w!#\$%&\*`\-\.\^~]*\+)?xml$/;
var JSON_MIME_TYPE_REXP = /^application\/(?:[\w!#\$%&\*`\-\.\^~]*\+)?json$/;


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
    if (request.method === 'DELETE')
      console.log(response.status);
    return response;
  });
}

function request(options) {
  console.log(options.method, options.url);
  return popsicle.request(options)
    .use(popsicle.plugins.stringify('json'))
    .use(parse)
    .then(function(response) {
      return response.body;
    });
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