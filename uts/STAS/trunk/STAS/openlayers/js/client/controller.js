
OpenLayers.SOS.Controller = OpenLayers.Class({
	CLASS_NAME: "OpenLayers.SOS.Controller",
	selectedConfInterval: 95.0,
	visualStyle: "intervals",
	
	format: {
		jsom: new OpenLayers.SOS.Format.JSOM(),
		xml: new OpenLayers.SOS.Format.ObservationCollection()
	},
	callback: {
		ready: 			 function () {},
		fail: 			 function () {},
		selectTime: 	 function () {},
		updateTimeRange: function () {},
		selectThreshold: function () {}
	},
	clients: [],
	selectedTime: null,
	visibleScale: null,
	scale: null,
	map: null,
	times: null,
	old: null,
	exceedanceProbabilityThreshold: null,
	valueRange: null,
	
	initialize: function(scalebar,map,time,scale,callbacks) {
		this.scale = scalebar;
		this.map = map;
		this.selectedTime = time;
		this.visibleScale = scale;
		OpenLayers.Util.extend(this.callback, callbacks);
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
		if (min != undefined && max != undefined) {
			this.visibleScale = [min, max];
		}
		this.scale.update(min, max, ints);
		$.each(this.clients, function (i, c){ 
			c.updateForNewScale(); 
		}); 
	},
	
	updateValueRange: function(v) {
		if (!this.valueRange) {
			this.valueRange = [v.min, v.max];
		} else {
			if (this.valueRange[0] > v.min)
				this.valueRange[0] = v.min;
			if (this.valueRange[1] < v.max)
				this.valueRange[1] = v.max;
		}
		this.scale.update(this.valueRange[0],this.valueRange[1]);
		this.callback.selectThreshold(this.valueRange);
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
		
		if (!this.times) {
			this.times = [ time.min, time.max, time.step ];
		} else {
			if (this.times[0] > time.min) this.times[0] = time.min;
			if (this.times[1] < time.max) this.times[1] = time.max;
			this.times[2] = gcd(time.step, this.times[2]);
		}
		this.callback.updateTimeRange(this.times);
		this.callback.selectTime(this.times[0]);
	},
	
	getVisibleScale: function() {
		return this.visibleScale;
	},
	
	getVisualStyle: function() {
		return this.visualStyle;
	},
	
	setVisualStyle: function(val) {
		this.visualStyle = val;
		switch (val) {
			case 'bars': 
			case 'intervals':
				if (this.old) {
					this.scale.setUom(this.old.uom);
					this.scale.update(this.old.min, this.old.max);
					this.callback.selectThreshold([this.old.min, this.old.max]);
					this.old = null;
				}
				this.scale.setPropertyName('resultValue');
				this.updateForNewScale();
			break;
			default: 
				this.setVisualStyle('intervals');
				this.callback.fail("Invalid visual style: " + val);
		}
	},
	
	setExceedenceProbabilityThreshold: function(val) {
		this.old = {
			min: this.scale.getMin(), 
			max: this.scale.getMax(), 
			uom: this.scale.getUom()
		};
		this.visualStyle = 'exceedance';
		this.exceedanceProbabilityThreshold = val;
		this.scale.setUom('%');
		this.callback.selectThreshold([0, 100]);
		this.scale.update(0, 100);
		this.visibleScale = [0, 100];
		this.scale.setPropertyName('exceedance');
		$.each(this.clients, function (i, c){ 
			c.updateForNewScale(); 
		}); 
	},

	getExceedanceProbabilityThreshold: function() {
		return this.exceedanceProbabilityThreshold;
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
		return this.selectedTime; 
	},
	
	setSelectedTime: function(val) { 
		this.selectedTime = val; 
		$.each(this.clients, function (i,c){ 
			c.updateForNewTime();
		});
	},
	
	setUom: function(uom) {
		this.scale.setUom(uom);
		this.callback.selectThreshold([this.scale.getMin(), this.scale.getMax()]);
	},
	
	getUom: function() {
		return this.scale.getUom();
	},
	
	fail: function(l, m) {
		l.destroy();
		this.callback.fail(m);
	},
	
	ready: function(l) {
		this.callback.ready();
	}
});
