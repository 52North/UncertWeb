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
	var parameters = OpenLayers.Util.getParameters();
	if (parameters.url) {
		if (parameters.request) {
			OpenLayers.Request.GET({
				url: parameters.request,
				success: function (r) {
					ctrl.addLayer({
						url: parameters.url,
						request: r.responseText,
						mime: parameters.mime
					});
				},
				failure: error
			});
		} else {
			ctrl.addLayer({
				url: parameters.url,
				mime: parameters.mime
			});
		}
	}
	parameters.oc && ctrl.addLayer({
		url: parameters.oc,
		mime: "application/xml" });
	parameters.json && ctrl.addLayer({
		url: parameters.json,
		mime: "application/jsom" });
	parameters.tiff && ctrl.addLayer({
		url: parameters.tiff,
		mime: "image/geotiff" });
	parameters.netcdf && ctrl.addLayer({
		url: parameters.netcdf,
		mime: "application/netcdf" });
	parameters.rasterOM && ctrl.addLayer({
		url: parameters.rasterOM,
		mime: "application/vnd.ogc.om+xml" });
}

