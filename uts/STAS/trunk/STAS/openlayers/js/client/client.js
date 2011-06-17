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
	CLASS_NAME: 'OpenLayers.SOS.Client',
	svId: 'singlevaluedialog',

	id: null, ctrl: null, layer: null,
	
	
	initialize: function (ctrl, features) {
		this.ctrl = ctrl;
		this.features = features;
		this.id = sosClientId++;
		this.generateLayer();
	},
	
	generateLayer: function () {
		var dest = this.ctrl.getMap().getProjectionObject();
		for (var i = 0; i < this.features.length; i++) {
			this.features[i].transform(dest);
		}

		this.layer = new OpenLayers.Layer.Vector('SOS Request ' + this.id, {
			styleMap: new OpenLayers.StyleMap({
				default: this.ctrl.getScale().getStyle(),
				select: { 'pointRadius': 10 }
			})
		});
		this.layer.addFeatures(this.features);
		var control = new OpenLayers.Control.SelectFeature(this.layer, {
				onSelect: this.onFeatureSelect, 
				onUnselect: this.onFeatureUnselect,
				scope: this
		});
		control.handlers.feature.stopDown = false;
		control.handlers.feature.stopUp = false;
		this.ctrl.getMap().addLayer(this.layer);
		this.ctrl.getMap().addControl(control);
		control.activate();
		
		var min = Number.POSITIVE_INFINITY;
		var max = Number.NEGATIVE_INFINITY;
		var step = Number.NaN;
		var uom;
		for (var i = 0; i < this.features.length; i++) {
			if (!uom) {
				uom = this.features[i].getUom();
			}
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
		this.ctrl.setSelectedTime(min);
		this.ctrl.updateTimeRange({
			min: min, 
			max: max, 
			step: isNaN(step) ? 0 : step
		});
		this.updateForNewTime();
		this.ctrl.setUom(uom);
		this.ctrl.ready(this);
	},
	
	updateForNewScale: function () {
		if (this.layer) {
			if (this.ctrl.getVisualStyle() === 'exceedance') {
				for (var i = 0; i < this.features.length; i++) {
					this.features[i].setExceedanceProbabilityThreshold(
						this.ctrl.getExceedanceProbabilityThreshold());
				}
			}
		
			this.layer.styleMap = new OpenLayers.StyleMap({
				default: this.ctrl.getScale().getStyle(),
				select: { 'pointRadius': 10 }
			});
			this.layer.redraw();
			if (this.selectedFeature && 
				this.selectedFeature.popup && 
				this.selectedFeature.popup.visible() && 
				this.plot) {
				this.onFeatureSelect(this.selectedFeature);
			}
		}
	},
	
	updateForNewTime: function () {
		for (var i = 0; i < this.features.length; i++) {
			this.features[i].setTime(this.ctrl.getSelectedTime());
		}
		this.updateForNewScale();
	},
	
	onFeatureUnselect: function (feature) {
		if (feature.popup) {
			feature.popup.hide();
			this.ctrl.getMap().removePopup(feature.popup);
			feature.popup.destroy();
			feature.popup = null;
			if ($('#' + this.svId).length) {
				$('#' + this.svId).dialog('close');
				$('#' + this.svId).remove();
				this.singleValueWindowOpen = false;
			}
		}
	},
	
	onFeatureSelect: function (feature) {
		var values = feature.getValues();
		var uom = feature.getUom();
		var method = this.ctrl.getVisualStyle();
		var id = 'plot' + new Date().getTime();
		var html;
		if (this.ctrl.getVisualStyle() !== 'exceedance') {
			html = 
			'<div class="bubble">' +
				'<div>' +
					'<h2>' + feature.attributes.id + ' (<span id="intervalValue"></span>% confidence interval)</h2>' +
				'</div>' +
				'<div class="bubbleContent">' +
					'<div id="' + id + '" class="bubblePlot"></div>' +
					'<div id="confidenceSliderContainer">' +
						'<div id="confidenceSlider"></div>' +
					'</div>' +
				'</div>' +
			'</div>';
		} else {
			html = 
			'<div class="bubble">' +
				'<div>' +
					'<h2>' + feature.attributes.id + '</h2>' +
				'</div>' +
				'<div class="bubbleContent">' +
					'<div id="' + id + '" class="bubblePlot"></div>' +
				'</div>' +
			'</div>';
		}
		
		var ctrls = this.ctrl.getMap().getControlsByClass('OpenLayers.Control.SelectFeature');
		feature.popup = new OpenLayers.Popup.FramedCloud('Feature',
			feature.geometry.getBounds().getCenterLonLat(),
			null, html, null, false, null);
		feature.popup.panMapIfOutOfView = true;
		this.selectedFeature = feature;
		this.ctrl.getMap().addPopup(feature.popup, true);

		if (this.ctrl.getVisualStyle() !== 'exceedance') {
			$('#intervalValue').html(this.ctrl.getSelectedConfidenceInterval().toFixed(1));
		
			var self = this;
			$('#confidenceSlider').slider({
				value: this.ctrl.getSelectedConfidenceInterval(),
				orientation: 'vertical', animate: true, 
				min: 0.1, max: 99.9, step: 0.1,
				change: function(e, ui) {
					self.ctrl.setSelectedConfidenceInterval(ui.value);
					self.draw(id, values, uom);
				},
				slide: function (e, ui) {
					$('#intervalValue').html(ui.value.toFixed(1));
				}
			});
		}
		this.draw(id, values, uom);
	},
	
	draw: function(id, v, uom, type) {
		var u = [], l = [], m = [];
		var self = this;
		var svId = this.svId;
		var p = (100-(100-this.ctrl.getSelectedConfidenceInterval())/2)/100;
		var DATA_INDEX;
		
		function colorPointHook(plot, ctx) {
			var data = plot.getData()[DATA_INDEX].data;
			for (var j = 0; j < data.length; j++) {
				if (data[j][1] >= plot.getAxes().yaxis.min && 
					data[j][1] <= plot.getAxes().yaxis.max) {
					var x = plot.getPlotOffset().left 
						  + plot.getAxes().xaxis.p2c(data[j][0]);
					var y = plot.getPlotOffset().top 
						  + plot.getAxes().yaxis.p2c(data[j][1]);
					ctx.lineWidth = 0;
					ctx.beginPath();
					ctx.arc(x, y, 3, 0, Math.PI * 2, true);
					ctx.closePath();            
					ctx.fillStyle = self.ctrl.getScale().getColor(data[j][1]);
					ctx.fill();
				}
			} 
		}
		
		function verticalTimeLineHook(plot, ctx) {
			var x  = plot.getPlotOffset().left 
				   + plot.getAxes().xaxis.p2c(self.ctrl.getSelectedTime());
			var y0 = plot.getPlotOffset().top 
				   + plot.getAxes().yaxis.p2c(plot.getAxes().yaxis.max);
			var y1 = plot.getPlotOffset().top 
				   + plot.getAxes().yaxis.p2c(plot.getAxes().yaxis.min);
			ctx.linewidth = 5;
			ctx.strokeStyle = '#000';
			ctx.beginPath();
			ctx.moveTo(x, y0);
			ctx.lineTo(x, y1);
			ctx.closePath();
			ctx.stroke();
		}

		function errorBarHook(plot, ctx) {
			var data = plot.getData()[0].data;
			var b = 4;
			ctx.strokeStyle = '#F00';
			ctx.lineWidth = 2;
			var maxY = plot.getPlotOffset().top 
				+ plot.getAxes().yaxis.p2c(plot.getAxes().yaxis.max);
			var minY = plot.getPlotOffset().top 
				+ plot.getAxes().yaxis.p2c(plot.getAxes().yaxis.min);
			for (var j = 0; j < data.length; j++) {
				if (u[j][1] != null && l[j][1] != null) {
					if (data[j][1] >= plot.getAxes().yaxis.min 
						&& data[j][1] <= plot.getAxes().yaxis.max) {
						var x  = plot.getPlotOffset().left 
							   + plot.getAxes().xaxis.p2c(data[j][0]);
						var ym = plot.getPlotOffset().top 
							   + plot.getAxes().yaxis.p2c(data[j][1])
						var y0 = plot.getPlotOffset().top 
							   + plot.getAxes().yaxis.p2c(u[j][1]);
						var y1 = plot.getPlotOffset().top 
							   + plot.getAxes().yaxis.p2c(l[j][1]);
						ctx.beginPath();
						ctx.moveTo(x, ym);
						if (minY > y0) {
							ctx.lineTo(x  , y0);
							ctx.moveTo(x-b, y0);
							ctx.lineTo(x+b, y0);
						} else {
							ctx.lineTo(x, minY);
						}
						ctx.moveTo(x, ym);
						if (maxY < y1) {
							ctx.lineTo(x  , y1);
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
		
		
		var min, max;
		if (this.ctrl.getVisualStyle() === 'exceedance') {
			min = 0;
			max = 100;
		} else {
			var vs = this.ctrl.getVisibleScale();
			if (vs) {
				var b = 0.1 * (vs[1] - vs[0]);
				min = vs[0] - b;
				max = vs[1] + b;
			} else {
				min = max = null;
			}
		}
		
		var options = {
			xaxis: { color: '#B6B6B6', mode: 'time' },
			yaxis: { color: '#B6B6B6', min: min, max: max }, 
			grid:  { 
				color: '#B6B6B6', 
				hoverable: true,
				clickable: true,
				mouseActiveRadius: 25 
			},
			lines: { show: true }
		};

		var series;
		if (this.ctrl.getVisualStyle() === 'exceedance') {
			var d = [];
			var t = this.ctrl.getExceedanceProbabilityThreshold();
			for (var i = 0; i < v.length; i++) {
				var ep = OpenLayers.SOS.ObservationSeries.prototype.calculateExceedanceProbability(t,v[i][1]);
				for (var j = 0; j < v[i][0].length; j++) {
					d.push([v[i][0][j], ep]);
				}
				
			}
			series = [{ data: d, color: '#4F4F4F', points: { show: true }}];
			options.hooks = {draw:[verticalTimeLineHook,colorPointHook]};
			DATA_INDEX = 0;
			uom = '%';
		} else {
			for (var i = 0; i < v.length; i++) {
				var time = v[i][0], value = v[i][1];
				if (typeof(value) === 'number') {
					value = [null, value, null];
				} else if (value.getClassName && value.getClassName().match('.*Distribution$')) {
					var cI = value.getConfidenceInterval(p);
					value = [ cI[1], value.getMean(), cI[0] ];
				} else {
					this.ctrl.fail(this, "Unknown Value Type: " + value);
				}
				for (var j = 0; j < time.length; j++) {
					l.push([time[j], value[0]]);
					m.push([time[j], value[1]]);
					u.push([time[j], value[2]]);
				}
			}
			if (this.ctrl.getVisualStyle() === 'bars') {
				series = [{ data: m, color: '#4F4F4F', points: { show: true } }];
				options.hooks = {draw:[verticalTimeLineHook,errorBarHook,colorPointHook]};
				DATA_INDEX = 0;
			} else if (this.ctrl.getVisualStyle() === 'intervals') {
				/* reverse lower to get background color... */
				if (l.length > 0) {
					l.sort(function(a,b){return (a[0]>b[0])?-1:((a[0]<b[0])?1:0);});
					l.push(u[0]);
				}
				series = [
					{ color: '#FF0000', lines: { fill: true }, data: u.concat(l) }, 
					{ color: '#4F4F4F', points: { show: true }, data: m  }
				];
				options.hooks = {draw:[verticalTimeLineHook,colorPointHook]};
				DATA_INDEX = 1;
			} else {
				this.ctrl.fail(self, 'Invalid type: ' + this.ctrl.getVisualStyle());
				return;
			}
		}
		
		this.plot = $.plot($('#' + id), series, options);

		var previous;
		$('#' + id).bind('plothover', function(event, pos, item) {
			$('#x').text(pos.x.toFixed(2));
			$('#y').text(pos.y.toFixed(2));
			if (item) {
				if (previous != item.datapoint) {
					previous = item.datapoint;
					$('#tooltip').remove();
					var text = item.datapoint[1] + ' ' + uom;
					$('<div id="tooltip" class="tooltip">' + text + '</div>').css({
						position: 'absolute',
						display: 'none',
						top: item.pageY + 15,
						left: item.pageX + 5
					}).appendTo('body').show();
				}
			} else {
				$('#tooltip').remove();
				previous = null;
			}
		});

		function drawSingleValue(v) {
			if (v[1].getClassName && v[1].getClassName().match('.*Distribution$')) {
				function plot(){
					var vs = self.ctrl.getVisibleScale();
					new DistributionPlot(svId, v[1], new Range(vs[0], vs[1], 100), 
							{ yaxis: { min: 0, max: 1 } }
					);
				}
				var title = new Date(v[0][0]).toGMTString();
				if (v[0].length == 2) {
					 title += ' - ' + new Date(v[0][1]).toGMTString()
				}
				if ($('#' + svId).length) {
					$('#' + svId).html('');
					plot();
				} else {
					$('<div id="' + svId + '"></div>').dialog({ 
						close: function() {
							$('#'+svId).remove();
							self.singleValueWindowOpen = false;
						},
						title: title, 
						open: plot, 
						width: 450, 
						resize: plot, 
						height: 450
					});
				}
			}
		}
		
		$('#' + id).unbind('plotclick');
		$('#' + id).bind('plotclick', function(event, pos, item) {
			if (item && item.seriesIndex == DATA_INDEX) {
				self.singleValueWindowOpen = true;
				self.ctrl.callback.selectTime(v[item.dataIndex][0][0]);
			}
		});
		if (self.singleValueWindowOpen) { //single value window is open
			var selectedTimeValue = null;
			for (var i = 0; i < v.length; i++) {
				if ((v[i][0][0] == this.ctrl.getSelectedTime()) || (v[i][0].length == 2 && 
					 v[i][0][0] <= this.ctrl.getSelectedTime() && 
					 v[i][0][1] >= this.ctrl.getSelectedTime())) { 
					drawSingleValue(v[i]); 
					break;
				}
			}
		}
	},
	
	destroy: function () {/*TODO*/}
});
/* vim: set ts=4 sts=4 sw=4 noet ft=javascript fenc=utf-8 */
