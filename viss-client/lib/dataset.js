var http = require('./http');
var util = require('./util');
var Visualizer = require('./visualizer');
var Promise = require('any-promise');

function Dataset(parent, options) {
  this.id = options.id;
  this.resource = parent;
  this.phenomenon = options.phenomenon;
  this.type = options.type;
  this.temporalExtent = options.temporalExtent;
  this.spatialExtent = options.spatialExtent;
  this.visualizations = options.visualizations;
  this.href = this.resource.href + '/datasets/' + this.id;
  this.visualizers = this.requestVisualizers().then(function(visualizers) {
    return util.byProperty('id', visualizers);
  });
}

Dataset.prototype.getUncertaintyType = function() {
  return this.id;
};

Dataset.prototype.getResource = function() {
  return this.resource;
};

Dataset.prototype.getType = function() {
  return this.type;
};

Dataset.prototype.getPhenomenon = function() {
  return this.phenomenon;
};

Dataset.prototype.getTemporalExtent = function() {
  return this.temporalExtent;
};

Dataset.prototype.isTimeEnabled = function() {
  return this.temporalExtent !== undefined && this.temporalExtent !== null;
};

Dataset.prototype.getVisualizations = function() {
  return this.visualizations;
};

Dataset.prototype.addVisualization = function(visualization) {
  this.visualizations.push(visualization);
};

Dataset.prototype.getSpatialExtent = function() {
  return this.spatialExtent;
};

Dataset.prototype.getVisualizer = function(id) {
  return this.visualizers.then(util.f.property(id));
};

Dataset.prototype.getVisualizers = function() {
  return this.visualizers.then(util.toArray);
};

Dataset.prototype.requestVisualizer = function(id) {
  return http.get({
    url: this.href + '/visualizers/' + id,
    headers: { 'Accept': 'application/vnd.org.uncertweb.viss.visualizer+json' }
  }).then(function(response) {
    return new Visualizer(this, response);
  }.bind(this));
};

Dataset.prototype.requestVisualizers = function() {
  return http.get({
    url: this.href + '/visualizers',
    headers: { 'Accept': 'application/vnd.org.uncertweb.viss.visualizer.list+json' }
  })
  .then(util.f.property('visualizers'))
  .then(function(visualizers) {
    return visualizers.map(function(visualizer) {
      return this.requestVisualizer(visualizer.id);
    }.bind(this));
  }.bind(this))
  .then(Promise.all.bind(Promise));
};

Dataset.prototype.getValue = function(lon, lat, epsgCode) {
  if (epsgCode == 900913) { epsgCode = 3857; }
  return http.post({
    url: this.href + '/value',
    headers : {
      'Accept': "application/vnd.ogc.om+json",
      'Content-Type': 'application/vnd.org.uncertweb.viss.value-request+json',
    },
    body: createPoint(lon, lat, epsgCode)
  });
};

Dataset.prototype.getTimeExtents = function() {
  var te;
  var i;
  var extents = [];

  function addInstant(instant) {
    var date = new Date(te.instant);
    extents.push([ date.getTime(), date.getTime() ]);
  }

  if (this.temporalExtent) {
    te = this.temporalExtent;
      if (te.instant) {
        addInstant(te.instant);

        } else if (te.begin && te.end) {
            var beginDate = new Date(te.begin);
            var endDate = new Date(te.end);
            var interval = te.intervalSize || 0;
            var separator = te.seperator || 0;
            var time;

            if (interval !== 0 || separator !== 0) {
                // TODO types
                for (time = beginDate.getTime();
                     time <= endDate.getTime();
                     time += interval + separator) {
                    extents.push([ time, time + interval ]);
                }
            }
        } else if (te.intervals) {
            for (i = 0; i < te.intervals.length; ++i) {
                extents.push([
                  new Date(te.intervals.begin).getTime(),
                  new Date(te.intervals.end).getTime()
                ]);
            }
        } else if (te.instants) {
            for (i = 0; i < te.instants.length; ++i) {
              addInstant(te.instants[i]);
            }
        }
  }
  return extents;
};

function createPoint(lon, lat, epsgCode) {
  return {
    type : 'Point',
    coordinates : [ lon, lat ],
    crs : {
      type : 'name',
      properties : {
        name : 'http://www.opengis.net/def/crs/EPSG/0/' + epsgCode
      }
    }
  };
}

module.exports = Dataset;