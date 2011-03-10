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
OpenLayers.SOSClient = OpenLayers.Class({
    CLASS_NAME: "OpenLayers.SOSClient",
	id: null,
	url: null,
	map: null,
	statusCallback: null,
	readyCallback: null,
	failCallback: null,
	request: null,
	getObsFormat: new OpenLayers.Format.ObservationCollection(),
	foiFeatureMapping: null,
	scalebar: null,
	initialize: function (options) {
        OpenLayers.Util.extend(this, options);
		this.id = sosClientId++;
		this.statusCallback("Sending Request");
    	OpenLayers.Request.POST({
         	url: this.url,
         	data: this.request,
         	scope: this,
         	success: this.generateFeatures,
			failure: this.failCallback
         });
    },
	generateFeatures: function (resp) {
		this.statusCallback("Parsing response");
		var obs;
		try {
			obs = this.getObsFormat.read(resp.responseText, 
					this.map.getProjectionObject());
		} catch(e) {
			if (!e) {
				this.failCallback("Unknown Error");
				return;
			} else if (typeof(e) === "string") {
				this.failCallback(e);
			} else if (e.exceptions) {
				var message = ""; 
				for (var i = 0; i < e.exceptions.length; i++) {
					message += e.exceptions[i].exceptionCode +": ";
					for (var j = 0; j < e.exceptions[i].exceptionTexts.length; j++) {
						message += e.exceptions[i].exceptionTexts[j] + "\n";
					}
				}
				this.failCallback(message);
				return;
			} else {
				throw e;
			}
		}
		if (!obs || obs.length === 0) {
			this.failCallback("Response does not offer any observations.");
			return;
		}
		this.statusCallback("Adding "+ obs.length +" Observations to the map.");
		var mapping = {};
		for (var i = 0; i < obs.length; i++) {
			var foi = obs[i].fois[0].features[0];
			if (!mapping[foi.attributes.id]) {
				 mapping[foi.attributes.id] = {feature: foi, values: []};
			}
			attr = {
				resultValue: obs[i].result.value,
				uom: obs[i].result.uom,
				samplingTime: obs[i].samplingTime,
				observedProperty: obs[i].observedProperty,
				procedure: obs[i].procedure
			}
			mapping[foi.attributes.id].values.push(attr);
		}
		this.features = [];
		for (key in mapping) {
			var attributes;
			var count = mapping[key].values.length;
			if (count === 1) {
				attributes = mapping[key].values[0];
				attributes.isMultiFeature = false;
			} else {
				/* sort values by time */
				mapping[key].values.sort(function (a, b) {
					var time1, time2;
					if (a.samplingTime.timeInstant) {
						if (b.samplingTime.timeInstant) {
							time1 = a.samplingTime.timeInstant.timePosition;
							time2 = b.samplingTime.timeInstant.timePosition;
						} else {
							time1 = a.samplingTime.timeInstant.timePosition;
							time2 = b.samplingTime.timePeriod.beginPosition;
						}
					} else {
						if (b.samplingTime.timeInstant){
							time1 = a.samplingTime.timePeriod.beginPosition;
							time2 = b.samplingTime.timeInstant.timePosition;
						} else {
							time1 = a.samplingTime.timePeriod.beginPosition;
							time2 = b.samplingTime.timePeriod.beginPosition;
						}
					}
					return time1.getTime() - time2.getTime();
				});
				var resultValue = 0;
				for (var i = 0; i < count; i++) {
					resultValue += mapping[key].values[i].resultValue;
				}
				attributes = {
					isMultiFeature: true,
					resultValue: parseFloat((resultValue/count).toFixed(2)),
					values: mapping[key].values
				}
			}
			attributes.id = key;
			mapping[key].feature.attributes = attributes;
			this.features.push(mapping[key].feature);
		}
		this.generateLayer();
	},
	generateLayer: function () {
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
				this.plot.triggerRedrawOverlay();
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
/*
	onFeatureSelect: function (feature) {
		writeValueLine = function (scale, attr) {
			var html = '<tr><td>';
			if (attr.samplingTime.timeInstant) {
				html += attr.samplingTime.timeInstant.timePosition.toISOString();
			} else if (attr.samplingTime.timePeriod) {
				html += attr.samplingTime.timePeriod.beginPosition.toISOString()
					 + " - " 
					 + attr.samplingTime.timePeriod.endPosition.toISOString();
			} else {
				html += "Unknown SamplingTime Format";
			}
			html += '</td><td>';
			html += '<span class="scaleIndicator" style="background-color:' 
				 + scale.getColorForResultValue(attr.resultValue) + '">&#160;&#160;&#160;&#160;</span>';
			html += " " + attr.resultValue.toFixed(2) + " " + attr.uom;
			html +=	"</td></tr>";
			return html;
		};
		var html = "<h2>" + feature.attributes.id + "</h2>";
		html += '<table class="resultTable">';
		html += '<tr><th>Time</th><th>Value</th></tr>';
		if (feature.attributes.isMultiFeature) {
			for (var i = 0;i < feature.attributes.values.length; i++) {
				html += writeValueLine(this.scalebar, feature.attributes.values[i]);
			}
		} else {
			html += writeValueLine(this.scalebar, feature.attributes);
		}
		html += "</table>";
		var ctrls = this.map.getControlsByClass("OpenLayers.Control.SelectFeature");
		feature.popup = new OpenLayers.Popup.FramedCloud("Feature",
			feature.geometry.getBounds().getCenterLonLat(),
			null, html, null, false, null);
		feature.popup.panMapIfOutOfView = true;
		this.selectedFeature = feature;
		this.map.addPopup(feature.popup, true);
	},
*/
	onFeatureSelect: function (feature) {
		function random(min, max) {
			return (min + parseInt(Math.random() * (max - min + 1)));
		}
		function getValue(attr) {
			var time;
			var value;
			var sd;
			
			if (attr.samplingTime.timeInstant) {
				time = attr.samplingTime.timeInstant.timePosition.getTime();
			} else if (attr.samplingTime.timePeriod) {
				time = [attr.samplingTime.timePeriod.beginPosition.getTime(),
						attr.samplingTime.timePeriod.endPosition.getTime()];
			} else {
				throw "Unknown SamplingTime Format";
			}
			value = attr.resultValue;
			sd = random(1,3);
			return [time,value,sd];
		}

		var values = [], uom;
		if (feature.attributes.isMultiFeature) {
			for (var i = 0;i < feature.attributes.values.length; i++) {
				if (!uom) uom = feature.attributes.values[i].uom;
				values.push(getValue(feature.attributes.values[i]));
			}
		} else {
			if (!uom) uom = feature.attributes.uom;
			values.push(getValue(feature.attributes));
		}

		var id = "plot" + new Date().getTime();
		var html = "<div class='bubble'><h2>" + feature.attributes.id + "</h2>";
		html += '<div id="' + id + '" class="bubblePlot"></div></div>';
		var ctrls = this.map.getControlsByClass("OpenLayers.Control.SelectFeature");
		feature.popup = new OpenLayers.Popup.FramedCloud("Feature",
			feature.geometry.getBounds().getCenterLonLat(),
			null, html, null, false, null);
		feature.popup.panMapIfOutOfView = true;
		this.selectedFeature = feature;
		this.map.addPopup(feature.popup, true);
		this.draw(id,values,uom,2.4);
	},
	draw: function(id, v, uom, timesInterval) {
		var u = [], l = [], m = [];
		for (var i = 0; i < v.length; i++) {
			if (typeof(v[i][0]) === "number") {
				if (v[i][2]) {
					l.push([v[i][0], v[i][1] - timesInterval * v[i][2]]);
					u.push([v[i][0], v[i][1] + timesInterval * v[i][2]]);
				}
				m.push([v[i][0], v[i][1]]);
			} else {
				if (v[i][2]) {
					l.push([v[i][0][0], v[i][1] - timesInterval * v[i][2]]);
					l.push([v[i][0][1], v[i][1] - timesInterval * v[i][2]]);
					u.push([v[i][0][0], v[i][1] + timesInterval * v[i][2]]);
					u.push([v[i][0][1], v[i][1] + timesInterval * v[i][2]]);
				}
				m.push([v[i][0][0], v[i][1]]);
				m.push([v[i][0][1], v[i][1]]);
			}
		}
		l.sort(function(a, b) {
			return ((a[0] > b[0]) ? -1 : ((a[0] < b[0]) ? 1 : 0)); 
		}); 
		var scale = this.scalebar;
		function raw(plot, ctx) {
			var data = plot.getData()[1].data;
			for (var j = 0; j < data.length; j++) {
				var x = plot.getPlotOffset().left + plot.getAxes().xaxis.p2c(data[j][0]);
				var y = plot.getPlotOffset().top + plot.getAxes().yaxis.p2c(data[j][1]);
				ctx.lineWidth = 0;
				ctx.beginPath();
				ctx.arc(x, y, 3, 0, Math.PI * 2, true);
				ctx.closePath();            
				ctx.fillStyle = scale.getColorForResultValue(data[j][1]);
				ctx.fill();
			}    
		}  
		this.plot = $.plot($('#' + id), [{ 
				color: "red", 
				data:u.concat(l), 
				lines: { fill: true } 
			},{ 
				color: "black",
				points: { show: true },
				data:m
			}], {
				xaxis: { mode: "time", color: "#B6B6B6" },
				yaxis: { color: "#B6B6B6" }, 
				lines: { show: true },
				grid: {
					color: "#B6B6B6",
					hoverable: true,
					mouseActiveRadius: 25
				},
				hooks: { draw: [raw] }
			}
		);
		var previous;
        $('#'+id).bind("plothover", function(event, pos, item) {
            $("#x").text(pos.x.toFixed(2));
            $("#y").text(pos.y.toFixed(2));
            if (item) {
                if (previous != item.datapoint) {
                    previous = item.datapoint;
                    $("#tooltip").remove();
                    var text = item.datapoint[1] + " " + uom;
                    var color = item.series.color;
					$('<div id="tooltip">' + text + '</div>').css( {
			            position: 'absolute',
			            display: 'none',
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
	destroy: function () {
		this.map.removeLayer(this.layer);
		this.map.removeControl(this.control);
		this.layer.destroy();
		this.ctrl.destroy();
		this.layer = null;
		this.ctrl = null;
		this.map = null;
		this.request = null;
		this.url = null;
		this.getObsFormat = null;
		while (this.features.length > 0) {
			this.features.pop().destroy();
		}
		this.features = null;
	}
});
