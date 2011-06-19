
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
		probabilityMode: function (bool) {},
	},
	
	clients: [],
	scale: null,
	map: null,
	mode: "intervals",
	
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
		propertyName: null,
		ints: null,
		threshold: null
	},
	
	oldValues: null,
	mapContainsProbabilityLayer: false,
	
	initialize: function(scalebar,map,time,callbacks) {
		this.scale = scalebar;
		this.map = map;
		this.values = { 
			min: this.scale.getMin(), 
			max: this.scale.getMax(), 
			ints: this.scale.getInts(),
			propertyName: 'resultValue' 
		};
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
			this.values.ints = ints
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
		this.times.step = (this.times.step == null)? time.step : gcd(time.step, this.times.step);
		this.callback.updateTime(this.times);
	},
	
	getVisibleScale: function() {
		return [this.values.min, this.values.max];
	},
	
	getVisualStyle: function() {
		return this.mode;
	},

	switchToMode: function(val, opts) {
		var self = this;
		
		function switchToPercantageMode() {
			self.values.min = 0;
			self.values.max = 100;
			self.values.uom = '%';
			self.scale.update(self.values);
			self.callback.updateValues(self.values);
			$.each(self.clients, function (i, c){ 
				c.updateForNewScale(); 
			}); 
		}
		
		function backup() {
			if (self.mode !== 'exceedance' 
				&& self.mode !== 'probabilities') {
				self.oldValues = self.values;
				self.oldValues.mode = self.mode;
			}
		}
		
		switch(val) {
			case 'bars': 
			case 'intervals':
				var prevMode = this.mode;
				if (this.oldValues) {
					this.values = this.oldValues;
					this.scale.update(this.values);
					this.callback.updateValues(this.values);
					this.mode = this.oldValues.mode;
					this.oldValues = null;
				} else {
					this.mode = val;
				}
				if (prevMode == 'exceedance' || prevMode == 'probabilities') {
					this.callback.updateValues(this.values);
				}
				this._deactivateProbabilityLayers();
				this.updateForNewScale();
				this.callback.probabilityMode(false);
				break;
			case 'probabilities':
				backup();
				this._deactivateNonProbabilityLayers();
				this.mode = val;
				switchToPercantageMode();
				this.callback.probabilityMode(true);
				break;
			case 'exceedance':
				backup();
				this._deactivateProbabilityLayers();
				this.values.propertyName = this.mode = val;
				this.values.threshold = opts;
				switchToPercantageMode();
				this.callback.probabilityMode(false);
				break;
			default: 
				this.callback.fail("Invalid visual style: " + val);
		}
	},

	_deactivateProbabilityLayers: function() {
		for (var i = 0; i < this.map.layers.length; i++) {
			if (this.map.layers[i].isProbabilityLayer && this.map.layers[i].getVisibility()) {
				this.map.layers[i].setVisibility(false);
			}
		}
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
		this.setSelectedTime(info.time.min);
		if (info.containsProbabilities) {
			this.switchToMode('probabilities');
		} else {
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
			if (this.mode == 'probabilities') {
				this.switchToMode('intervals');
			}
		}
		this.callback.ready();
	}
});
