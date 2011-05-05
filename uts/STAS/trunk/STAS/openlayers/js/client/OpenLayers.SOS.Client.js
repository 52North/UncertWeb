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
/* 
 * Contains portions of OpenLayers Map Viewer Library (http:/openlayers.org):
 * 
 * Copyright 2005-2010 OpenLayers Contributors, released under the Clear BSD
 * license. Please see http://svn.openlayers.org/trunk/openlayers/license.txt
 * for the full text of the license.
 *
 */
var sosClientId = 1;
OpenLayers.SOS = OpenLayers.SOS || {};
OpenLayers.SOS.Client = OpenLayers.Class({
		CLASS_NAME: "OpenLayers.SOS.Client",
		id: null,
		url: null, request: null,
		oc: null, json: null,
		map: null,
		readyCallback: null,
		failCallback: null,
		getObsFormat: new OpenLayers.SOS.Format.ObservationCollection(),
		jsomFormat: new OpenLayers.SOS.Format.JSOM(),
		foiFeatureMapping: null,
		selectedConfInterval: 95,
		scalebar: null,
		initialize: function (options) {
			OpenLayers.Util.extend(this, options);

			if (!this.readyCallback) {
				this.readyCallback = function (min, max){}
			}

			this.id = sosClientId++;
			if (this.url) {
				if (this.request && !this.request.trim() == "") {
					OpenLayers.Request.POST({
						url: this.url,
						data: this.request,
						scope: this,
						success: this.generateFeaturesFromXml,
						failure: this.failCallback
					});
				} else {
					OpenLayers.Request.GET({
						url: this.url,
						success: this.generateFeaturesFromXml,
						failure: this.failCallback
					});
				}
			} else if (this.oc) {
				this.generateFeaturesFromXml(this.oc);
				delete this.oc;
			} else if (this.json) {
				this.generateFeaturesFromJsom(this.json);
			}
		},
		generateFeaturesFromXml: function (r) {
			this.features = this.getObsFormat.read(
				(r.responseXML) ? r.responseXML : (r.responseText) ? r.responseText : r 
			);
			this.generateLayer();
		},
		generateFeaturesFromJsom: function () {
			this.features = this.jsomFormat.read(this.json);
			this.generateLayer();
		},
		generateLayer: function () {
			var dest = this.map.getProjectionObject();
			for (var i = 0; i < this.features.length; i++) {
				this.features[i].transform(dest);
			}

			this.layer = new OpenLayers.Layer.Vector("SOS Request " + this.id, {
				styleMap: new OpenLayers.StyleMap({
					default: this.scalebar.getStyle(),
					select: { 'pointRadius': 10 }
				})
			});
			this.layer.addFeatures(this.features);
			this.ctrl = new OpenLayers.Control.SelectFeature(this.layer, {
				scope: this, 
				onSelect: this.onFeatureSelect, 
				onUnselect: this.onFeatureUnselect
			});
			this.ctrl.handlers.feature.stopDown = false;
			this.ctrl.handlers.feature.stopUp = false;
			this.map.addLayer(this.layer);
			this.map.addControl(this.ctrl);
			this.ctrl.activate();
			
			var min = Number.POSITIVE_INFINITY, 
				max = Number.NEGATIVE_INFINITY,
				step = Number.NaN;
			for (var i = 0; i < this.features.length; i++) {
				var values = this.features[i].getValues();
				for (var j = 0; j < values.length; j++) {
					if (values[j][0].length == 2) {
						if (values[j][0][0] < min) min = values[j][0][0];
						if (values[j][0][1] > max) max = values[j][0][1];
					} else {
						if (values[j][0][0] < min) min = values[j][0][0];
						if (values[j][0][0] > max) max = values[j][0][0];
					}
				}
				if (values.length > 2) {
					// just check the distance between the first values...
					var curStep = values[1][0][0] - values[0][0][0];
					if (isNaN(step) || curStep < step) {
						step = curStep;
					}
				}
			}
			log.info("Created Layer: Timestamps: " + new Date(min).toUTCString() + " - " + new Date(max).toUTCString());

			this.readyCallback({
				time: { min: min, max: max, step: isNaN(step) ? 0 : step},
				layer: this.layer
			});
		},
		
		updateForNewScale: function () {
			if (this.layer) {
				this.layer.styleMap = new OpenLayers.StyleMap({
					default: this.scalebar.getStyle(),
					select: { 'pointRadius': 10 }
				});
				this.layer.redraw();
				if (this.selectedFeature && this.selectedFeature.popup 
					&& this.selectedFeature.popup.visible() && this.plot) {

					this.onFeatureSelect(this.selectedFeature);
				}
			}
		},
		
		updateForNewTime: function (time) {
			for (var i = 0; i < this.features.length; i++) {
				this.features[i].setTime(time);
			}
			this.updateForNewScale();
		},
		
		onFeatureUnselect: function (feature) {
			if (feature.popup) {
				feature.popup.hide();
				this.map.removePopup(feature.popup);
				feature.popup.destroy();
				feature.popup = null;
			}
		},
		
		onFeatureSelect: function (feature) {
			var values = feature.getValues();
			var uom = feature.getUom();
			var id = "plot" + new Date().getTime();
			var html = '<div class="bubble"><h2>' + feature.attributes.id 
			+ ' (<span id="intervalValue"></span>% confidence interval)</h2>'
			+'<div class="bubbleContent"><div id="' + id 
			+ '" class="bubblePlot"></div><div id="confidenceSliderContainer">'
			+'<div id="confidenceSlider"></div></div></div></div>';
			var ctrls = this.map.getControlsByClass("OpenLayers.Control.SelectFeature");
			feature.popup = new OpenLayers.Popup.FramedCloud("Feature",
				feature.geometry.getBounds().getCenterLonLat(),
				null, html, null, false, null);
			feature.popup.panMapIfOutOfView = true;
			this.selectedFeature = feature;
			this.map.addPopup(feature.popup, true);

			var self = this;
			$('#intervalValue').html(this.selectedConfInterval.toFixed(1));
			$("#confidenceSlider").slider({ 
				animate: true, 
				orientation: 'vertical',
				value: this.selectedConfInterval,
				min: 0.1, max: 99.9, step: 0.1,
				change: function(e, ui) {
					self.selectedConfInterval = ui.value;
					self.draw(id, values, uom);
				},
				slide: function (e, ui) {
					$('#intervalValue').html(ui.value.toFixed(1));
				}
			});
			
			this.draw(id, values, uom);
		},
		
		draw: function(id, v, uom) {
			var u = [], l = [], m = [];
			var p = parseFloat(this.selectedConfInterval);
			p = (100 - (100-p)/2)/100;
			for (var i = 0; i < v.length; i++) {
				var time = v[i][0], value = v[i][1];
				if (typeof(value) == "number") {
					value = [null, value, null];
				} else {
					/* jStat distribution */
					if (value.getClassName && value.getClassName().match(".*Distribution$")) {
						value = [ value.getQuantile(p), value.getMean(), value.getQuantile(1-p) ];
					}
				}
				for (var j = 0; j < time.length; j++) {
					l.push([time[j], value[0]]);
					m.push([time[j], value[1]]);
					u.push([time[j], value[2]]);
				}
			}

			/* reverse lower to get background color... */
			if (l.length > 0) {
				l.sort(function(a,b){return (a[0]>b[0])?-1:((a[0]<b[0])?1:0);});
				l.push(u[0]);
			}
			var scale = this.scalebar;
			
			this.plot = $.plot($('#' + id), [
				{ color: "#FF0000", data: u.concat(l), lines: { fill: true } }, 
				{ color: "#4F4F4F", points: { show: true }, data: m }
			], {
					xaxis: { color: "#B6B6B6", 
							 mode: "time" },
					yaxis: { color: "#B6B6B6" }, 
					grid:  { color: "#B6B6B6", 
							 hoverable: true, 
							 mouseActiveRadius: 25 },
					lines: { show: true },
					hooks: { draw: [function(plot, ctx) {
						var data = plot.getData()[1].data;
						for (var j = 0; j < data.length; j++) {
							var x = plot.getPlotOffset().left 
								+ plot.getAxes().xaxis.p2c(data[j][0]);
							var y = plot.getPlotOffset().top 
								+ plot.getAxes().yaxis.p2c(data[j][1]);
							ctx.lineWidth = 0;
							ctx.beginPath();
							ctx.arc(x, y, 3, 0, Math.PI * 2, true);
							ctx.closePath();            
							ctx.fillStyle = scale.getColorForResultValue(data[j][1]);
							ctx.fill();
						} 
					}]
				}
			});
			var previous;
			$('#'+id).bind("plothover", function(event, pos, item) {
					$("#x").text(pos.x.toFixed(2));
					$("#y").text(pos.y.toFixed(2));
					if (item) {
						if (previous != item.datapoint) {
							previous = item.datapoint;
							$("#tooltip").remove();
							var text = item.datapoint[1] + " " + uom;
							$('<div id="tooltip">' + text + "</div>").css( {
									position: "absolute",
									display: "none",
									top: item.pageY + 15,
									left: item.pageX + 5,
								}).appendTo("body").show();
						}
					} else {
						$("#tooltip").remove();
						previous = null;
					}
				});
		},
		destroy: function () {/*TODO*/}
	});
/* vim: set ts=4 sts=4 sw=4 noet ft=javascript fenc=utf-8 */
