
module.exports = {
  read: function read(xml) {
    return new DOMParser().parseFromString(xml, 'application/xml');
  },
  write: function write(doc) {
    if (doc.xml) {
      return doc.xml;
    } else {
      return new XMLSerializer().serializeToString(doc);
    }
  }
};