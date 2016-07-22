
var Reader = (typeof DOMParser     === 'undefined') ? require('xmldom').DOMParser     : DOMParser;
var Writer = (typeof XMLSerializer === 'undefined') ? require('xmldom').XMLSerializer : XMLSerializer;

module.exports = {
  read: function read(xml) {
    return new Reader().parseFromString(xml, 'application/xml');
  },
  write: function write(doc) {
    if (doc.xml) {
      return doc.xml;
    } else {
      return new Writer().serializeToString(doc);
    }
  }
};