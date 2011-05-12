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
		url: null, 
		request: null,
		oc: null, 
		json: null,
		map: null,
		readyCallback: null,
		failCallback: null,
		getObsFormat: new OpenLayers.SOS.Format.ObservationCollection(),
		jsomFormat: new OpenLayers.SOS.Format.JSOM(),
		foiFeatureMapping: null,
		selectedConfInterval: 95,
		visualStyle: "intervals",
		svId: "singlevaluedialog",
		selectedTime: null,
		visibleScale: null,
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
			this.selectedTime = min;
			this.readyCallback({
				time: { min: min, max: max, step: isNaN(step) ? 0 : step},
				layer: this.layer,
			});
		},
		
		updateForNewScale: function (minmax) {
			if (minmax) this.visibleScale = minmax;
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
			this.selectedTime = time;
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
				if ($("#" + this.svId).length) {
					$("#" + this.svId).dialog("close");
					$("#" + this.svId).remove();
					this.singleValueWindowOpen = false;
				}
			}
		},
		
		onFeatureSelect: function (feature) {
			var values = feature.getValues();
			var uom = feature.getUom();
			var id = "plot" + new Date().getTime();
			var html = 
			'<div class="bubble">'
				+ '<div>'
					+ '<span id="viewChooser">'
						+ '<input type="radio" name="viewChooser" id="convInterval" checked="checked"/>'
						+ '<label for="convInterval">Intervals</label>'
						+ '<input type="radio" name="viewChooser" id="errorBars"/>'
						+ '<label for="errorBars">Error Bars</label>'
					+ '</span>'
					+ '<h2>' + feature.attributes.id + ' (<span id="intervalValue"></span>% confidence interval)</h2>'
				+ '</div>'
				+ '<div class="bubbleContent">'
					+ '<div id="' + id + '" class="bubblePlot"></div>'
					+ '<div id="confidenceSliderContainer">'
						+ '<div id="confidenceSlider"></div>'
					+ '</div>'
				+ '</div>'
			+ '</div>';
			var ctrls = this.map.getControlsByClass("OpenLayers.Control.SelectFeature");
			feature.popup = new OpenLayers.Popup.FramedCloud("Feature",
				feature.geometry.getBounds().getCenterLonLat(),
				null, html, null, false, null);
			feature.popup.panMapIfOutOfView = true;
			this.selectedFeature = feature;
			this.map.addPopup(feature.popup, true);
			
			var self = this;
			$('#convInterval').button().click(function(){
				self.visualStyle = "intervals"
				self.draw(id, values, uom); 
			});
			$('#errorBars').button().click(function(){
				self.visualStyle = "bars";
				self.draw(id, values, uom);
			});
			
			$('#viewChooser').buttonset();
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
		
		draw: function(id, v, uom, type) {
			var u = [], l = [], m = [];
			var self = this;
			var svId = this.svId;
			var p = (100-(100-parseFloat(this.selectedConfInterval))/2)/100;
			var DATA_INDEX;
			
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
			
			function colorPointHook(plot, ctx) {
				var data = plot.getData()[DATA_INDEX].data;
				for (var j = 0; j < data.length; j++) {
					if (data[j][1] >= plot.getAxes().yaxis.min 
						&& data[j][1] <= plot.getAxes().yaxis.max) {
						var x = plot.getPlotOffset().left + plot.getAxes().xaxis.p2c(data[j][0]);
						var y = plot.getPlotOffset().top + plot.getAxes().yaxis.p2c(data[j][1]);
						ctx.lineWidth = 0;
						ctx.beginPath();
						ctx.arc(x, y, 3, 0, Math.PI * 2, true);
						ctx.closePath();            
						ctx.fillStyle = self.scalebar.getColorForResultValue(data[j][1]);
						ctx.fill();
					}
				} 
			}
			
			function verticalTimeLineHook(plot, ctx) {
				var x = plot.getPlotOffset().left + plot.getAxes().xaxis.p2c(self.selectedTime);
				var y0  = plot.getPlotOffset().top + plot.getAxes().yaxis.p2c(plot.getAxes().yaxis.max);
				var y1 = plot.getPlotOffset().top + plot.getAxes().yaxis.p2c(plot.getAxes().yaxis.min);
				ctx.linewidth = 5;
				ctx.strokeStyle = "#000";
				ctx.beginPath();
				ctx.moveTo(x, y0);
				ctx.lineTo(x, y1);
				ctx.closePath();
				ctx.stroke();
			}

			function errorBarHook(plot, ctx) {
				var data = plot.getData()[0].data;
				var b = 4;
				ctx.strokeStyle = "#F00";
				ctx.lineWidth = 2;
				var maxY = plot.getPlotOffset().top + plot.getAxes().yaxis.p2c(plot.getAxes().yaxis.max);
				var minY = plot.getPlotOffset().top + plot.getAxes().yaxis.p2c(plot.getAxes().yaxis.min);
				for (var j = 0; j < data.length; j++) {
					if (u[j][1] != null && l[j][1] != null) {
						if (data[j][1] >= plot.getAxes().yaxis.min 
							&& data[j][1] <= plot.getAxes().yaxis.max) {
							var x = plot.getPlotOffset().left + plot.getAxes().xaxis.p2c(data[j][0]);
							var ym = plot.getPlotOffset().top + plot.getAxes().yaxis.p2c(data[j][1])
							var y0 = plot.getPlotOffset().top + plot.getAxes().yaxis.p2c(u[j][1]);
							var y1 = plot.getPlotOffset().top + plot.getAxes().yaxis.p2c(l[j][1]);
							ctx.beginPath();
							ctx.moveTo(x, ym);
							if (minY > y0) {
								ctx.lineTo(x,   y0);
								ctx.moveTo(x-b, y0);
								ctx.lineTo(x+b, y0);
							} else {
								ctx.lineTo(x, minY);
							}
							ctx.moveTo(x, ym);
							if (maxY < y1) {
								ctx.lineTo(x,   y1);
								ctx.moveTo(x-b, y1);
								ctx.lineTo(x+b, y1);
							} else {
								ctx.lineTo(x, maxY);
							}
							ctx.closePath();            
							ctx.stroke();
						}
					}
				} 
			}
			
			var series;
			var buffer = (this.visibleScale) ? 0.1 * (this.visibleScale[1] - this.visibleScale[0]) : 0;
			var options = {
				xaxis: { 
					color: "#B6B6B6", 
					mode: "time" 
				},
				yaxis: {
					color: "#B6B6B6",
					min: (this.visibleScale) ? this.visibleScale[0] - buffer: null,
					max: (this.visibleScale) ? this.visibleScale[1] + buffer: null
				}, 
				grid:  { 
					color: "#B6B6B6", 
					hoverable: true,
					clickable: true,
					mouseActiveRadius: 25 
				},
				lines: { show: true }
			};
			if (self.visualStyle == "bars") {
				series = [
					{ color: "#4F4F4F", points: { show: true }, data: m }
				]
				options.hooks = { 
					draw: [ verticalTimeLineHook, errorBarHook, function(plot, ctx) {
						colorPointHook(plot, ctx)
					}]
				}
				DATA_INDEX = 0;
			
			} else if (self.visualStyle === "intervals") {
				/* reverse lower to get background color... */
				if (l.length > 0) {
					l.sort(function(a,b){return (a[0]>b[0])?-1:((a[0]<b[0])?1:0);});
					l.push(u[0]);
				}
				series = [
					{ color: "#FF0000", lines: { fill: true }, data: u.concat(l) }, 
					{ color: "#4F4F4F", points: { show: true }, data: m }
				];
				options.hooks = { 
					draw: [ verticalTimeLineHook, function(plot, ctx) {
						colorPointHook(plot, ctx)
					}]
				}
				DATA_INDEX = 1;
			} else {
				throw "Invalid type: "+ self.visualStyle;
			}
			this.plot = $.plot($('#' + id), series, options);
			var previous;
			$('#' + id).bind("plothover", function(event, pos, item) {
				$("#x").text(pos.x.toFixed(2));
				$("#y").text(pos.y.toFixed(2));
				if (item) {
					if (previous != item.datapoint) {
						previous = item.datapoint;
						$("#tooltip").remove();
						var text = item.datapoint[1] + " " + uom;
						$('<div id="tooltip" class="tooltip">' + text + "</div>").css( {
							position: "absolute",
							display: "none",
							top: item.pageY + 15,
							left: item.pageX + 5
						}).appendTo("body").show();
					}
				} else {
					$("#tooltip").remove();
					previous = null;
				}
			});

			function drawSingleValue(v) {
				function plot() {
					if (typeof(v[1]) === "number") {
						$("#"+svId).html(v[1].toFixed(5) + " " + uom);
					} else {
						if (v[1].getClassName && v[1].getClassName().match(".*Distribution$")) {
							var dplot = new DistributionPlot(svId, v[1], new Range(self.visibleScale[0], self.visibleScale[1], 100), {
								yaxis: { min: 0, max: 1}
							});
						} else {
							throw "Unsupported!!!";
						}
					}
				}
				
				var title = new Date(v[0][0]).toGMTString();
				if (v[0].length == 2) {
					 title += " - " + new Date(v[0][1]).toGMTString()
				}
				
				if ($("#" + svId).length) {
					$("#" + svId).html("");
					plot();
				} else {
					$('<div id="' + svId + '"></div>').dialog({ 
						title: title, open: plot, resize: plot, 
						width: 450, height: 450
					});
				}
			}
			$('#' + id).unbind("plotclick");
			$('#' + id).bind("plotclick", function(event, pos, item) {
				if (item && item.seriesIndex == DATA_INDEX) {
					self.singleValueWindowOpen = true;
					self.timeSelectCallback(v[item.dataIndex][0]);
				}
			});
			if (self.singleValueWindowOpen) { //single value window is open
				var selectedTimeValue = null;
				for (var i = 0; i < v.length; i++) {
					if ((v[i][0][0] == this.selectedTime) 
						|| (v[i][0].length == 2 
							&& v[i][0][0] <= this.selectedTime 
							&& v[i][0][1] >= this.selectedTime)) { 
						drawSingleValue(v[i]); break;
					}
				}
			}
		},
		destroy: function () {/*TODO*/}
	});
/* vim: set ts=4 sts=4 sw=4 noet ft=javascript fenc=utf-8 */
