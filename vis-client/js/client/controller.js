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
	selectedConfInterval: 95,
	
	format: {
		jsom: new OpenLayers.SOS.Format.JSOM(),
		xml: new OpenLayers.SOS.Format.ObservationCollection()
	},
	
	callback: {
		ready:			 function(){},
		fail: 			 function(){},
		updateTime:		 function(){},
		updateValues:	 function(){},
		probabilityMode: function(){},
		exceedanceMode:  function(){},
		standardMode:	 function(){}
	},
	
	scale: null,
	map: null,
	oldValues: null,
	
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
			var n = {};
			OpenLayers.Util.extend(n, this);
			return n;
		}
	},

	rasterLayers: [],
	
	initialize: function(scalebar, map, callbacks) {
		this.scale = scalebar; this.map = map;
		OpenLayers.Util.extend(this.values, {
			min: this.scale.getMin(),
			max: this.scale.getMax(),
			ints: this.scale.getInts()
		});
		OpenLayers.Util.extend(this.callback, callbacks);

		var self = this;
		this.map.events.register("changelayer", map, function(ev) {
			var mapContainsProbabilityLayer = self._doesMapContainProbabilityLayers();
			if (ev.layer.isProbabilityLayer) {
				if (!ev.layer.getVisibility()) {
					if (!mapContainsProbabilityLayer) {
						self.switchToMode("intervals");
					}					
				} else if (!mapContainsProbabilityLayer) {
					self.mapContainsProbabilityLayer = true;
					self.switchToMode("probabilities");
				}
			} else if (mapContainsProbabilityLayer && ev.layer.getVisibility()) {
				self._deactivateProbabilityLayers();
			}
			$.each(self.map.getControlsByClass('OpenLayers.Control.LayerSwitcher'), function(i,c) { c.redraw(); });
			$.each(self.map.getControlsByClass('OpenLayers.Control.AdvancedLayerSwitcher'), function(i,c) { c.draw(); });
		});
	},
	
	addLayer: function(options) {
		var self = this;

		function updateTime(time){
			if (self.times.min == null || self.times.min > time.min) 
				self.times.min = time.min;
			if (self.times.max == null || self.times.max < time.max) 
				self.times.max = time.max;
			self.times.step = (self.times.step == null) ? time.step : 
				OpenLayers.Util.gcd(time.step, self.times.step);
			self.callback.updateTime(self.times);
			self._update();
		}

		function createLayer(f) {
			var map = self.getMap();
			var dest = map.getProjectionObject();
			var meta = OpenLayers.Util.analyzeFeatures(f);
			
			/* reproject features to the maps reference system */
			$.each(f, function(i,e) { e.transform(dest); });

			/* create the layer */
			var layer = new OpenLayers.Layer.Vector(meta.proposedTitle, {
				styleMap: self._getStyleMap()
			});

			/* create a controller for the layer */
			var ctrl = new OpenLayers.Control.Visualizer(self, layer, function(time) {
				self.selectTime(time); self.callback.updateTime(self.times);
			});

			OpenLayers.Util.extend(layer, { 
				isObservationLayer: true, 
				isProbabilityLayer: meta.gotProbabilities
			});
			
			layer.addFeatures(f); 
			map.addLayer(layer);
			map.addControl(ctrl);

			updateTime(meta.time);
			
			self.selectTime(self.times.min);
		
			if (!meta.containsProbabilities) {
				var val = self.oldValues ? self.oldValues : self.values;
				if (val.min > meta.min) {
					val.min = meta.min;
				}
				if (val.max < meta.max) {
					val.max = meta.max;
				}
				val.uom = meta.uom;
			}
		
			if (meta.containsProbabilities) {
				self.switchToMode('probabilities');
			} else if (self.values.mode == 'probabilities') {
				self.switchToMode('intervals');
			} else {
				self.callback.updateValues(self.values);
				self._update();
			}
			
			self.callback.ready();
		}

		function generateFromJsom(r) { 
			createLayer(self.format.jsom.read(r.responseText)); 
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

		function request(options, generator){
			if (options.url) {
				if (options.request && options.request.trim()) {
					OpenLayers.Request.POST({
						url: options.url,
						data: options.request,
						success: generator,
						failure: self.callback.fail
					});
				} else {
					OpenLayers.Request.GET({
						url: options.url,
						success: generator,
						failure: self.callback.fail
					});
				}
			}
		}

		function createRasterLayer(options) {
			self.rasterLayers.push(new OpenLayers.Layer.VISS(self, options));
			self.callback.ready();
		}

		if (options.oc) {
			generateFromXml(options.oc);
			return;
		} else if (options.json) {
			generateFromJsom(options.json);
			return;
		}
		if (!options.mime) {
			options.mime = "application/xml";
		}
		
		switch (options.mime) {
			case "application/jsom":
				request(options, generateFromJsom);
				break;
			case "application/xml":
				request(options, generateFromXml);
				break;
			case "application/vnd.ogc.om+xml":
			case "application/netcdf":
			case "image/geotiff":
				createRasterLayer(options);
		}
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
			self._update();
			self.callback.updateValues(self.values);
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
			this.callback.updateValues(this.values);
			this.callback.standardMode();
			this._update();
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

	_updateControls: function() {
		$.each(this.getMap().getControlsByClass("OpenLayers.Control.Visualizer"), 
			function(i, c) { c.update(); }
		);
	},

	updateScale: function(min, max, ints) {
		if (min != undefined) {
			this.values.min = min;
		}
		if (max != undefined) {
			this.values.max = max;
		}
		if (ints) {
			this.values.ints = ints;
		}
		this._update();
	},
	
	_update: function() {
		this.scale.update(this.values);
		this._updateLayers();
		this._updateControls();
	},

	_updateLayers: function() {
		var self = this;
		var sm = this._getStyleMap();
		$.each(this.getMap().getLayersBy("isObservationLayer", true), function(i, l) {
			if (self.getVisualStyle() === 'exceedance') {
				$.each(l.features, function (i,f) {
					f.setThreshold(self.getThreshold());
				});
			}
			$.each(l.features, function (i,f) {
				f.setTime(self.getSelectedTime());
			});
			l.styleMap = sm;
			l.redraw();
		});
		
		$.each(this.rasterLayers, function(i,l) {
			l.updateSld();
		});
	},
	
	selectTime: function(val) { 
		if (val) this.times.selected = val;
		this._update();
	},
	
	_deactivateProbabilityLayers: function() {
		$.each(this.getMap().getLayersBy("isObservationLayer", true), 
			function(i, l) {
				if (l.isProbabilityLayer && l.getVisibility()) { 
					l.setVisibility(false); 
				}
			}
		);
	},
	
	_deactivateNonProbabilityLayers: function() {
		$.each(this.getMap().getLayersBy("isObservationLayer", true), 
			function(i, l) {
				if (!l.isProbabilityLayer && l.getVisibility()) { 
					l.setVisibility(false); 
				}
			}
		);
	},
	
	_doesMapContainProbabilityLayers: function() {
		var contains = false;
		$.each(this.getMap().getLayersBy("isProbabilityLayer", true), 
			function(i,l) {
				if (l.getVisibility()) { 
					contains = true;
					return false; //~break;
				}
			}
		);
		return contains;
	},
	
	_getStyleMap: function() {
		return new OpenLayers.StyleMap({
			"default": this.getScale().getStyle(),
			select: { 'pointRadius': 10 }
		});
	},
	
	getThreshold: function() { return this.values.threshold; },
	getMap: function() { return this.map; },
	getScale: function() { return this.scale; },
	getSelectedConfidenceInterval: function() { return this.selectedConfInterval; },
	setSelectedConfidenceInterval: function(val) { this.selectedConfInterval = parseFloat(val); },
	getSelectedTime: function() { return this.times.selected; },
	getUom: function() { return this.values.uom; },
	getVisibleScale: function() { return [this.values.min, this.values.max]; },
	getVisualStyle: function() { return this.values.mode; },
	fail: function(l, m) { l.destroy(); this.callback.fail(m); },
	
});
