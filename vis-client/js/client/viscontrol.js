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
OpenLayers.Control.Visualizer = OpenLayers.Class(OpenLayers.Control.SelectFeature, {
	CLASS_NAME: "OpenLayers.Control.Visualizer",

	feature: null,
	popup: null,
	selectTimeCallback: null,

	initialize: function(ctrl, layer, selectTimeCallback) {
		this.selectTimeCallback = selectTimeCallback;
		this.ctrl = ctrl;
		OpenLayers.Control.SelectFeature.prototype.initialize.apply(this, [
			layer, {
				toggle: true,
				clickout: true,
				multiple: false,
				hover: false,
				onSelect: this.on,
				onUnselect: this.off,
				autoActivate: true
			}
		]);
	},

	setMap: function(map) {
		OpenLayers.Control.SelectFeature.prototype.setMap.apply(this, [map]);
		var self = this;
		map.events.register("changelayer", map, function(ev) {
			if (self.active && ev.layer == self.layer && !self.layer.getVisibility()) self.off();
		});
		map.events.register("removelayer", map, function(ev) {
			if (self.layer == ev.layer) {
				self.removePopup();
				self.map.removeControl(self);
			}
		});
		this.handlers.feature.stopDown = false;
		this.handlers.feature.stopUp = false;
	},

	update: function() {
		if (this.popup) {
			this.popup.drawContents();
		}
	},

	on: function(f) {
		if (this.active) {
			this.addPopup(f);
			this.feature = f;
		}
	},

	off: function() {
		this.removePopup();
		this.feature = null;
	},

	removePopup: function() {
		if (this.popup) {
			this.popup.hide();
			this.map.removePopup(this.popup);
			this.popup = null;
		}
	},
	addPopup: function(f) {
		if (this.popup) this.removePopup();
		this.popup = new OpenLayers.Popup.VisualizingPopup(
			this.map, this.ctrl, f, this.selectTimeCallback);
	},

});
