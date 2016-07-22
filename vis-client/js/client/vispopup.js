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
OpenLayers.Popup.VisualizingPopup = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {
	CLASS_NAME: "OpenLayers.Popup.VisualizingPopup",
	svId: 'singlevaluedialog',
	panMapIfOutOfView: true,
	plotId: null,
	feature: null,
	selectTimeCallback: null,

	initialize: function (map, ctrl, feature, selectTimeCallback) {
		this.plotId = 'plot' + new Date().getTime();
		this.feature = feature;
		this.ctrl = ctrl;
		if (this.ctrl == null || this.ctrl == undefined) {
			throw "ctrl can not be null!";
		}
		this.selectTimeCallback = (selectTimeCallback) ? selectTimeCallback : function(){};
		var html;
		if (this.ctrl.getVisualStyle() !== 'exceedance') {
			html = '<div class="bubble"><div><h2>' + this.feature.getHumanReadableObservedProperty()
				+ '@' + this.feature.getFoiId()
				+ ' (<span id="intervalValue"></span>% confidence interval)\
					</h2></div><div class="bubbleContent"><div id="'
				+ this.plotId + '" class="bubblePlot"></div><div id="confidenceSliderContainer">\
					<div id="confidenceSlider"></div></div></div></div>';
		} else {
			html = '<div class="bubble"><div><h2>' + this.feature.getFoiId()
				+ '</h2></div><div class="bubbleContent"><div id="' + this.plotId
				+ '" class="bubblePlot"></div></div></div>';
		}

		OpenLayers.Popup.FramedCloud.prototype.initialize.apply(this, [
			'Feature', this.feature.geometry.getBounds().getCenterLonLat(),
			null, html, null, false, null ]);

		map.addPopup(this, true);
		this.drawContents();
		if (this.ctrl == null || this.ctrl == undefined) {
			throw "ctrl can not be null!";
		}
		if (this.ctrl.getVisualStyle() !== 'exceedance'
			|| this.ctrl.getVisualStyle() !== 'probabilties') {
			this.initConfidenceSlider();
		}
	},

	destroy: function() {
		this.feature = null;
		this.ctrl = null;
		this.plotId = null;
		if ($('#' + this.svId).length) {
			$('#' + this.svId).dialog('close');
			$('#' + this.svId).remove();
		}
		OpenLayers.Popup.FramedCloud.prototype.destroy.apply(this);
	},

	initConfidenceSlider: function() {
		var self = this;
		$('#intervalValue').html(this.ctrl.getSelectedConfidenceInterval());
		$('#confidenceSlider').slider({
			value: this.ctrl.getSelectedConfidenceInterval(),
			orientation: 'vertical', animate: true,
			min: 1, max: 99, step: 1,
			change: function(e, ui) {
				self.ctrl.setSelectedConfidenceInterval(ui.value);
				self.drawContents();
			},
			slide: function (e, ui) {
				$('#intervalValue').html(ui.value);
			}
		});
	},

	drawContents: function() {
		var self = this;
		var v = this.feature.getValues();
		var p = (100-(100-this.ctrl.getSelectedConfidenceInterval())/2)/100;

		function colorPointHook(plot, ctx) {
			for (var i = 0; i < plot.getData().length; i++) {
				if (plot.getData()[i].points.show) {
					var data = plot.getData()[i].data;
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
							   + plot.getAxes().yaxis.p2c(data[j][1]);
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
		var label = (self.ctrl.getVisualStyle() === 'probabilities'
						|| self.ctrl.getVisualStyle() === 'exceedance') ? '%' : this.feature.getUom();
		var fontSize = parseInt($('body').css('font-size'));
		var fontFamily = $('body').css('font-family').split(',')[0];
		var options = {
			xaxis: {
				color: '#B6B6B6',
				mode: 'time',
				axisLabel: 'Time',
				axisLabelUseCanvas: true,
				axisLabelFontSizePixels: fontSize,
				axisLabelFontFamily: fontFamily
			},
			yaxis: {
				color: '#B6B6B6',
				min: min,
				max: max,
				axisLabel: label,
				axisLabelUseCanvas: true,
				axisLabelFontSizePixels: fontSize,
				axisLabelFontFamily: fontFamily
			},
			grid:  {
				color: '#B6B6B6',
				hoverable: true,
				clickable: true,
				mouseActiveRadius: 25
			},
			lines: { show: true }
		};

		var series;

		if (this.ctrl.getVisualStyle() === 'probabilities') {
			series = [{ data: [], color: '#4F4F4F', points: { show: true }}];
			options.hooks = {draw:[verticalTimeLineHook,colorPointHook]};
			for (var i = 0; i < v.length; i++) {
				for (var j = 0; j < v[i][0].length; j++) {
					series[0].data.push([v[i][0][j], v[i][1].probability]);
				}
			}
		} else if (this.ctrl.getVisualStyle() === 'exceedance') {
			var d = [];
			var t = this.ctrl.getThreshold();
			for (var i = 0; i < v.length; i++) {
				var ep = OpenLayers.SOS.ObservationSeries.prototype.calculateExceedanceProbability(t,v[i][1]);
				for (var j = 0; j < v[i][0].length; j++) {
					d.push([v[i][0][j], ep]);
				}
			}
			series = [{ data: d, color: '#4F4F4F', points: { show: true }}];
			options.hooks = { draw: [ verticalTimeLineHook, colorPointHook ] };
		} else {
			var u = [], l = [], m = [];
			var realisations = [];
			for (var i = 0; i < v.length; i++) {
				var time = v[i][0], value = v[i][1];
				if (value.length) { //realisation
					for (var j = 0; j < value.length; j++) {
						for (var k = 0; k < time.length; k++) {
							realisations.push([time[k], value[j]]);
						}
					}
				} else {
					if (typeof(value) === 'number') {
						value = [null, value, null];
					} else if (value.getClassName && value.getClassName().match('.*Distribution$')) {
						var cI = value.getConfidenceInterval(p);
						value = [ cI[1], value.getMean(), cI[0] ];
					} else {
						return this.ctrl.fail(this, "Unknown Value Type: " + value);
					}
					for (var j = 0; j < time.length; j++) {
						l.push([time[j], value[0]]);
						m.push([time[j], value[1]]);
						u.push([time[j], value[2]]);
					}
				}
			}
			if (this.ctrl.getVisualStyle() === 'bars') {
				series = [{ data: m, color: '#4F4F4F', points: { show: true } }];
				options.hooks = {draw:[verticalTimeLineHook,errorBarHook,colorPointHook]};
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
			} else {
				return this.ctrl.fail(this, 'Invalid type: ' + this.ctrl.getVisualStyle());
			}
			if (realisations.length > 0) {
				series.push({
					data: realisations,
					points: { show: true },
					lines: { show: false }
				});
			}
		}

		this.plot = $.plot($('#' + this.plotId), series, options);

		$('#' + this.plotId).bind('plothover', function(event, pos, item) {
			$('#x').text(pos.x.toFixed(2));
			$('#y').text(pos.y.toFixed(2));
			if (item) {
				var tooltip = item.seriesIndex+'.'+item.dataIndex;
				if (self.previousTooltip != tooltip) {
					self.previousTooltip = tooltip;
					$('#tooltip').remove();
					var text = item.datapoint[1].toFixed(2) + ' ';
					text += (self.ctrl.getVisualStyle() === 'probabilities' || self.ctrl.getVisualStyle() === 'exceedance') ? '%' : self.feature.getUom();
					$('<div id="tooltip" class="tooltip">' + text + '</div>').css({
						position: 'absolute',
						display: 'none',
						top: item.pageY + 15,
						left: item.pageX + 5
					}).appendTo('body').show();
				}
			} else {
				$('#tooltip').remove();
				self.previousTooltip = null;
			}
		});

		function drawSingleValue(v) {
			if (v[1].getClassName && v[1].getClassName().match('.*Distribution$')) {
				function plot(){
					var vs = self.ctrl.getVisibleScale();
					new DistributionPlot(self.svId, v[1], new Range(vs[0], vs[1], 100)).setFill(true);
				}
				var title = new Date(v[0][0]).toGMTString();
				if (v[0].length == 2) {
					 title += ' - ' + new Date(v[0][1]).toGMTString();
				}
				if ($('#' + self.svId).length) {
					$('#' + self.svId).html('');
					plot();
				} else {
					$('<div id="' + self.svId + '"></div>').dialog({
						close: function() {
							$('#'+self.svId).remove();
							self.singleValueWindowOpen = false;
						},
						title: title,
						open: plot,
						width: 450,
						resize: plot,
						height: 450
					});
				}
			} else if (v[1].length) { // realisations
				//TODO
			}
		}

		$('#' + this.plotId).unbind('plotclick');
		$('#' + this.plotId).bind('plotclick', function(event, pos, item) {
			if (item && item.series.points.show) {
				self.singleValueWindowOpen = true;
				self.selectTimeCallback(item.datapoint[0]);
			}
		});
		if (this.singleValueWindowOpen) { //single value window is open
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
	}
});
