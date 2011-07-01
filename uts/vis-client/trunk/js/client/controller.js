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
OpenLayers.SOS.Controller = OpenLayers.Class({
	CLASS_NAME: "OpenLayers.SOS.Controller",
	selectedConfInterval: 95.0,
	
	format: {
		jsom: new OpenLayers.SOS.Format.JSOM(),
		xml: new OpenLayers.SOS.Format.ObservationCollection()
	},
	
	callback: {
		ready:			function () {},
		fail: 			function () {},
		updateTime:		function () {},
		updateValues:	function () {},
		probabilityMode:function () {},
		exceedanceMode: function () {},
		standardMode:	function () {}
	},
	
	clients: [],
	scale: null,
	map: null,
	
	times: {
		selected: null,
		step: null,
		min: null,
		max: null
	},
	values: {
		min: null,
		max: null,
		uom: null,
		propertyName: 'resultValue',
		mode: "intervals",
		ints: null,
		threshold: null,
		clone: function () {
			var newObj = {};
			for (i in this) newObj[i] = this[i]; 
			return newObj;
		}
	},
	
	oldValues: null,
	mapContainsProbabilityLayer: false,
	
	initialize: function(scalebar,map,time,callbacks) {
		this.scale = scalebar; this.map = map;
		OpenLayers.Util.extend(this.values, {
			min: this.scale.getMin(),
			max: this.scale.getMax(),
			ints: this.scale.getInts()
		});
		OpenLayers.Util.extend(this.callback, callbacks);

		var self = this;
		
		this.map.events.register("changelayer", map, function(ev) {
			if (ev.layer.isProbabilityLayer) {
				if (!ev.layer.getVisibility()) {
					self.mapContainsProbabilityLayer = self._doesMapContainProbabilityLayers();
					if (!self.mapContainsProbabilityLayer) {
						self.switchToMode("intervals");
					}					
				} else if (!self.mapContainsProbabilityLayer) {
					self.mapContainsProbabilityLayer = true;
					self.switchToMode("probabilities");
				}
			} else if (self.mapContainsProbabilityLayer && ev.layer.getVisibility()) {
				self._deactivateProbabilityLayers();
			}
			var ls = self.map.getControlsByClass('OpenLayers.Control.LayerSwitcher');
			for (var i = 0; i < ls.length; i++) {
				ls[i].redraw();
			}
		});
	},
	
	addLayer: function(options) {
		var self = this;

		function createLayer(f) {
			self.clients.push(new OpenLayers.SOS.Client(self, f));
		}

		function generateFromJsom(j) {
			createLayer(self.format.jsom.read(j));
		}
		
		function generateFromXml(r) {
			if (r.responseXML) {
				createLayer(self.format.xml.read(r.responseXML));
			} else if (r.responseText) {
				createLayer(self.format.xml.read(r.responseText));
			} else {
				createLayer(self.format.xml.read(r));
			}
		}
				
		if (options.url) {
			if (options.request && options.request.trim()) {
				OpenLayers.Request.POST({
					url: options.url,
					data: options.request,
					success: generateFromXml,
					failure: this.callback.fail
				});
			} else {
				OpenLayers.Request.GET({
					url: options.url,
					success: generateFromXml,
					failure: this.callback.fail
				});
			}
		} else if (options.oc) { 
			generateFromXml(options.oc); 
		} else if (options.json) { 
			generateFromJsom(options.json); 
		}
	},
	
	updateForNewScale: function(min, max, ints) {
		if (min != undefined)
			this.values.min = min;
		if (max != undefined)
			this.values.max = max;
		if (ints)
			this.values.ints = ints;
		this.scale.update(this.values);
		$.each(this.clients, function (i, c){ 
			c.updateForNewScale(); 
		}); 
	},
	
	updateTimeRange: function(time){
		function gcd(u, v) {
			var k, d;
			if (u == 0 || v == 0) { 
				return u | v;
			}
			for (k = 0; ((u | v) & 1) == 0; ++k) {
				u >>= 1;
				v >>= 1;
			}
			while ((u & 1) == 0) {
				u >>= 1;
			}
			do {
				while ((v & 1) == 0) {
					v >>= 1; 
				}
				if (u < v) {
					 v -= u; 
				} else {
					d = u - v;
					u = v;
					v = d;
				}
				v >>= 1; 
			} while (v != 0);
			return u << k;
		}
		if (this.times.min == null || this.times.min > time.min) this.times.min = time.min;
		if (this.times.max == null || this.times.max < time.max) this.times.max = time.max;
		this.times.step = (this.times.step == null) ? time.step : gcd(time.step, this.times.step);
		this.callback.updateTime(this.times);
	},
	
	getVisibleScale: function() {
		return [this.values.min, this.values.max];
	},
	
	getVisualStyle: function() {
		return this.values.mode;
	},

	switchToMode: function(val, opts) {
		var self = this;
		
		function switchToPercantageMode(val) {
			if (self.oldValues == null) {
				self.oldValues = self.values.clone();
			}
			OpenLayers.Util.extend(self.values, {
				min: 0, max: 100, uom: '%', mode: val
			});
			self.scale.update(self.values);
			self.callback.updateValues(self.values);
			$.each(self.clients, function (i, c){ 
				c.updateForNewScale(); 
			}); 
		}
		
		if (val === 'bars' || val === 'intervals') {
			var prevMode = this.values.mode;
			if (this.oldValues != null) {
				this.values = this.oldValues;
				this.oldValues = null;
			}
			if (prevMode != 'probabilities') {
				this.values.mode = val;
			}
			this.scale.update(this.values);
			this.callback.updateValues(this.values);
			this.callback.standardMode();
			this.updateForNewScale();
			this._deactivateProbabilityLayers();
		} else if (val === 'probabilities') {
			switchToPercantageMode(val);
			this._deactivateNonProbabilityLayers();
			this.callback.probabilityMode();
		} else if (val === 'exceedance') {
			switchToPercantageMode(val);
			OpenLayers.Util.extend(this.values, {
				propertyName: val, threshold: opts
			});
			this._deactivateProbabilityLayers();
			this.callback.exceedanceMode();
		} else {
			this.callback.fail("Invalid visual style: " + val);
		}
	},

	_deactivateProbabilityLayers: function() {
		for (var i = 0; i < this.map.layers.length; i++) {
			if (this.map.layers[i].isProbabilityLayer && this.map.layers[i].getVisibility()) {
				this.map.layers[i].setVisibility(false);
			}
		}
		this.mapContainsProbabilityLayer = false;
	},
	
	_deactivateNonProbabilityLayers: function() {
		for (var i = 0; i < this.map.layers.length; i++) {
			if (!this.map.layers[i].isProbabilityLayer && !this.map.layers[i].isBaseLayer && this.map.layers[i].getVisibility()) {
				this.map.layers[i].setVisibility(false);
			}
		}						
	},
	
	_doesMapContainProbabilityLayers: function() {
		for (var i = 0; i < this.map.layers.length; i++) {
			if (this.map.layers[i].getVisibility() && this.map.layers[i].isProbabilityLayer) {
				return true;
			}
		}
		return false;
	},
	
	getExceedanceProbabilityThreshold: function() {
		return this.values.threshold;
	},
	
	getMap: function() { 
		return this.map; 
	},
	
	getScale: function() { 
		return this.scale; 
	},
	
	getSelectedConfidenceInterval: function() { 
		return this.selectedConfInterval; 
	},
	
	setSelectedConfidenceInterval: function(val) { 
		this.selectedConfInterval = parseFloat(val); 
	},
	
	getSelectedTime: function() {
		return this.times.selected; 
	},
	
	setSelectedTime: function(val) { 
		this.times.selected = val;
		$.each(this.clients, function (i,c){ 
			c.updateForNewTime();
		});
	},
	setSelectedTimeC: function(val) {
		this.setSelectedTime(val);
		this.callback.updateTime(this.times);
	},
	
	getUom: function() {
		return this.values.uom;
	},
	
	fail: function(l, m) {
		l.destroy();
		this.callback.fail(m);
	},
	
	ready: function(info) {
		this.updateTimeRange(info.time);
		this.setSelectedTime(this.times.min);
		
		if (!info.containsProbabilities) {
			if (this.oldValues) {
				if (this.oldValues.min > info.min)
					this.oldValues.min = info.min;
				if (this.oldValues.max < info.max)
					this.oldValues.max = info.max;
				this.oldValues.uom = info.uom;
			} else {
				if (this.values.min > info.min)
					this.values.min = info.min;
				if (this.values.max < info.max)
					this.values.max = info.max;
				this.values.uom = info.uom;
			}
		}
		
		if (info.containsProbabilities) {
			this.switchToMode('probabilities');
		} else if (this.values.mode == 'probabilities') {
			this.switchToMode('intervals');
		} else {
			this.scale.update(this.values);
			this.callback.updateValues(this.values);
		}
		this.callback.ready();
	}
});
