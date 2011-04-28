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
OpenLayers.SOS = OpenLayers.SOS || {};
OpenLayers.SOS.ObservationSeries = OpenLayers.Class(OpenLayers.Feature.Vector, {
		CLASS_NAME: "OpenLayers.SOS.ObservationSeries",
		
		srid: null,

		initialize: function(id, geometry, srid, procedure, observedProperty, uom, values) {
			var test = function(obj, name) { 
				if (!obj) throw name + " is obligatory!";
			}
			test(id, "ID");
			test(srid, "SRID");
			test(geometry,"Geometry");
			test(procedure,"Procedure");
			test(observedProperty, "ObservedProperty");
			test(uom,"UOM");
			test(values, "values");

			this.srid = srid;
			
			var mapValue = 0;
			var timeValueArray = [];
			
			values.sort(function(a, b) {
				var time1, time2;
				a = (a.time.samplingTime) ? a.time.samplingTime : a.time;
				b = (b.time.samplingTime) ? b.time.samplingTime : b.time;
				if (a.timeInstant) {
					if (b.timeInstant) {
						time1 = a.timeInstant.timePosition;
						time2 = b.timeInstant.timePosition;
					} else {
						time1 = a.timeInstant.timePosition;
						time2 = b.timePeriod.beginPosition;
					}
				} else {
					if (b.timeInstant){
						time1 = a.timePeriod.beginPosition;
						time2 = b.timeInstant.timePosition;
					} else {
						time1 = a.timePeriod.beginPosition;
						time2 = b.timePeriod.beginPosition;
					}
				}
				return time1.getTime() - time2.getTime();
			});
			
			for (var i = 0; i < values.length; i++) {
				if (typeof(values[i].value) == "number") {
					mapValue += values[i].value;
				} else if (values[i].value.getClassName 
						&& values[i].value.getClassName().match(".*Distribution$")) {
					mapValue += values[i].value.getMean();
				} else {
					throw "TODO!!!";
				}
				var time;
				if (values[i].time.timeInstant) {
					time = [values[i].time.timeInstant.timePosition.getTime()];
				} else if (values[i].time.timePeriod) {
					time = [values[i].time.timePeriod.beginPosition.getTime(),
						    values[i].time.timePeriod.endPosition.getTime()];
				} else {
					throw "Unknown SamplingTime Format";
				}
				timeValueArray.push([time, values[i].value]);
			}
			mapValue /= values.length;
			var attr = { 
				id: id, 
				uom: uom,
				procedure: procedure,
				observedProperty: observedProperty,
				isMultiFeature: values.length != 1,
				timeValueArray: timeValueArray,
				resultValue: mapValue
			};
			OpenLayers.Feature.Vector.prototype.initialize.apply(this, [geometry, attr]);
		},
		getFoiId: function() {
			return this.attributes.id;
		},
		getUom: function() {
			return this.attributes.uom;
		},
		getObservedProperty: function() {
			return this.attributes.observedProperty;
		},
		getProcedure: function() {
			return this.attributes.procedure;
		},
		transform: function(dest){
			this.geometry.transform(this.srid, dest);
			this.srid = dest;
		},
		getValues: function() {
			return this.attributes.timeValueArray;
		},
		getValue: function(){
			return this.attributes.attributes.resultValue;
		},
		isMultiFeature: function() {
			return this.attributes.isMultiFeature;
		}
	});
/* vim: set ts=4 sts=4 sw=4 noet ft=javascript fenc=utf-8 */
