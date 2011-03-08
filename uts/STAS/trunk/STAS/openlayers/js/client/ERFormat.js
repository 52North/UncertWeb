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
OpenLayers.Format.ExceptionReport = OpenLayers.Class(OpenLayers.Format.XML, {
	CLASS_NAME: "OpenLayers.Format.ExceptionReport",
	namespaces: { ows: "http://www.opengis.net/ows/1.1" },
	schemaLocation: "http://www.opengis.net/ows/1.1" 
				+ " http://schemas.opengis.net/ows/1.1.0/owsAll.xsd",
    defaultPrefix: "ows",
    regExes: { trimSpace: (/^\s*|\s*$/g) },
    initialize: function (options) {
        OpenLayers.Format.XML.prototype.initialize.apply(this, [options]);
    },
    read: function (data, destinationProjection) {
        if (typeof data === "string") {
            data = OpenLayers.Format.XML.prototype.read.apply(this, [data]);
        }
        if (data && data.nodeType === 9) {
            data = data.documentElement;
        }
        var info = {};
        this.readNode(data, info);
		return info;
    },
    readers: {
		"ows": {
			"ExceptionReport": function (node, obj) {
				obj.exceptions = [];
				obj.version = node.getAttribute("version");
				this.readChildNodes(node, obj);	
			},
			"Exception": function (node, report) {
				var ex = { exceptionTexts: [] };
				ex.exceptionCode = node.getAttribute("exceptionCode");
				var locator = node.getAttribute("locator");
				if (locator) {
					ex.locator = locator;	
				}
				report.exceptions.push(ex);
				this.readChildNodes(node, ex);
			},
			"ExceptionText": function (node, exception) {
				exception.exceptionTexts.push(this.getChildValue(node));
			}
        }
	},
    write: function () {/* we don't need to write any xml */},
	writers: {/* we don't need to write any xml */}
});

