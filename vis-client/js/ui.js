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
OpenLayers.ImgPath = 'img/';
const PROJ4326 = new OpenLayers.Projection('EPSG:4326');
const PROJMERC = new OpenLayers.Projection('EPSG:900913');

function error(e) {
	$('<div><p class="failMessage">' + e + '</p></div>').dialog({
		title: 'Failure', width: 600, 
		buttons: {
			'Ok': function () { $(this).dialog('close'); }
		},  
		close: function (e,ui) {},
		open: function (e,ui) {	
			$(this).parent().children().children('.ui-dialog-titlebar-close').hide(); 
		}
	});
}


var now = new Date();
var editor;
var map;
var mapControls;
var bounds; 
var ctrl;

function init() {
	/* load default url & request and init editor */
	$('#sosUrl').val(defaultUrl);
	if (defaultReq) {
		OpenLayers.Request.GET({
			url: defaultReq,
			callback: function (r) {
				$('#sosRequest').val(r.responseText);
				editor = CodeMirror.fromTextArea('sosRequest', {
					lineNumbers: true,
					height: '420px',
					parserfile: 'parsexml.js',
					stylesheet: 'css/xmlcolors.css',
					path: 'js/codemirror/'
				});
			}
		});
	} else {
		editor = CodeMirror.fromTextArea('sosRequest', {
			lineNumbers: true,
			height: '420px',
			parserfile: 'parsexml.js',
			stylesheet: 'css/xmlcolors.css',
			path: 'js/codemirror/'
		});
	}

	
	function updateLegend() { 
		var min = $('#thresholdSlider').slider('values', 0); 
		var max = $('#thresholdSlider').slider('values', 1);
		var ints = $('#intervalSlider').slider('value');
		ctrl.updateScale(min, max, ints);
	}

	function updateTimeLabel(date) {
		$('#timeSliderValue').html(date.toUTCString());
	}
	
	function updateIntervalLabel(ints) {
		$('#intervals').html(ints);
	}
	
	function updateThresholdLabel(minmax) {
		var uom = ctrl.getUom();
		uom = (uom) ? ' ' + uom : '';
		$('#threshold1').html(minmax[0].toFixed(2) + uom);
		$('#threshold2').html(minmax[1].toFixed(2) + uom);
	}

	$('#thresholdSlider').slider({ 
		animate: true, 
		range: true, 
		values: threshold.init, 
		min: threshold.range[0], 
		max: threshold.range[1],
		step: threshold.step, 
		change: updateLegend, 
		slide: function (e, ui) { updateThresholdLabel(ui.values); }
	});
	$('#intervalSlider').slider({
		animate: true, 
		value: intervals.init, 
		min: intervals.range[0], 
		max: intervals.range[1], 
		change: updateLegend,
		slide: function (e, ui) { updateIntervalLabel(ui.value); }
	});
	
	$('#timeSlider').slider({ 
		animate: true, 
		min: 0, 
		max: now.getTime(), 
		step: now.getTime(), 
		value: now.getTime(), 
		slide: function(e, ui) { updateTimeLabel(new Date(ui.value)); },
		change: function(e, ui) { ctrl.selectTime(parseInt(ui.value)); }
	}).slider('disable');
	
	var scale = new OpenLayers.SOS.ScaleBar({ 
		setLegendHtml: function (html) { 
			$('#scale').html(html); 
		},
		ints: intervals.init,
		min: threshold.init[0],
		max: threshold.init[1],
		height: 27,
		width: 800 
	});
	scale.getStyle();


	/* init map */
	map = new OpenLayers.Map({ 
		div: 'map', 
		units: 'm', 
		numZoomLevels: 22,
		projection: PROJMERC, 
		displayProjection: PROJ4326, 
		controls: [],
		layers: [
			new OpenLayers.Layer.OSM.Mapnik('OpenStreetMap Mapnik'),
			new OpenLayers.Layer.OSM.Osmarender('OpenStreetMap Osmarender'),
			new OpenLayers.Layer.OSM.CycleMap('OpenStreetMap CycleMap'),
			new OpenLayers.Layer.Google('Google Streets', {}),
			new OpenLayers.Layer.Google('Google Satellite', { 
				type: google.maps.MapTypeId.SATELLITE 
			}),
			new OpenLayers.Layer.Google('Google Hybrid', { 
				type: google.maps.MapTypeId.HYBRID 
			}),
			new OpenLayers.Layer.Google('Google Physical', { 
				type: google.maps.MapTypeId.TERRAIN
			}),
			new OpenLayers.Layer.OSM("Empty", "img/empty_tile.gif")
		]
	});
	bounds = new OpenLayers.Bounds(5.8669,47.2708,15.0436,55.0591).transform(PROJ4326, PROJMERC);
	map.zoomToExtent(bounds);
	
	$.each(mapControls = {
		mouse: new OpenLayers.Control.MouseDefaults(),
		zoomin: new OpenLayers.Control.ZoomBox({ 
			title:'Zoom in box', out: false 
		}),
		zoomout: new OpenLayers.Control.ZoomBox({ 
			title:'Zoom out box', out: true 
		}),
		layer: new OpenLayers.Control.AdvancedLayerSwitcher('layerSwitcher')
	}, function (k,v) { map.addControl(v); });


	/* create and bind menu */
	function icon(icon) { 
		return { text: false, icons: { primary: icon } }; 
	}
	
	$('#resetZoom').button(icon('ui-icon-arrow-4-diag')).click(function (){ 
			var bounds = new OpenLayers.Bounds(-180,-65,180,84).transform(PROJ4326,PROJMERC);
			map.zoomToExtent(bounds, true);
	});
	
	$('#navigate').button(icon('ui-icon-arrow-4')).click(function () { 
		mapControls.zoomin.deactivate(); 
		mapControls.zoomout.deactivate(); 
	});
	
	$('#zoomin').button(icon('ui-icon-zoomin')).click(function () { 
		mapControls.zoomin.activate(); 
		mapControls.zoomout.deactivate(); 
	});
	
	$('#zoomout').button(icon('ui-icon-zoomout')).click(function () { 
		mapControls.zoomout.activate(); 
		mapControls.zoomin.deactivate(); 
	});
		
	$('#exceedance').button().click(function () {
		$('#thresholdSlider').slider('disable');
		$('#excee-prob-threshold').removeAttr("disabled");
		ctrl.switchToMode('exceedance', $('#excee-prob-threshold').val());
	});
	
	$('#excee-prob-threshold').change(function (ev) {
		var t = parseFloat($(this).val());
		if (isNaN(t))
			$(this).val(t = 100);
		ctrl.switchToMode('exceedance', t);
	});
	
	$('#convInterval').button().click(function(){
		$('#thresholdSlider').slider('enable');
		$('#excee-prob-threshold').attr("disabled", true);
		ctrl.switchToMode('intervals'); 
	});
	
	$('#errorBars').button().click(function(){
		$('#thresholdSlider').slider('enable');		
		$('#excee-prob-threshold').attr("disabled", true);
		ctrl.switchToMode('bars'); 
	});

	$('#send').button().click(function () {
		$(this).button('disable');
		var mime;
		switch ($("#mime").val()) {
			case "O&M Vector data": mime = "application/xml";			  break;
			case "O&M2 Raster data":  mime = "application/vnd.ogc.om+xml"; break;
			case "JSOM":			 mime = "application/jsom";			  break;
			case "NetCDF":			 mime = "application/netcdf";		  break;
			case "GeoTIFF":			 mime = "image/geotiff";			  break;
		}

		ctrl.addLayer({
			url: $('#sosUrl').val(),
			request: editor.getCode(),
			mime: mime
		});
	});
	
	$("#aboutButton").button(icon("ui-icon-help")).click(function () { 
		$("#about").dialog({
			title: "OpenLayers SOS Client",
			autoOpen: false,
			close: function () { $("#aboutButton").button("enable"); },
			open: function () { $("#aboutButton").button("disable"); }
		}).dialog("open");
	});	
	
	$('#viewChooser').buttonset();
	$('#visualizationStyle').buttonset();
	$('#controlMethod').buttonset();

	// the tab loses focus/mouseover/everything when using a select drop down... so...
	$('#main-nav').children('.main-nav-item').children('a').addClass('ui-button-text');
	$('#main-nav').children('.main-nav-item').click(function(ev) {
		$(this).parent().children('.main-nav-item-active')
			   .removeClass('main-nav-item-active');
		var dd = $(this).find('.main-nav-dd');
		dd.css('left', Math.min(
			$(this).find('.main-nav-tab').position().left, 
			$('#main-nav').width() - dd.width()+20));
		$(this).addClass('main-nav-item-active');
	});
	$(document).click(function(ev) {
		if (!$(ev.srcElement).hasClass('.main-nav-item') 
				&& $(ev.srcElement).parents(".main-nav-item").length == 0
				&& $(ev.srcElement).find(".main-nav-item").length == 0) {
			$('#main-nav').children('.main-nav-item-active').removeClass('main-nav-item-active'); 
		}
	});
		
	
	ctrl = new OpenLayers.SOS.Controller(
		scale,
		map, {
			fail: error, 

			ready: function() {
				$('#send').button('enable');
			},
			
			updateValues: function(values) {
				var mm = [values.min, values.max];
				updateThresholdLabel(mm);
				var b = (values.max - values.min)/10;
				$('#thresholdSlider').slider('option', {
					min: values.min - b,
					max: values.max + b,
					values: mm
				});
			},

			updateTime: function(times) {
				$('#timeSlider').slider('option', {
					min: times.min, 
					max: times.max, 
					step: times.step,
					value: times.selected
				}).slider('enable');
				updateTimeLabel(new Date(times.selected));
			},
			standardMode: function() {
				$('#thresholdSlider').slider('enable');
				$('#viewChooser input').button('enable');
				$('#excee-prob-threshold').attr("disabled", true);
			},
			
			exceedanceMode: function() {
				$('#thresholdSlider').slider('disable');
				$('#viewChooser input').button('enable');
				$('#excee-prob-threshold').attr("disabled", false);
			},

			probabilityMode: function(enable) {
				$('#thresholdSlider').slider('disable');
				$('#viewChooser input').button('disable');
				$('#excee-prob-threshold').attr("disabled", true);
			}
		}
	);
	
	updateThresholdLabel(threshold.init);
	updateIntervalLabel(intervals.init);
	updateTimeLabel(now);
}	
