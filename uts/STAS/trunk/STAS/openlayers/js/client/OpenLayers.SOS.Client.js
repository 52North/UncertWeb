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
		statusCalback: null,
		readyCallback: null,
		failCallback: null,
		getObsFormat: new OpenLayers.SOS.Format.ObservationCollection(),
		jsomFormat: new OpenLayers.SOS.Format.JSOM(),
		foiFeatureMapping: null,
		scalebar: null,
		initialize: function (options) {
			OpenLayers.Util.extend(this, options);
			this.id = sosClientId++;
			this.statusCallback("Sending Request");
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

			this.statusCallback("Generating layer");
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
			this.readyCallback();
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
			var html = '<div class="bubble"><h2>' + feature.attributes.id + ' (<span id="intervalValue"></span>% confidence interval)</h2>'
			+'<div class="bubbleContent"><div id="' + id + '" class="bubblePlot"></div><div id="confidenceSliderContainer">'
			+'<div id="confidenceSlider"></div></div></div></div>';
			var ctrls = this.map.getControlsByClass("OpenLayers.Control.SelectFeature");
			feature.popup = new OpenLayers.Popup.FramedCloud("Feature",
				feature.geometry.getBounds().getCenterLonLat(),
				null, html, null, false, null);
			feature.popup.panMapIfOutOfView = true;
			this.selectedFeature = feature;
			this.map.addPopup(feature.popup, true);

			var self = this;
			var initConfidenceValue = 95;
			$('#intervalValue').html(initConfidenceValue.toFixed(2));
			$("#confidenceSlider").slider({ 
				animate: true, 
				value: initConfidenceValue,
				min: 0.00001, 
				max: 99.99999,
				step: 0.00001,
				orientation: 'vertical',
				change: function(e, ui) {
					self.draw(id, values, uom, ui.value);
				},
				slide: function (e, ui) {
					$('#intervalValue').html(ui.value);
				}
			});
			
			this.draw(id, values, uom, initConfidenceValue);
		},
		
		draw: function(id, v, uom, p) {
			var u = [], l = [], m = [];
			p = parseFloat(p);
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
