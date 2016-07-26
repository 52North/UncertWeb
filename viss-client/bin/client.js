#!/usr/bin/env node

process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

var Viss = require('../lib/viss');
var color = require('color');

var viss = Viss('http://localhost:8080/viss');

var path = '/home/autermann/Source/uncertweb/greenland/data/netCDF/biotemperature_normalDistr.nc';

function createSymbolizer(visualization) {
  return new Viss.Symbolizer({
    minValue: visualization.getMinValue(),
    maxValue: visualization.getMaxValue(),
    numIntervals: 5,
    minColor: color("green"),
    maxColor: color("red")
  });
}

viss.createResourceFromFile(path, 'application/netcdf')
  .then(function(resource) {
    return resource.getDatasets();
  })
  .then(function(datasets) {
    return Promise.all(datasets.map(function(dataset) {
      return dataset.getVisualizer('Distribution-Normal-Mean')
        .then(function(visualizer) {
          return visualizer.execute({});
        });
    }));
  })
  .then(function(visualizations) {

    return Promise.all(visualizations.map(function(visualization) {
      var symbolizer = createSymbolizer(visualization);
      return visualization.deleteAllStyles().then(function() {
        return visualization.addStyle(symbolizer.toStyledLayerDescriptor());
      });
    }));
  })
  .then(function(styles) {
  })
  .catch(console.error.bind(console));

