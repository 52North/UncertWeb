var XML = require('./xml');

function SLDEncoder() {
  this.namespace = 'http://www.opengis.net/sld';
  this.prefix = 'sld';
}

SLDEncoder.prototype.createElemFunction = function(doc, prefix, namespace) {
  return function elem(name, attributes, content) {
    var element;
    if (namespace) {
      if (prefix) {
        element = doc.createElementNS(namespace, prefix + ':' + name);
      } else {
        element = doc.createElementNS(namespace, name);
      }
    } else {
      element = doc.createElement(name);
    }
    if (attributes) {
      for (var key in attributes) {
        if (attributes[key] !== null && attributes[key] !== undefined) {
          element.setAttribute(key, attributes[key]);
        }
      }
    }
    if (content !== null && content !== undefined) {
      element.textContent = content;
    }
    return element;
  };
};

SLDEncoder.prototype.create = function(colorMap) {
  var doc = this.createDocument(this.prefix, this.namespace, 'StyledLayerDescriptor');
  var elem = this.createElemFunction(doc, this.prefix, this.namespace);

  var namedLayerNode = doc.documentElement.appendChild(elem('NamedLayer'));
  namedLayerNode.appendChild(elem('Name', null, 'defaultStyle'));

  var colorMapNode = namedLayerNode
    .appendChild(elem('UserStyle'))
    .appendChild(elem('FeatureTypeStyle'))
    .appendChild(elem('Rule'))
    .appendChild(elem('RasterSymbolizer'))
    .appendChild(elem('ColorMap', { type: 'intervals' }));

  colorMap.forEach(function(entry) {
    colorMapNode.appendChild(elem('ColorMapEntry', {
      color: entry.color.hexString(),
      opacity: entry.opacity,
      quantity: entry.quantity
    }));
  });
  return XML.write(doc);
};

SLDEncoder.prototype.createDocument = function(prefix, namespace, name) {
  var xml = '<';
  if (namespace) {
    if (prefix) {
      xml +=  prefix + ':' + name + ' xmlns:' + prefix + '="' + namespace + '"';
    } else {
      xml += name + ' xmlns="' + namespace + '"';
    }
  } else {
    xml += name ;
  }
  xml += '/>';
  return XML.read(xml);

};

var instance = new SLDEncoder();

module.exports = function(symbolizer) {
  return instance.create(symbolizer);
};

