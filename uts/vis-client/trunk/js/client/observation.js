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
		threshold: Number.NaN,
		time: null,

		initialize: function(id, geometry, srid, procedure, observedProperty, uom, values) {
			function test(obj, name) { 
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
			
			var attr = { 
				id: id, 
				uom: uom,
				procedure: procedure,
				observedProperty: observedProperty,
				isMultiFeature: values.length != 1,
				timeValueArray: timeValueArray,
				resultValue: this.getMapValueForArray(timeValueArray)
			};
			OpenLayers.Feature.Vector.prototype.initialize.apply(this, [geometry, attr]);
		},
	
		getMapValueForArray: function(values) {
			var mapValue = 0.0;
			for (var i = 0; i < values.length; i++) {
				if (typeof(values[i][1]) == "number") {
					mapValue += values[i][1];
				} else if (values[i][1].getClassName 
						&& values[i][1].getClassName().match(".*Distribution$")) {
					mapValue += values[i][1].getMean();
				} else if (values[i][1].length) { // realisations
					var m = 0;
					for (var j = 0; j < values[i][1].length; j++) {
						m += values[i][1][j];
					}
					mapValue += m/values[i][1].length;
				} else if (values[i][1].probability) {
					mapValue += values[i][1].probability;
				} else {
					throw "TODO!!!";
				}
			}
			return mapValue/values.length;
		},
		
		setExceedanceProbabilityThreshold: function(val) {
			//console.log("setExceedanceProbabilityThreshold("+val+");");
			this.threshold = val;
			if (this.time) this.setTime(this.time);
		},
		
		calculateExceedanceProbability: function(t,val) {
			var result;
			if (typeof(val) === "number") {
				result = (val < t) ? 0 : 100;
			} else if (val.getClassName && val.getClassName().match(".*Distribution$")) {
				result = val.getExceedanceProbability(t)*100;
			} else if (val.length) {
				var u = 0;
				for (var i = 0; i < val.length; i++) {
					if (val[i] > t) { u++; }
				}
				return 100*(u/val.length);
			} else {
				throw "Unsupported value type: " + val;
			}
			//console.log("calculateExceedanceProbability("+val+"); == "+result);
			return result;
		},
		
		setTime: function(time) {
			//console.log("setTime("+time+");");
			var v = this.getValues(), matchedValues = [];
			this.time = time;
			for (var i = 0; i < v.length; i++) {
				if ((v[i][0][0] == time) || (v[i][0].length == 2 
					&& v[i][0][0] <= time && v[i][0][1] >= time)) { 
					matchedValues.push(v[i]);
				}
			}
			if (!isNaN(this.threshold)) { 
				this.attributes.exceedance = (matchedValues.length > 0) ? 
					this.calculateExceedanceProbability(this.threshold, matchedValues[0][1]) 
						: Number.NEGATIVE_INFINITY;
			}
			var rv = this.getMapValueForArray(matchedValues);
			this.attributes.resultValue = (isNaN(rv)) ? Number.NEGATIVE_INFINITY : rv;
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
			return this.attributes.resultValue;
		},
		
		isMultiFeature: function() {
			return this.attributes.isMultiFeature;
		}
	});
/* vim: set ts=4 sts=4 sw=4 noet ft=javascript fenc=utf-8 */
