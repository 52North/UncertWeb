var color = require('color');
var sld = require('./sld');

function Symbolizer(options) {
  this.outOfBoundsColor = color("white");
  this.opacity = ('opacity' in options) ? options.opacity : 1.0;
  this.numIntervals = options.numIntervals;

  this.minValue = options.minValue;
  this.maxValue = options.maxValue;
  this.minColor = options.minColor || color("white");
  this.maxColor = options.maxColor || color("black");
  this.intervals = new Array(this.numIntervals);
  this.intervalSize = (this.maxValue - this.minValue) / this.numIntervals;
  for (var i = 0; i < this.numIntervals; ++i) {
    this.intervals[i] = [
      this.minValue + i * this.intervalSize,
      this.minValue + (i + 1) * this.intervalSize
    ];
  }
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
  var min = this.minColor.hsvArray();
  var max = this.maxColor.hsvArray();
  var segment = Math.floor((value - this.minValue) / this.intervalSize);
  var numIntervals = this.numIntervals;
  return color().hsv(min.map(function(_, index) {
    return min[index] + (segment * (max[index]-min[index])/numIntervals);
  }));
};

Symbolizer.prototype.createColorMap = function() {
  var ints = this.getIntervals();
  var entries = new Array(ints.length + 2);

  // everything below this.minValue
  entries[0] = {
    color: this.outOfBoundsColor,
    opacity: 0,
    quantity: this.getMinValue()
  };

  ints.forEach(function(interval, idx) {
    entries[idx+1] = {
      color: this.getColor(interval[0]),
      quantity: interval[1],
      opacity: this.getOpacity()
    };
  }.bind(this));

  // everything above this.maxValue
  entries[entries.length-1] = {
    color: this.outOfBoundsColor,
    opacity: 0,
    quantity: Number.MAX_VALUE
  };

  return entries;
};

module.exports = Symbolizer;

