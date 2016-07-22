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
OpenLayers.Layer.VISS = OpenLayers.Class({
	CLASS_NAME: "OpenLayers.Layer.VISS",
	vissUrl: VISS_URL,
	json: new OpenLayers.Format.JSON(),
	resourceId: null,
	visualizers: null,
	ctrl: null,
	visualization: null,
	wmsLayers: null,
	wmsUrl: null,
	olLayer: null,
	phenomenon: null,
	dialogWidth: 500,
	dialogHeight: 500,
	imageCacheWorkaround: Math.random(),

	scale: {
		max: null,
		min: null,
		ints: null
	},

	initialize: function(ctrl, options) {
		var self = this;
		self.ctrl = ctrl;

		this.ctrl.getMap().events.register("changelayer", map, function(ev) {
			var l = self.ctrl.getMap().layers;
			if (self.olLayer && l[l.length-1] == self.olLayer) {
				//we are the first
				self.showRasterDialog();
			}
		});

		var req = { url: options.url, "responseMediaType": options.mime };
		if (options.request && options.request.trim()) {
			OpenLayers.Util.applyDefaults(req, {
				request: options.request,
				method: "POST",
				requestMediaType: "application/xml"
			});
		} else {
			req.method = "GET";
		}
		OpenLayers.Request.issue({
			method: "POST",
			url: self.vissUrl + "/resources",
			headers: {
				"Content-Type": "application/vnd.org.uncertweb.viss.request+json"
			},
			data: self.json.write(req),
			success: function(resp) {
				var resource = self.json.read(resp.responseText);
				self.resourceId = resource.id;
				self.phenomenon = resource.phenomenon;
				OpenLayers.Request.GET({
					url: self.getResourceUrl() + "/visualizers",
					success: function(r) {
						self.visualizers = self.json.read(r.responseText).visualizers;
						self.showRasterDialog();
					},
					failure: function(r) {
						throw r.responseText;
					}
				});
			},
			failure: function(r) {
				throw r.responseText;
			}
		});
	},

	getResourceUrl: function() {
		return this.vissUrl
			+ "/resources/"
			+ this.resourceId;
	},

	showRasterDialog: function() {
		var self = this;

		function fillOptions(v) {
			var options = "";
			$("#visDescription").html(v.description ?
					v.description : "No Description available.");
			var thereAreOptions = false;
			for (var key in v.options) {
				if (!thereAreOptions) {
					thereAreOptions = true;
				}
				var o = v.options[key];
				var inputId = "rasterInput-" + key;
				options += "<li><b>" + key + "</b>: " + o.description;
				options += '<span id="' + inputId + '-value" class="sliderValue"></span>';
				options += "<br/>"
				if (o.type == "number") {
					if (o.minimum != undefined && o.maximum != undefined) {
						options += '<div id="' + inputId + '" class="rasterInputSlider"></div>'
					} else {
						options += '<input id="' + inputId + '" type="text" />';
						options += '<span id="'+inputId+'-error" class="viss-input-error"></span>';
					}
				} else {
					options += "<p>Options of type " + o.value
						+ " are currently not supported<p>";
				}
				options += "</li>"
			}
			if (!thereAreOptions) {
				options += "<li>none</li>"
			}
			$("#optionsList").html(options);

			for (var key in v.options) {
				var o = v.options[key];
				if (o.type == "number" && o.minimum != "undefined" && o.maximum != "undefined") {
					var id = "#rasterInput-"+key;
					$(id).slider({ animate: true, range: false,
						min: o.minimum, max: o.maximum,
						step: (Math.abs(o.maximum-o.minimum)/100),
						change: function(e,ui){
							//self.updateVisualization();
						},
						slide: function(e,ui){
							$("#" + $(this).attr("id") + "-value").html(ui.value);
						}
					});
				}
			}
		}


		var html = '';
		html += '<h2>Visualizers</h2><select id="visualizer" name="visualizer" size="1">'
		for (var i = 0; i < this.visualizers.length; i++) {
			html += "<option>" + this.visualizers[i].id + "</option>";
		}
		html += '</select><p id="visDescription"></p><h2>Options</h2><ul id="optionsList"></ul><button id="updateVis">Update</button>';
		$("#rasterDialog").html(html);
		$("#visualizer").change(function() {
			OpenLayers.Request.GET({
				url: self.findVisualizer($(this).val()).href,
				success: function(r){
					fillOptions(self.json.read(r.responseText));
				}
			});
		});
		$("#updateVis").button().click(function() {
			self.updateVisualization();
		});
		OpenLayers.Request.GET({
			url: self.findVisualizer($("#visualizer").val()).href,
			success: function(r){
				fillOptions(self.json.read(r.responseText));
			}
		});
		$("#rasterDialog").dialog({
			title: 'VISS for ' +  this.phenomenon,
			width: this.dialogWidth,
			height: this.dialogHeight,
			close: function() {
				self.destroy()
			}
		});
	},

	findVisualizer: function(name) {
		for (var i = 0; i < this.visualizers.length; i++) {
			if (this.visualizers[i].id == name) {
				return this.visualizers[i];
			}
		}
	},

	updateVisualization: function() {
		var self = this;
		OpenLayers.Request.GET({
			url: this.findVisualizer($("#visualizer").val()).href,
			success: function(r) {
				var v = self.json.read(r.responseText);
				var options = {};
				var error = false;
				for (var key in v.options) {
					var o = v.options[key];
					if (o.type == "number") {
						try {
							if (o.minimum != undefined && o.maximum != undefined) {
								options[key] = parseFloat($("#rasterInput-" + key).slider("value"));
							} else {
								options[key] = parseFloat($("#rasterInput-" + key).val());
							}
							if (isNaN(options[key]) && v.options[key].required == true) {
								error = true;
								$('#rasterInput-' + key + '-error').html("required");
							}
						} catch(e) {
							error=true;
							$('#rasterInput-' + key + '-error').html(e);
						}
					}
				}
				if (error) {
					return;
				}
				OpenLayers.Request.POST({
					headers: { "Content-Type": "application/vnd.org.uncertweb.viss.create+json" },
					url: self.getResourceUrl() + "/visualizers/" + v.id,
					data: self.json.write(options),
					success: function(r) {
						var v = self.json.read(r.responseText);
						self.wmsUrl = v.reference.url;
						self.wmsLayers = v.reference.layers.join(",");
						self.visualization = v;
						self.scale = { min: null, max: null, ints: null };
						if (v.uom == "%") {
							//service calculates in 0..1... legend in 1..100
							self.ctrl.getScale().update({uom: ""});
							self.ctrl.updateScale(0, 1);
						} else {
							self.ctrl.getScale().update({uom: v.uom});
							self.ctrl.updateScale(v.minValue, v.maxValue);
						}
						self.ctrl.callback.updateValues(self.ctrl.values);
					}
				});
			}
		});
	},

	updateSld: function() {
		var update = false;
		if (this.ctrl.getScale().max != this.scale.max) {
			this.scale.max = this.ctrl.getScale().max;
			update = true;
		}
		if (this.ctrl.getScale().min != this.scale.min) {
			this.scale.min = this.ctrl.getScale().min;
			update = true;
		}
		if (this.ctrl.getScale().ints != this.scale.ints) {
			this.scale.ints = this.ctrl.getScale().ints;
			update = true;
		}

		if (this.visualization && update) {
			var self = this;
			var sld = this.ctrl.getScale().getSld();
			var data = new XMLSerializer().serializeToString(sld);
			OpenLayers.Request.POST({
				url: this.getResourceUrl() + "/visualizations/" + this.visualization.id + "/sld",
				headers: {
					"Content-Type": "application/vnd.ogc.sld+xml"
				},
				data: data,
				success: function() {
					self.visualizationUpdated();
				}
			});
		}
	},

	visualizationUpdated: function() {
		if (this.olLayer) {
			this.ctrl.getMap().removeLayer(this.olLayer);
			this.olLayer.destroy();
			this.imageCacheWorkaround = Math.random();
		}
		this.olLayer = new OpenLayers.Layer.WMS(
			this.visualization.id, this.wmsUrl, {
				layers: this.wmsLayers,
				transparent: true,
				tiled: true,
				/* browsers will cache the tiles. if the SLD changes the URL
				 * stays the same and no new image is shown... so randomize
				 * the url... */
				imageCacheWorkaround : this.imageCacheWorkaround
			}, {
				displayOutsideMaxExtent: true,
				isBaseLayer: false,
				opacity: .8,
			}
		);
		this.ctrl.getMap().addLayer(this.olLayer);
		if (!this.addedToMap) {
			this.addedToMap = true;
			this.ctrl.getMap().zoomToExtent(this.olLayer.getExtent())
		}
	},

	destroy: function() {
		if (this.olLayer) {
			this.olLayer.destroy();
		}
		if (this.resourceId) {
			OpenLayers.Request.DELETE({
				url: this.getResourceUrl(),
				callback: function(){}
			});
		}
		var idx = this.ctrl.rasterLayers.indexOf(this);
		if (idx != -1) this.ctrl.rasterLayers.splice(idx, 1);
    },

});
