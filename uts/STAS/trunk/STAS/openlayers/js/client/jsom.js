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
/* 
 * Contains portions of OpenLayers Map Viewer Library (http:/openlayers.org):
 * 
 * Copyright 2005-2010 OpenLayers Contributors, released under the Clear BSD
 * license. Please see http://svn.openlayers.org/trunk/openlayers/license.txt
 * for the full text of the license.
 *
 */
 
var print = true;
OpenLayers.SOS = OpenLayers.SOS || {};
OpenLayers.SOS.Format = OpenLayers.SOS.Format || {};
OpenLayers.SOS.Format.JSOM = OpenLayers.Class(OpenLayers.Format.JSON, {
		CLASS_NAME: "OpenLayers.SOS.Format.JSOM",
		initialize: function (options) {
			OpenLayers.Format.JSON.prototype.initialize.apply(this, [options]);
		},
		read: function (o) {
			var j = (typeof(o) === "string") ? OpenLayers.Format.JSON.prototype.read(o) : o;
			var result;
			if (j.OM_MeasurementCollection) {
				result = this.parsers.collection(j.OM_MeasurementCollection, "OM_Measurement")
			} else if (j.OM_DiscreteNumericObservationCollection) {
				result = this.parsers.collection(j.OM_DiscreteNumericObservationCollection, "OM_Measurement");
			} else if (j.OM_UncertaintyObservationCollection) {
				result = this.parsers.collection(j.OM_UncertaintyObservationCollection, "OM_UncertaintyObservation");
			} else if (j.OM_TextObservationCollection) { 
				throw "OM_TextObservations are not supported."; 
			} else if (j.OM_ReferenceObservationCollection) {
				throw "OM_ReferenceObservations are not supported."; 
			} else {
				throw "JSON does not contain a parsable collection.";
			}
			return result;
		},

		parsers: {
			collection: function(json, obsType) {
				var collection = [];
				var foiMapping = {};
				
				for (var i = 0; i < json.length; i++) {
					var o = this.observation(json[i][obsType], obsType);
					if (!foiMapping[o.id]) {
						foiMapping[o.id] = o;
					} else {
						for (var j = 0; j < o.values.length; j++) {
							foiMapping[o.id].values.push(o.values[j]);
						}
					}
				}
				
				for (var key in foiMapping) {
					var o = foiMapping[key];
					collection.push(new OpenLayers.SOS.ObservationSeries(o.id, 
						o.geom, o.srid, o.proc, o.obsProp, o.uom, 
						o.values));
				}
				return collection;
			},
			observation: function(json, obsType) {
				if (!json.phenomenonTime)
					throw "Invalid JSOM: no PhenomenonTime";
				if (!json.featureOfInterest)
					throw "Invalid JSOM: no FeatureOfInterest.";
				if (typeof(json.procedure) != "string")
					throw "Invalid JSOM: no Procedure.";
				if (typeof(json.observedProperty) !== "string")
					throw "Invalid JSOM: no ObservedProperty";

				var result = this.result(json, obsType);
				var time = this.time(json.phenomenonTime);
				var foi = this.featureOfInterest(json.featureOfInterest);
				return {
					id: foi.id, 
					geom: foi.geom, 
					srid: foi.srid, 
					proc: json.procedure,
					obsProp: json.observedProperty, 
					uom: result.uom, 
					values: [{ time: time, value: result.value }]};
			},
			featureOfInterest: function(json) {
				if (!json.SF_SpatialSamplingFeature)
					throw "Invalid JSOM.";
				json = json.SF_SpatialSamplingFeature;
				if (!json.shape)
					throw "Invalid JSOM: no shape.";
				if (!json.identifier)
					throw "Invalid JSOM.";
				if (!json.identifier.value)
					throw "Invalid JSOM.";
				var id = json.identifier.value;
				var geom = new OpenLayers.Format.GeoJSON().parseGeometry(json.shape);
				if (!json.shape.crs)
					throw "No CRS specified!";
				if (json.shape.crs.type != "name")
					throw "Currently only CRS of type 'name' are supported!";
				var crs = json.shape.crs.properties.name;
				
				if (crs.match("^http")) {
					crs = crs.split("/");
				} else if (crs.match("^urn")) {
					crs = crs.split(":");
				} else {
					throw "Unsupported CRS notation: " + crs;
				}
				var srid = new OpenLayers.Projection("EPSG:" + parseInt(crs[crs.length - 1]));
				return { id: id, geom: geom, srid: srid };
			},
			time: function(json) {
				parseInstant = function(json) {
					if (!json.timePosition) throw "Invalid JSOM.";
					json.timePosition = OpenLayers.Date.parse(json.timePosition);
				};
				parsePeriod = function(json) {
					if (!json.begin) throw "Invalid JSOM.";
					if (!json.end) throw "Invalid JSOM.";
					parseInstant(json.begin.TimeInstant);
					json.beginPosition = json.begin.TimeInstant;
					parseInstant(json.end.TimeInstant);
					json.endPosition = json.end.TimeInstant;
				};
				if (json.TimePeriod) {
					parsePeriod(json.TimePeriod);
					json.timePeriod = json.TimePeriod;
				} else if (json.TimeInstant) {
					parseInstant(json.TimeInstant);
					json.timeInstant = json.TimeInstant;
				} else {
					throw "Unsupported Time Format";
				}
				return json;
			},
			result: function(json, type) {
				function parseUncertainty(uom, j) {
					var value = null;
					try {
						if (j.Probability) {
							var lt = null, le = null, gt = null, ge = null;
							for (var i = 0; i < j.Probability.constraints.length; i++) {
								switch(j.Probability.constraints[i].type) {
									case 'GREATER_THAN':	  gt = j.Probability.constraints[i].value; break;
									case 'GREATER_OR_EQUAL' : ge = j.Probability.constraints[i].value; break;
									case 'LESS_THAN':		  lt = j.Probability.constraints[i].value; break;
									case 'LESS_OR_EQUAL':     le = j.Probability.constraints[i].value; break;
								}
							}
							var constraint = '';
							if (gt != null) constraint += gt + ' ' + uom + ' &lt; ';
							if (ge != null) constraint += ge + ' ' + uom +' &le; ';
							constraint += 'x';
							if (le != null) constraint += ' &le; ' + le + ' ' + uom;
							if (lt != null) constraint += ' &lt; ' + lt + ' ' + uom;
							value = {
								constraint: constraint, 
								probability: j.Probability.values[0] * 100
							};
						} else if (j.Realisation) {
							value = j.Realisation.values;
						} else {
							if (j.GaussianDistribution) {
								j.NormalDistribution = j.GaussianDistribution;
								j.NormalDistribution.standardDeviation 
									= j.NormalDistribution.variance;
								for (var i = 0; i < j.NormalDistribution.standardDeviation.length; i++) {
									j.NormalDistribution.standardDeviation[i] 
										= Math.sqrt(parseFloat(j.NormalDistribution.standardDeviation[i]));
								}
							}
							if (j.LogNormalDistribution) {
								j.LogNormalDistribution.location 
									= j.LogNormalDistribution.mean;
								j.LogNormalDistribution.scale
									= j.LogNormalDistribution.variance
								for (var i = 0; i < j.LogNormalDistribution.scale.length; i++) {
									j.LogNormalDistribution.scale[i] 
										= Math.sqrt(parseFloat(j.LogNormalDistribution.scale[i]));
								}
							}
							/*
	 						 * TODO quantiles
							 */
							value = DistributionFactory.build(j);
						}
					} catch (e) {
						console.log(j);
						throw "Unsupported uncertainty type" + j;
					}
					if (value == null) throw "Unsupported uncertainty type" + j;
					return value;						
				}
			
				var uom, value;
				switch (type) {
				case "OM_UncertaintyObservation":
					if (json.result.uom)
						uom = json.result.uom;
					value = parseUncertainty(uom, json.result.value)
					break;
				case "OM_Measurement":
					if (json.result.uom)
						uom = json.result.uom;
					if (json.result.value) 
						value = json.result.value;
					if (json.resultQuality) {
						if (json.resultQuality[0]) {
							if (json.resultQuality[0].uom) 
								uom = json.resultQuality[0].uom;
							if (!json.resultQuality[0].values) 
								throw "Invalid JSOM: no values in resultQuality";
							if (json.resultQuality[0].values.length != 1) 
								throw "Currently only one resultQuality value is supported.";
							value = parseUncertainty(uom, json.resultQuality[0].values[0]);
						}
					}
					break;
				}
				if (!value)
					throw "No value in type " + type;
				return {value: value, uom: uom};
			},
		}
});
/* vim: set ts=4 sts=4 sw=4 noet ft=javascript fenc=utf-8 */
