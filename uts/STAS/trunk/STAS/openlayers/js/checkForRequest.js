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
function checkForRequest() {
	var parameters = {};
	var str = document.location.search.substr(1, document.location.search.length);
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
				ctrl.addLayer({
					url: parameters['url'], 
					request: r.responseText 
				}); 
			},
			failure: error
		});
	}
	if (parameters['oc']) {
		OpenLayers.Request.GET({
			url: parameters['oc'],
			success: function (r) { 
				ctrl.addLayer({ 
					oc: (r.responseXML)? r.responseXML : r.responseText 
				}); 
			},
			failure: error
		});
	}
	if (parameters['json']) {
		OpenLayers.Request.GET({
			url: parameters['json'],
			success: function(r) { 
				ctrl.addLayer({ 
					json: r.responseText 
				}); 
			},
			failure: error
		});
	}
}
