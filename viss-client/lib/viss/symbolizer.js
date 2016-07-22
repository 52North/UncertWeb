var color = require('../color');
var SVG = require('../color/names/svg');
var sld = require('./sld');

function Symbolizer(options) {
  this.outOfBoundsColor = SVG.white;

  this.opacity = ('opacity' in options) ? options.opacity : 1.0;
  this.numIntervals = options.numIntervals;

  this.minValue = options.minValue;
  this.maxValue = options.maxValue;
  this.minColor = (options.minColor || SVG.white).toHSV();
  this.maxColor = (options.maxColor || SVG.black).toHSV();
  console.log(this.minColor, this.maxColor);
  this.intervals = new Array(this.numIntervals);
  this.intervalSize = (this.maxValue - this.minValue) / this.numIntervals;
  console.log(this.minValue, this.maxValue);
  for (var i = 0; i < this.numIntervals; i++) {
    this.intervals[i] = [
      this.minValue + i * this.intervalSize,
      this.maxValue + (i + 1) * this.intervalSize
    ];
  }
  console.log(this.intervals);
  this.colorMap = this.createColorMap();
}

Symbolizer.prototype.getColorMap = function() {
  return this.colorMap;
};

Symbolizer.prototype.getOpacity = function() {
  return this.opacity;
};

Symbolizer.prototype.toStyledLayerDescriptor = function() {
  return sld(this.getColorMap());
};

Symbolizer.prototype.getIntervalSize = function() {
  return this.intervalSize;
};

Symbolizer.prototype.getNumIntervals = function() {
  return this.numIntervals;
};

Symbolizer.prototype.getMinValue = function() {
  return this.minValue;
};

Symbolizer.prototype.getMaxValue = function() {
  return this.maxValue;
};

Symbolizer.prototype.getIntervals = function() {
  return this.intervals;
};

Symbolizer.prototype.getInterval = function(value) {
  var idx = this.getIntervalIndex();
  return idx < 0 ? null : this.intervals[idx];
};

Symbolizer.prototype.getIntervalIndex = function(value) {
  var lowerIndex = 0;
  var higherIndex = this.intervals.length - 1;
  if (this.intervals[higherIndex][0] <= val) {
    lowerIndex = higherIndex;
  } else {
    while ((higherIndex - lowerIndex) > 1) {
      var midIndex = Math.floor((lowerIndex + higherIndex) / 2);
      if (this.intervals[midIndex][0] <= val) {
        lowerIndex = midIndex;
      } else {
        higherIndex = midIndex;
      }
    }
  }
  if (val >= this.intervals[lowerIndex][0] &&
    val <= this.intervals[lowerIndex][1]) {
    return lowerIndex;
  } else {
    return -1;
  }
};

Symbolizer.prototype.getColor = function(value) {
  var min = this.minColor.toArray();
  var max = this.maxColor.toArray();
    var segment = Math.floor((value - this.minValue) / this.intervalSize);
  var numIntervals = this.numIntervals;
  return color.hsv.apply(null, min.map(function(_, index) {
    return min[index] + (segment * (max[index]-min[index])/numIntervals);
  })).toRGB();
};

Symbolizer.prototype.createColorMap = function() {
  var entries = [];

  // everything below this.minValue
  entries.push({
    color: this.outOfBoundsColor,
    opacity: 0,
    quantity: this.getMinValue()
  });

  var ints = this.getIntervals();
  for (var i = 0; i < ints.length; ++i) {
    var min = ints[i][0];
    var max = ints[i][1];
    entries.push({
      color: this.getColor(min),
      quantity: max,
      opacity: this.getOpacity()
    });
  }

  // everything above this.maxValue
  entries.push({
    color: this.outOfBoundsColor,
    opacity: 0,
    quantity: Number.MAX_VALUE
  });
  console.dir(entries);
  return entries;
};

module.exports = Symbolizer;

