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

const initThreshold = 60;
const maxThreshold = 200;
const minThreshold = 1;
const initIntervals = 10;
const maxIntervals = 20;
const minIntervals = 2;

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
	(function () {	
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
	})();

	dialog = {
		error: function (e) {
			$("<div></div>").html(
						'The SOS request failed: <p class="failMessage">' + e 
								+ "</p>").dialog({
				title: "Error", width: 600, buttons: {
					"Ok": function () { $(this).dialog("close"); } 
				},
				close: function (e,ui) { dialog.issue.dialog("close"); },
				open: function (e,ui) {	$(this).parent().children().children(
										'.ui-dialog-titlebar-close').hide(); }
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
					try {
						clients.push(new OpenLayers.SOSClient({
							map: map, scalebar: scaleBar,
							url: $("#sosUrl").val(),
							request: editor.getCode(),
							readyCallback: function () { 
								dialog.issue.dialog("close"); },
							statusCallback: function (stat) {},
							failCallback: dialog.error
						}));
					} catch(e) {
						dialog.error(e);
					}
				},
				"Cancel": function () { dialog.issue.dialog("close"); }
			},
			close: function (e, ui) {
				$("#issueButton").button("enable");
				$(this).parent().children(".ui-dialog-buttonpane").children()
							.children("button").button("enable");
			},
			open: function (e, ui) {
				$(this).parent().children().children(
						'.ui-dialog-titlebar-close').hide();
				$("#issueButton").button("disable");
			}
		})
	};

	(function () {
		var ll1 = new OpenLayers.LonLat( 5.8669, 47.2708)
								.transform(PROJ4326, PROJMERC);
		var ll2 = new OpenLayers.LonLat(15.0436, 55.0591)
								.transform(PROJ4326, PROJMERC);
		bounds = new OpenLayers.Bounds(ll1.lon, ll1.lat, ll2.lon, ll2.lat);
	})();

	/* init sliders */
	(function () {
		var thresholdInput = false;
		var intervalsInput = false;

		function toggleIntervalsInput(){
			if (!intervalsInput) {
				intervalsInput = true;
				$("#intervals").html('<input size="4" value="' 
							+ $("#intervals span").html() + '"/>');
				$("#intervals input").change(function (){
					if ($("#intervals input").val() > maxIntervals) { 
						$("#intervals input").val(maxIntervals); }
					if ($("#intervals input").val() < minIntervals) { 
						$("#intervals input").val(minIntervals); }
					$("#intervalSlider").slider("value", 
							$("#intervals input").val());
					toggleIntervalsInput();
				}).focusout(toggleIntervalsInput);
			} else {
				intervalsInput = false;
				$("#intervals").html('<span>' + $("#intervals input").val() 
																+ '</span>');
				$("#intervals span").click(toggleIntervalsInput).addClass(
						"slider-value");
			}
		}
		function toggleThresholdInput(){
			if (!thresholdInput) {
				thresholdInput = true;
				$("#threshold").html('<input size="4" value="' 
						+ $("#threshold span").html() + '"/>');
				$("#threshold input").change(function () {
					if ($("#threshold input").val() > maxThreshold) { 
						$("#threshold input").val(maxThreshold); }
					if ($("#threshold input").val() < minThreshold) { 
						$("#threshold input").val(minThreshold); }
					$("#thresholdSlider").slider("value", 
							$("#threshold input").val());
					toggleThresholdInput();
				}).focusout(toggleThresholdInput);
			} else {
				thresholdInput = false;
				$("#threshold").html('<span>' + $("#threshold input").val() 
																+ '</span>');
				$("#threshold span").click(toggleThresholdInput)
									.addClass("slider-value");
			}
		}

		function updateLegend() { 
			scaleBar.writeLegend(); 
			$.each(clients, function (i,c){ c.updateForNewScale(); }); 
		}

		$("#thresholdSlider").slider({ animate: true, 
			value: initThreshold, min: minThreshold, max: maxThreshold,
			change: updateLegend,
			slide: function (e, ui) { 
				updateLegend();
				if (thresholdInput) { $("#threshold input").val(ui.value); }
				else { $("#threshold span").html(ui.value); }
			}
		});
		$("#intervalSlider").slider({ animate: true, 
			value: initIntervals, min: minIntervals, max: maxIntervals,
			change: updateLegend,
			slide: function (e, ui) { 
				updateLegend();
				if (intervalsInput) { $("#intervals input").val(ui.value); } 
				else { $("#intervals span").html(ui.value); }
			}
		});
		
		$("#threshold").html('<span class="slider-value">' 
								+ initThreshold + '</span');
		$("#threshold span").click(toggleThresholdInput);
		$("#intervals").html('<span class="slider-value">' 
								+ initIntervals + '</span');
		$("#intervals span").click(toggleIntervalsInput);
	})();

	/* init scalebar */
	scaleBar = new OpenLayers.ScaleBar({
		width: 800, height: 27,
		setLegendHtml: function (html) { $("#scale").html(html); },
		getNumIntervals: function () { 
			return parseInt($("#intervalSlider").slider("value")); },	
		getThreshold: function () {	
			return parseFloat($("#thresholdSlider").slider("value")); }
	});

	/* init map */
	(function () {
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
				new OpenLayers.Layer.Google("Google Physical", {
					type: google.maps.MapTypeId.TERRAIN}),
				new OpenLayers.Layer.Google("Google Satellite", {
					type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22}),
				new OpenLayers.Layer.Google("Google Streets", {
					numZoomLevels: 20}),
				new OpenLayers.Layer.Google("Google Hybrid", {
					type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20})
			]
		});
		$.each(map.layers, function (l){ l.animationEnabled = true; });
		mapControls = {
			mouse: new OpenLayers.Control.MouseDefaults(),
			zoomin: new OpenLayers.Control.ZoomBox({
				title:"Zoom in box", out: false }),
			zoomout: new OpenLayers.Control.ZoomBox({ 
				title:"Zoom out box", out: true }),
			layer: new OpenLayers.Control.LayerSwitcher({ 
				div: $("#layerSwitcher").get()[0], roundedCorner: false })
		};
		$.each(mapControls, function (k,v) { map.addControl(v); });
		map.zoomToExtent(bounds);
	})();
	
	/* create and bind menu */
	(function () {
		function icon(icon) { return { text: false, icons: {primary: icon} } }
		$("#issueButton").button().click(function () { 
			dialog.issue.dialog("open"); });
		$("#scaleButton").button().click(function () { 
			dialog.scale.dialog("open"); });
		$("#layerButton").button().click(function () { 
			dialog.layer.dialog("open"); });
		$("#aboutButton").button(icon("ui-icon-help")).click(function () { 
			dialog.about.dialog("open"); });
		$("#resetZoomButton").button(icon("ui-icon-arrow-4-diag")).click(
			function (){ map.zoomToExtent(bounds); });
		$("#navigate").button(icon("ui-icon-arrow-4")).click(function () { 
			mapControls.zoomin.deactivate();mapControls.zoomout.deactivate();});
		$("#zoomin").button(icon("ui-icon-zoomin")).click(function () {
			mapControls.zoomin.activate(); mapControls.zoomout.deactivate(); });
		$("#zoomout").button(icon("ui-icon-zoomout")).click(function () {
			mapControls.zoomout.activate(); mapControls.zoomin.deactivate(); });
		$("#controlMethod").buttonset();
	})();

	//check if we got an request as parameter
	(function () {
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
			try {
				OpenLayers.Request.GET({
					url: parameters['request'],
					success: function (r) {
						clients.push(new OpenLayers.SOSClient({
							map: map, scalebar: scaleBar,
							url: parameters['url'],
							request: r.responseText,
							readyCallback: function () {},
							statusCallback: function (stat) {},
							failCallback: dialog.error
						}));
					}
				});
			} catch(e) {
				dialog.error(e);
			}
		}
		if (parameters['oc']) {
			try {
				OpenLayers.Request.GET({
					url: parameters['oc'],
					success: function (r) {
						clients.push(new OpenLayers.SOSClient({
							map: map, scalebar: scaleBar,
							oc: (r.responseXML)? r.responseXML : r.responseText,
							readyCallback: function () {},
							statusCallback: function (stat) {},
							failCallback: dialog.error
						}));
					}
				});
			} catch(e) {
				dialog.error(e);
			}
			
		}
	})();
});
