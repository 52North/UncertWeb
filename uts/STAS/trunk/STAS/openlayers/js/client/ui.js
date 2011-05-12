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
const PROJ4326 = new OpenLayers.Projection("EPSG:4326");
const PROJMERC = new OpenLayers.Projection("EPSG:900913");
const threshold = { init: [0, 50], range: [-100, 150], step: 0.1 };
const intervals = { init: 10, range: [2, 20] };
const includeGoogle = false;

$(function (){
	OpenLayers.ImgPath = "img/";
	var editor;
	var clients = [];
	var scaleBar;
	var map;
	var bounds;
	var dialog;
	var mapControls;

	/* load default url & request and init editor */
	$("#sosUrl").val("http://giv-uw.uni-muenster.de:8080/STAS-SOS/sos");
	//$("#sosUrl").val("http://giv-uw.uni-muenster.de:8080/AQE/sos");
	//$("#sosUrl").val("http://localhost:8080/sos/sos");
	OpenLayers.Request.GET({
		url: "xml/req/stas.xml",
		//url: "xml/req/aqe.xml",
		callback: function (r) {
			$("#sosRequest").val(r.responseText);
			editor = CodeMirror.fromTextArea("sosRequest", {
				height: "370px",
				parserfile: "parsexml.js",
				stylesheet: "css/xmlcolors.css",
				path: "js/codemirror/",
				lineNumbers: true
			});
		}			
	});

	dialog = {
		error: function (e) {
			$("<div></div>").html('The SOS request failed: <p class="failMessage">' + e + "</p>").dialog({
				title: "Error", width: 600, buttons: {
					"Ok": function () { $(this).dialog("close"); } 
				},  
				close: function (e,ui) { 
					dialog.issue.dialog("close"); 
				},
				open: function (e,ui) {	
					$(this).parent().children().children('.ui-dialog-titlebar-close').hide(); 
				}
			});
		},
		layer: $("#layerDialog").dialog({
			title: "Layer Settings",
			autoOpen: false,
			close: function () {
				mapControls.layer.minimizeControl();
				$("#layerButton").button("enable");
			},
			open: function () {
				mapControls.layer.maximizeControl();
				$("#layerButton").button("disable");
			}
		}),
		scale: $("#scaleDialog").dialog({
			title: "Scale Settings",
			autoOpen: false,
			close: function () { $("#scaleButton").button("enable"); },
			open: function () { $("#scaleButton").button("disable"); }
		}),
		about: $("#aboutDialog").dialog({
			title: "OpenLayers SOS Client",
			autoOpen: false,
			close: function () { $("#aboutButton").button("enable"); },
			open: function () { $("#aboutButton").button("disable"); }
		}),
		issue : $("#issueDialog").dialog({
			title: "Issue SOS Request",
			width: 800,
			modal: true,
			autoOpen: false,
			buttons: {
				"OK": function (e, ui) {
					$(".ui-dialog-buttonpane button", ui).button("disable");
					clients.push(new OpenLayers.SOS.Client({
						map: map, 
						scalebar: scaleBar,
						url: $("#sosUrl").val(),
						request: editor.getCode(),
						readyCallback: function (info) { 
							updateTimeSlider(info);
							dialog.issue.dialog("close"); 
						},
						timeSelectCallback: function(t) { $('#timeSlider').slider("option", "value",  t); },
						visibleScale: getThresholdMinMax(),
						failCallback: dialog.error
					}));
				},
				"Cancel": function () { 
					dialog.issue.dialog("close"); 
				}
			},
			close: function (e, ui) {
				$("#issueButton").button("enable");
				$(this).parent().children(".ui-dialog-buttonpane").children().children("button").button("enable");
			},
			open: function (e, ui) {
				$(this).parent().children().children('.ui-dialog-titlebar-close').hide();
				$("#issueButton").button("disable");
			}
		})
	};

	function updateLegend() { 
		var minmax = getThresholdMinMax();
		scaleBar.update(minmax[0], minmax[1], $('#intervalSlider').slider("value"));
		$.each(clients, function (i,c){ 
			c.updateForNewScale(minmax); 
		}); 
	}
	
	function getThresholdMinMax() {
		return [ $('#thresholdSlider').slider("values", 0), 
				 $('#thresholdSlider').slider("values", 1) ];
	}
	
	function changeForTime(time) {
		$.each(clients, function (i,c){ 
			c.updateForNewTime(time);
		});
		updateTimeLabel();
	}
	
	function updateTimeLabel() {
		$("#timeSliderValue").html(new Date(parseInt($("#timeSlider").slider("value"))).toUTCString());
	}
	
	function updateTimeSlider(info) {
		if ($('#timeSlider').slider("option", "disabled")) {
			$('#timeSlider').slider("option", "min",  info.time.min);
			$('#timeSlider').slider("option", "max",  info.time.max);
			$('#timeSlider').slider("option", "step", info.time.step);
			$('#timeSlider').slider("enable");
		} else {	
			var curStep = $('#timeSlider').slider("option", "step");
			if (curStep > info.time.step && (curStep % info.time.step) == 0) {
				$('#timeSlider').slider("option", "step", info.time.step);
			} else if (curStep != info.time.step && ((info.time.step % curStep) != 0)) {			
				map.removeLayer(info.layer);
				dialog.error("Incompatible sampling time steps: " + curStep + " and " + info.time.step + ".");
				return;
			}
			if ($('#timeSlider').slider("option", "min") > info.time.min)
				$('#timeSlider').slider("option", "min",   info.time.min);
			if ($('#timeSlider').slider("option", "max") < info.time.max)
				$('#timeSlider').slider("option", "max",   info.time.max);
		}
		$('#timeSlider').slider("option", "value", info.time.min);
		changeForTime(info.time.min);
	}


	$("#thresholdSlider").slider({ 
		animate: true, range: true,
		values: threshold.init,
		min: threshold.range[0],
		max: threshold.range[1],
		step: threshold.step,
		change: updateLegend,
		slide: function (e, ui) { 
			$("#threshold span").html(ui.values[0] + " - " + ui.values[1]);
		}
	});
	$("#intervalSlider").slider({ 
		animate: true, 
		value: intervals.init, 
		min: intervals.range[0], 
		max: intervals.range[1],
		change: updateLegend,
		slide: function (e, ui) { 
			$("#intervals span").html(ui.value);
		}
	});
	
	$("#threshold").html('<span class="slider-value">' 
		+ $("#thresholdSlider").slider("values", 0) + ' - ' 
		+ $("#thresholdSlider").slider("values", 1) + '</span');
	$("#intervals").html('<span class="slider-value">' 
		+ $("#intervalSlider").slider("value") + '</span');

	/* init scalebar */
	scaleBar = new OpenLayers.SOS.ScaleBar({
		width: 800, height: 27,
		setLegendHtml: function (html) { $("#scale").html(html); },
		numIntervals: $("#intervalSlider").slider("value"),
		minimum: $("#thresholdSlider").slider("values", 0),
		maximum: $("#thresholdSlider").slider("values", 1),
	});

	var now = new Date().getTime();
	$("#timeSlider").slider({
		animate: true, value: now, 
		max: now, min: 0, step: now, 
		slide: updateTimeLabel,
		change: function(e, ui) { changeForTime(parseFloat(ui.value)); }
	}).slider("disable");			
	updateTimeLabel();
	

	/* init map */
	var ll1 = new OpenLayers.LonLat( 5.8669, 47.2708).transform(PROJ4326, PROJMERC);
	var ll2 = new OpenLayers.LonLat(15.0436, 55.0591).transform(PROJ4326, PROJMERC);
	bounds = new OpenLayers.Bounds(ll1.lon, ll1.lat, ll2.lon, ll2.lat);

	map = new OpenLayers.Map({
		div: "map",
		units: "m",
		numZoomLevels: 22,
		projection: PROJMERC,
		displayProjection: PROJ4326,
		controls: [],
		layers: [
			new OpenLayers.Layer.OSM.Mapnik("OpenStreetMap Mapnik"),
			new OpenLayers.Layer.OSM.Osmarender("OpenStreetMap Osmarender"),
			new OpenLayers.Layer.OSM.CycleMap("OpenStreetMap CycleMap"),
		]
	});
	if (includeGoogle) {
		map.addLayer(new OpenLayers.Layer.Google("Google Physical", { type: google.maps.MapTypeId.TERRAIN}));
		map.addLayer(new OpenLayers.Layer.Google("Google Satellite", { type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22 }));
		map.addLayer(new OpenLayers.Layer.Google("Google Streets", { numZoomLevels: 20 }));
		map.addLayer(new OpenLayers.Layer.Google("Google Hybrid", { type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20 }));
	}
	$.each(map.layers, function (l){ l.animationEnabled = true; });
	mapControls = {
		mouse: new OpenLayers.Control.MouseDefaults(),
		zoomin: new OpenLayers.Control.ZoomBox({
			title:"Zoom in box", out: 
			false 
		}),
		zoomout: new OpenLayers.Control.ZoomBox({ 
			title:"Zoom out box",
			out: true 
		}),
		layer: new OpenLayers.Control.LayerSwitcher({ 
			div: $("#layerSwitcher").get()[0], 
			roundedCorner: false 
		})
	};
	$.each(mapControls, function (k,v) { map.addControl(v); });
	map.zoomToExtent(bounds);

	/* create and bind menu */
	function icon(icon) { 
		return { 
			text: false, 
			icons: {
				primary: icon
			} 
		};
	}
	$("#issueButton").button().click(function () { 
		dialog.issue.dialog("open"); 
	});
	$("#scaleButton").button().click(function () { 
		dialog.scale.dialog("open"); 
	});
	$("#layerButton").button().click(function () { 
		dialog.layer.dialog("open"); 
	});
	$("#aboutButton").button(icon("ui-icon-help")).click(function () { 
		dialog.about.dialog("open"); 
	});
	$("#resetZoomButton").button(icon("ui-icon-arrow-4-diag")).click(function (){ 
		map.zoomToExtent(bounds); 
	});
	$("#navigate").button(icon("ui-icon-arrow-4")).click(function () { 
		mapControls.zoomin.deactivate();
		mapControls.zoomout.deactivate();
	});
	$("#zoomin").button(icon("ui-icon-zoomin")).click(function () { 
		mapControls.zoomin.activate(); 
		mapControls.zoomout.deactivate(); 
	});
	$("#zoomout").button(icon("ui-icon-zoomout")).click(function () { 
		mapControls.zoomout.activate(); 
		mapControls.zoomin.deactivate(); 
	});
	$("#controlMethod").buttonset();

	//check if we got an request as parameter
	var parameters = {};
	var str = document.location.search.substr(1, 
		document.location.search.length);
	if (str != '') {
		params = str.split('&');
		for (var i = 0; i < params.length; i++) {
			v = '';
			kvPair = params[i].split('=');
			if (kvPair.length > 1) {
				v = kvPair[1];
			}
			parameters[unescape(kvPair[0])] = unescape(v);
		}
	}
	if (parameters['url']) {
		OpenLayers.Request.GET({
			url: parameters['request'],
			success: function (r) {
				clients.push(new OpenLayers.SOS.Client({
					map: map, scalebar: scaleBar,
					url: parameters['url'],
					visibleScale: getThresholdMinMax(),
					request: r.responseText,
					readyCallback: updateTimeSlider,
					timeSelectCallback: function(t) { $('#timeSlider').slider("option", "value",  t); },
					failCallback: dialog.error
				}));
			}
		});
	}
	if (parameters['oc']) {
		OpenLayers.Request.GET({
			url: parameters['oc'],
			success: function (r) {
				clients.push(new OpenLayers.SOS.Client({
					map: map, scalebar: scaleBar,
					oc: (r.responseXML)? r.responseXML : r.responseText,
					visibleScale: getThresholdMinMax(),
					readyCallback: updateTimeSlider,
					timeSelectCallback: function(t) { $('#timeSlider').slider("option", "value",  t); },
					failCallback: dialog.error
				}));
			}
		});
		}
	if (parameters['json']) {
		OpenLayers.Request.GET({
			url: parameters['json'],
			callback: function(r) {
				clients.push(new OpenLayers.SOS.Client({
					map: map, scalebar: scaleBar,
					json: r.responseText,
					visibleScale: getThresholdMinMax(),
					readyCallback: updateTimeSlider,
					timeSelectCallback: function(t) { $('#timeSlider').slider("option", "value",  t); },
					failCallback: dialog.error
				}));
			}
		});
	}
});
/* vim: set ts=4 sts=4 sw=4 noet ft=javascript fenc=utf-8 */
