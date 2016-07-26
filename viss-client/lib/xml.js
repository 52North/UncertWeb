var xmldom = require('xmldom');

module.exports = {
  read: function read(xml) {
    return new xmldom.DOMParser().parseFromString(xml, 'application/xml');
  },
  write: function write(doc) {
    return new xmldom.XMLSerializer().serializeToString(doc);
  }
};