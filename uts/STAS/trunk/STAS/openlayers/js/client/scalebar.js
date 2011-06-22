/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
OpenLayers.Color = {
	RGB: OpenLayers.Class({
		CLASS_NAME: 'OpenLayers.Color.RGB',
		HEX_DIGITS: '0123456789ABCDEF',
		
		initialize: function (red, green, blue) {
			this.r = red;
			this.g = green;
			this.b = blue;
		},
		
		toHex: function () {
			return '#' + this.hexify(this.r) 
					   + this.hexify(this.g) 
					   + this.hexify(this.b);
		},
		
		hexify: function (number) {
			var lsd = number % 16;
			var msd = (number - lsd) / 16;	
			return this.HEX_DIGITS.charAt(msd) + this.HEX_DIGITS.charAt(lsd);
		}
	}),
	
	HSV: OpenLayers.Class({
		CLASS_NAME: 'OpenLayers.Color.HSV',
		
		initialize: function (hue, sat, val) {
			this.h = hue;
			this.s = sat;
			this.v = val;
		},
		
		toRGB: function () {
			var h = this.h / 360;
			var s = this.s / 100;
			var v = this.v / 100;
			var r, g, b;
			if (s === 0) {
				r = g = b = v;
			} else {
				var h6, i, x, y, z;
				h6 = h * 6;
				i = Math.floor(h6);
				x = v * (1 - s);
				y = v * (1 - s * (h6 - i));
				z = v * (1 - s * (1 - (h6 - i)));
				switch (i) {
				case 0: r = v; g = z; b = x; break;
				case 1: r = y; g = v; b = x; break;
				case 2: r = x; g = v; b = z; break;
				case 3: r = x; g = y; b = v; break;
				case 4: r = z; g = x; b = v; break;
				case 5: r = v; g = x; b = y; break;
				}
			}
			return new OpenLayers.Color.RGB(r * 255, g * 255, b * 255);
		}
	})
};

OpenLayers.SOS = OpenLayers.SOS || {};
OpenLayers.SOS.ScaleBar = OpenLayers.Class({
	CLASS_NAME: 'OpenLayers.SOS.ScaleBar',
	propertyName: 'resultValue',
	color: { 
		less: '#000000', 
		more: '#FF0000', 
		mult: '#444444' 
	},
	hue: { 
		min: 90, 
		max: 15 
	},
	defaultStyle: OpenLayers.Util.applyDefaults({
		'pointRadius': 4, 
		'fill': true,
		'fillOpacity': 0.8, 
		'stroke': true,
		'strokeWidth': 1, 
		'strokeOpacity': 1,
		'strokeDashstyle': 'solid'
	}, OpenLayers.Feature.Vector.style['default']),
	setLegendHtml: function(html){},
	width: null, 
	height: null, 
	style: null,
	max: null, 
	min: null, 
	ints: null, 
	uom: null,
	
	initialize: function (options) {
		OpenLayers.Util.extend(this, options);
		this.writeLegend();
	},
	
	getColor: function (value) {
		if (this.ints <= 1 || value < this.min) {
			return this.color.less;
		} 
		if (value >= this.max) {
			return this.color.more;
		} 
		var valueIntervalSize = (this.max - this.min)/(this.ints-1);
		var hueIntervalSize = (this.hue.min - this.hue.max)/(this.ints-1);
		var segment = Math.floor((value - this.min)/valueIntervalSize);
		var hue = this.hue.min - segment * hueIntervalSize;
		return new OpenLayers.Color.HSV(hue, 100, 100).toRGB().toHex();
	},
	
	update: function (v) {
		if (v.ints) this.ints = parseInt(v.ints);
		if (v.max != undefined && v.max != null) this.max = parseFloat(v.max);
		if (v.min != undefined && v.min != null) this.min = parseFloat(v.min);
		if (v.uom) this.uom = v.uom; else this.uom = null;
		if (v.propertyName) this.propertyName = v.propertyName;
		this.writeLegend();
	},
	
	getUom: function() {
		return this.uom;
	},
	
	getMax: function() {
		return this.max;
	},
	
	getMin: function() {
		return this.min;
	},
	
	getInts: function() {
		return this.ints;
	},
	
	writeLegend: function () {
		var html = '';
		var width = Math.floor(this.width/this.ints);
		var valueWidth = (this.max - this.min)/(this.ints - 1);
		for (var i = 0; i < (this.ints - 1); i++) {
			var value = this.min + i * valueWidth;
			html += '<span style="width:' + width + 'px;'
			+ 'height:' + this.height + ';'
			+ 'background-color:' 
			+ this.getColor(value) + ';'
			+ '">&ensp;' + value.toFixed(1) + ((this.uom) ? ' ' + this.uom : '')
			+ '</span>';
		}
		takenSize = (this.ints-1) * width;
		html += '<span style="'
		+ 'width:' + (this.width-takenSize) + 'px;'
		+ 'height:' + this.height + ';' 
		+ 'background-color:' + this.getColor(this.max) + ';'
		+ '">&ensp;' + this.max.toFixed(1) + ((this.uom) ? ' ' + this.uom  : '') + '</span>';
		this.setLegendHtml(html);
		this.style = null; /* regenerate style */
	},
	
	getStyle: function () {
		var self = this;
		function getRule(color, lower, upper, multi) {
			var filters = [];
			if (upper === 0 || upper) {
				filters.push(new OpenLayers.Filter.Comparison({
					type: OpenLayers.Filter.Comparison.LESS_THAN,
					property: self.propertyName,
					value: upper
				}));
			}
			if (lower === 0  || lower) {
				filters.push(new OpenLayers.Filter.Comparison({
					type: OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO,
					property: self.propertyName,
					value: lower
				}));
			}
			filters.push(new OpenLayers.Filter.Comparison({
				type: OpenLayers.Filter.Comparison.EQUAL_TO,
				property: 'isMultiFeature',
				value: multi
			}));
			return new OpenLayers.Rule({
				filter: new OpenLayers.Filter.Logical({
					type: OpenLayers.Filter.Logical.AND,
					filters: filters
				}),
				symbolizer: OpenLayers.Util.applyDefaults({
					'color': color,
					'strokeColor': (multi) ? self.color.mult : color,
					'fillColor': color
				}, self.defaultStyle)
			});
		}
		if (!this.style) {
			this.style = new OpenLayers.Style();
			var rules = [];
			var intervalSize = (this.max-this.min)/(this.ints-1);
			rules.push(getRule(this.color.less, undefined, this.min, true)); 
			rules.push(getRule(this.color.less, undefined, this.min, false));
			for (var i = 0; i < (this.ints-1); i++) {
				var lower = this.min + (i * intervalSize);
				rules.push(getRule(this.getColor(lower), lower, lower + intervalSize, false));
				rules.push(getRule(this.getColor(lower), lower, lower + intervalSize, true));
			}
			rules.push(getRule(this.getColor(this.max), this.max, undefined, true)); 
			rules.push(getRule(this.getColor(this.max), this.max, undefined, false)); 
			this.style.addRules(rules);
		}
		return this.style;
	}
	
});
/* vim: set ts=4 sts=4 sw=4 noet ft=javascript fenc=utf-8 */
