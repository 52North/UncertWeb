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
OpenLayers.Control.AdvancedLayerSwitcher = OpenLayers.Class(OpenLayers.Control, {
	CLASS_NAME: 'OpenLayers.Control.AdvancedLayerSwitcher',
	divId: null,
	numBaseLayers: 0,
	initialize: function(divId) {
		this.divId = '#' + divId;
		var self = this;

		$(this.divId + ' .changeableLayerName').live('dblclick', function() {
			var id = $(this).attr('for'), l = self.map.getLayer(id);
			$(this).html('<input class="layerNameInput" name="' + id
				+ '"type="text" size="' + l['name'].length 
				+ '" value="'+ l['name'] + '"/>');
		});
		
		$(this.divId + ' .layerNameInput').live('change', function() {
			var nn = $(this).val(), l = self.map.getLayer($(this).attr('name'));
			if (l['name'] != nn) l.setName(nn);
		}).live('focusout', function() { self.draw(); });
		
		$(this.divId + ' input[name=olDataLayers]').live('change', function() {
			var l = self.map.getLayer($(this).attr('id'));
			l.setVisibility(!l.getVisibility());
		});

		$(this.divId + ' input[name=olBaseLayers]').live('change', function() {
			self.map.setBaseLayer(self.map.getLayer($(this).attr('id')));
		});
		
		$(this.divId + ' .deleteButton').live('click', function() {
			self.map.removeLayer(self.map.getLayer($(this).parent().attr('id')));
		});
	},

	setMap: function(map) {
		OpenLayers.Control.prototype.setMap.apply(this, [map]);
		this.numBaseLayers = this.map.getLayersBy("isBaseLayer", true).length;
		this.map.events.on({ 
            'removelayer': this.onRemovingLayer,
            'addlayer': this.onAddingLayer,
            'changebaselayer': this.draw,
            'changelayer': this.draw,
			'scope': this
        });
	},
	
	onRemovingLayer: function(l) {
		if (l.isBaseLayer) --this.numBaseLayers; this.draw();
	},
	
	onAddingLayer: function(l) {
		if (l.isBaseLayer) ++this.numBaseLayers; this.draw();
	},
	
	destroy: function() {
		this.map.events.un({
            'removelayer': this.onRemovingLayer,
            'addlayer': this.onAddingLayer,
            'changebaselayer': this.draw,
            'changelayer': this.draw,
			'scope': this
        });
        OpenLayers.Control.prototype.destroy.apply(this, arguments);
	},
	
	draw: function() {
		var self = this;
		var containsBaseLayers = false;
		var containsOverlays = false;
		var $div = $(this.divId).html('');
		var $base = $('<ul style="list-style: none"/>');
		var $data = $('<ul style="list-style: none"/>');
		for (var i = this.map.layers.length-1; i >= 0; --i) {
			var l = this.map.layers[i], $li;
			if (l.isBaseLayer) { containsBaseLayers = true;
				$li = $('<li>').attr({ 'id': l.id });
				$('<input type="radio" name="olBaseLayers" id="' + l.id + '"/>')
					.prop('checked', this.map.baseLayer.id === l.id)
					.appendTo($li);
				$('<label for="' + l.id + '"/>')
					.text(l['name'])
					.appendTo($li);
				$li.appendTo($base);
			} else { containsOverlays = true;
				$li = $('<li>').attr({ id: l.id });
				$('<input type="checkbox" name="olDataLayers" id="' + l.id + '"/>')
					.attr({ 'checked': l.getVisibility() })
					.appendTo($li);
				$('<label class="changeableLayerName" for="' + l.id + '"/>')
					.text(l['name'])
					.appendTo($li);
				$('<span class="ui-icon ui-icon-triangle-2-n-s"/>')
					.appendTo($li);
				$('<span class="deleteButton ui-icon ui-icon-close"/>')
					.appendTo($li);
				$li.appendTo($data);
			}
		}
	
		if (containsBaseLayers)
			$div.append('<h2>Base Layer</h2>').append($base);
		if (containsOverlays)
			$div.append('<h2>Overlays</h2>').append($data);
		
		$data.sortable({ 'update': function(ev,ui) {
			var ids = $data.sortable('toArray');
			for (var i = 0; i < ids.length; i++) {
				var l = self.map.getLayer(ids[i]);
				var ni = self.map.layers.length-(i+1);
				if (self.map.getLayerIndex(l) != ni) 
					self.map.setLayerIndex(l, ni);
			}
			self.map.resetLayersZIndex();
		}});
	},
});

