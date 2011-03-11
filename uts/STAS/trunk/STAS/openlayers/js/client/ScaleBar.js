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

OpenLayers.ScaleBar = OpenLayers.Class({
	CLASS_NAME: "OpenLayers.ScaleBar",
	propertyName: "resultValue",
	lessThanZeroColor: "#000000",
	multiFeatureStrokeColor: "#444444",
	width: null,
	height: null,
	defaultStyle: OpenLayers.Util.applyDefaults({
		"pointRadius": 4,
		"fill": true,
		"fillOpacity": 0.8,
		"stroke": true,
		"strokeWidth": 1,
		"strokeDashstyle": "solid",
		"strokeOpacity": 1,
	}, OpenLayers.Feature.Vector.style["default"]),
	/* callback functions */
	getNumIntervals: null,
	getThreshold: null,
	setLegendHtml: null,
	minHue: 90,
	maxHue: 15,
	thresholdHue: 0,
	style: null,
	/* actual valid values */
	threshold: null,
	numIntervals: null,
	initialize: function (options) {
		OpenLayers.Util.extend(this, options);
		this.writeLegend();
	},
	getColorForResultValue: function (value) {
		var result;
		if (this.getNumIntervals() <= 1) {
			result = new OpenLayers.Color.RGB(0,0,0).toHex();
		} else if (value >= this.getThreshold()) {
			result = new OpenLayers.Color.HSV(this.thresholdHue, 100, 100)
				.toRGB().toHex();
		} else {
			var hue= (this.minHue - this.getIntervalNumForResultValue(value) 
				* ((this.minHue - this.maxHue)/(this.getNumIntervals() - 1)));
			result = new OpenLayers.Color.HSV(hue, 100, 100).toRGB().toHex();
		}
		return result;
	},
	getIntervalNumForResultValue: function (value) {
		return Math.floor(value/(this.getThreshold()
					/(this.getNumIntervals()-1)));
	},

	writeLegend: function () {
		var threshold = this.getThreshold();
		var numIntervals = this.getNumIntervals();	
		if (threshold != this.threshold || numIntervals != this.numIntervals) {

			this.threshold = threshold;
			this.numIntervals = numIntervals;
			
			var html = "";
			var width = Math.floor((this.width)/numIntervals);

			for (var i = 0; i < (numIntervals-1); i++) {
					value = i*(threshold/(numIntervals-1));
					html += '<span style="width:' + width + 'px;'
									   + 'height:' + this.height + ';'
									   + 'background-color:' 
									   + this.getColorForResultValue(value) + ';'
									   + '">&ensp;' + value.toFixed(1) 
																	+ '</span>';
			}
			takenSize = (numIntervals-1) * width;
			html += '<span style="width:' + (this.width-takenSize) + 'px;'
				+ 'height:' + this.height + ';'
				+ 'background-color:' + this.getColorForResultValue(threshold) 
				+ ';">&ensp;' + threshold.toFixed(1)+'</span>';
			this.setLegendHtml(html);
			this.style = null; /* regenerate style */
		}
	},

	getStyle: function () {
		var threshold = this.getThreshold();
		var numIntervals = this.getNumIntervals();	
		if (!this.style || threshold != this.threshold 
						|| numIntervals != this.numIntervals) {
			this.threshold = threshold;
			this.numIntervals = numIntervals;
			this.style = new OpenLayers.Style();
			var rules = [];
			var intervalSize = threshold/(numIntervals-1);
			rules.push(this.getRule(this.lessThanZeroColor,undefined,0,true)); 
			rules.push(this.getRule(this.lessThanZeroColor,undefined,0,false)); 
			for (var i = 0; i < (numIntervals-1); i++) {
				var lower = i * intervalSize;
				rules.push(this.getRule(this.getColorForResultValue(lower),
							lower, lower + intervalSize, false));
				rules.push(this.getRule(this.getColorForResultValue(lower), 
							lower, lower + intervalSize, true));
			}
			rules.push(this.getRule(this.getColorForResultValue(threshold), 
						threshold, undefined, true)); 
			rules.push(this.getRule(this.getColorForResultValue(threshold), 
						threshold, undefined, false)); 
			this.style.addRules(rules);
		}
		return this.style;
	},
	getRule: function (color, lower, upper, multi) {
		var filters = [];
		if (upper) {
			filters.push(new OpenLayers.Filter.Comparison({
				type: OpenLayers.Filter.Comparison.LESS_THAN,
				property: this.propertyName,
				value: upper
			}));
		}
		if (lower) {
			filters.push(new OpenLayers.Filter.Comparison({
				type: OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO,
				property: this.propertyName,
				value: lower
			}));
		}
		filters.push(new OpenLayers.Filter.Comparison({
			type: OpenLayers.Filter.Comparison.EQUAL_TO,
			property: "isMultiFeature",
			value: multi
		}));
		return new OpenLayers.Rule({
			filter: new OpenLayers.Filter.Logical({
				type: OpenLayers.Filter.Logical.AND,
				filters: filters
			}),
			symbolizer: OpenLayers.Util.applyDefaults({
				"color": color,
				"strokeColor": (multi) ? this.multiFeatureStrokeColor : color,
				"fillColor": color
			}, this.defaultStyle)
		});
	}
});
