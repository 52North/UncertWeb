XmlUtils = (function() {
	var ns = {
		wps: "http://www.opengis.net/wps/1.0.0",
		ows: "http://www.opengis.net/ows/1.1",
		xlink: "http://www.w3.org/1999/xlink",
		om: "http://www.opengis.net/om/2.0",
		gml: "http://www.opengis.net/gml/3.2",
		xsi: "http://www.w3.org/2001/XMLSchema-instance",
		sams: "http://www.opengis.net/samplingSpatial/2.0",
		sf: "http://www.opengis.net/sampling/2.0",
		xlink: "http://www.w3.org/1999/xlink",
		un: "http://www.uncertml.org/2.0"
	};
	return {

		uncertainAlbatrossInput: function(id,sector,stddev,parameterName) {
			var xml = jsxml.fromString(
				'<?xml version="1.0" encoding="UTF-8"?>'+
				'<UncertainAlbatrossInput xmlns="http://www.uncertweb.org">'+
					'<albatrossID>'+id+'</albatrossID>'+
					'<parameter name="'+parameterName+'">' + sector + '</parameter>'+
					'<un:StandardDeviation xmlns:un="http://www.uncertml.org/2.0">'+
						'<un:values>' + stddev + '</un:values>'+
					'</un:StandardDeviation>'+
				'</UncertainAlbatrossInput>');
			xml.schema = "http://v-mars.uni-muenster.de/uncertweb/schema/Profiles/Albatross/albatross_uInput.xsd";
			return xml;
		},

		xml2string: function(doc) {
			if (typeof(doc) == "string") return doc;
			var xml = doc.xml || new XMLSerializer().serializeToString(doc);
			return vkbeautify.xml(vkbeautify.xmlmin(xml), 2)
		},

		xml2console: function (doc) {
			console.log(vkbeautify.xml(XmlUtils.xml2string(doc)));    
		},

		createExecute: function(options) {
			var doc = jsxml.fromString(
				'<?xml version="1.0" encoding="UTF-8"?>' +
				'<wps:Execute service="WPS" version="1.0.0" '
					+ 'xmlns:wps="' + ns.wps  + '" '
					+ 'xmlns:ows="' + ns.ows + '" '
					+ 'xmlns:xlink="' + ns.xlink + '" />');

			var identifier = doc.createElement("ows:Identifier");
			identifier.appendChild(doc.createTextNode(options.id));
			var inputs = doc.createElement("wps:DataInputs");

			function createInput(id, value) {
				var input = doc.createElement("wps:Input");
				var inputId = doc.createElement("ows:Identifier");
				inputId.appendChild(doc.createTextNode(id));
				input.appendChild(inputId);
				if (value instanceof Object) {
					if (value.documentElement) {
							//XML fragment
							var data = doc.createElement("wps:Data");
							var complexData = doc.createElement("wps:ComplexData");
							complexData.appendChild(value.documentElement);
							complexData.setAttribute("schema", value.schema);
							data.appendChild(complexData);
							input.appendChild(data);
					} else {
						//REFERENCE
						var reference = doc.createElement("wps:Reference");
						for (var key in value) {
							if (value[key]) {
								reference.setAttribute(key, value[key]);	
							}
							
						}
						input.appendChild(reference);
					}
				} else {
					var data = doc.createElement("wps:Data");
					var literalData = doc.createElement("wps:LiteralData");
					literalData.appendChild(doc.createTextNode(value));
					data.appendChild(literalData);
					input.appendChild(data);
				}
				
				
				inputs.appendChild(input);
			}
			for (var key in options.inputs) {
				if (options.inputs[key] instanceof Array)
					for (var i = options.inputs[key].length - 1; i >= 0; i--)
						createInput(key, options.inputs[key][i]);
				else createInput(key, options.inputs[key]);
			}
			var respForm = doc.createElement("wps:ResponseForm");
			var outputs = doc.createElement("wps:ResponseDocument")
			respForm.appendChild(outputs);
			for (var key in options.outputs) {
				var output = doc.createElement("wps:Output");
				for (var attr in options.outputs[key]) {
					output.setAttribute(attr, options.outputs[key][attr]);
				}
				var outputId = doc.createElement("ows:Identifier");
				outputId.appendChild(doc.createTextNode(key));
				output.appendChild(outputId);
				outputs.appendChild(output);
			}
			respForm.appendChild(outputs);
			doc.documentElement.appendChild(identifier);
			doc.documentElement.appendChild(inputs);
			doc.documentElement.appendChild(respForm);
			return doc;
		},


		createObservationCollection: function(obs) {
			var doc = jsxml.fromString(
				'<?xml version="1.0" encoding="UTF-8"?>' +
				'<om:OM_ObservationCollection '
					+ 'xmlns:om="' + ns.om + '" '
					+ 'xmlns:xsi="' + ns.xsi + '" '
					+ 'xmlns:gml="' + ns.gml + '" '
					+ 'xmlns:sams="' + ns.sams + '" '
					+ 'xmlns:sf="' + ns.sf + '" '
					+ 'xmlns:xlink="' + ns.xlink + '" />');

			for (var i = 0; i < obs.length; ++i) {
				var observation = doc.createElement("om:OM_BooleanObservation");
				observation.setAttribute("gml:id", "o" + i);

				var identifier = doc.createElement("gml:identifier");
				identifier.setAttribute("codeSpace", window.location.href);
				identifier.appendChild(doc.createTextNode(i));
				observation.appendChild(identifier);

				/* phenomenon time */
				var phenomenonTime = doc.createElement("om:phenomenonTime");
				var phenomenonTimeInstant = doc.createElement("gml:TimeInstant");
				phenomenonTimeInstant.setAttribute("gml:id", "pt" + i);
				var phenomenonTimePosition = doc.createElement("gml:timePosition");
				phenomenonTimePosition.appendChild(doc.createTextNode(obs[i].phenomenonTime))
				phenomenonTimeInstant.appendChild(phenomenonTimePosition);
				phenomenonTime.appendChild(phenomenonTimeInstant);
				observation.appendChild(phenomenonTime);

				/* result time */
				var resultTime = doc.createElement("om:resultTime");
				var resultTimeInstant = doc.createElement("gml:TimeInstant");
				resultTimeInstant.setAttribute("gml:id", "rt" + i);
				var resultTimePosition = doc.createElement("gml:timePosition");
				resultTimePosition.appendChild(doc.createTextNode(obs[i].resultTime))
				resultTimeInstant.appendChild(resultTimePosition);
				resultTime.appendChild(resultTimeInstant);
				observation.appendChild(resultTime);

				/* procedure */
				var procedure = doc.createElement("om:procedure");
				procedure.setAttribute("xlink:href", obs[i].procedure);
				observation.appendChild(procedure);

				/* observed property */
				var observedProperty = doc.createElement("om:observedProperty");
				observedProperty.setAttribute("xlink:href", obs[i].observedProperty);
				observation.appendChild(observedProperty);

				/* feature */
				var featureOfInterest = doc.createElement("om:featureOfInterest");
				var ssf = doc.createElement("sams:SF_SpatialSamplingFeature");
				ssf.setAttribute("gml:id", "ssf" + i);
				var ssfIdentifier = doc.createElement("gml:identifier");
				ssfIdentifier.setAttribute("codeSpace", window.location.href);
				ssfIdentifier.appendChild(doc.createTextNode("foi" + i));
				ssf.appendChild(ssfIdentifier);

				var type = doc.createElement("sf:type");
				type.setAttribute("xlink:href", "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint");
				ssf.appendChild(type);

				var sampledFeature = doc.createElement("sf:sampledFeature");
				sampledFeature.setAttribute("xsi:nil", true);
				ssf.appendChild(sampledFeature);

				var shape = doc.createElement("sams:shape");
				var point = doc.createElement("gml:Point");
				point.setAttribute("gml:id", "point" + i);
				var pos = doc.createElement("gml:pos");
				pos.setAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/4326");
				pos.appendChild(doc.createTextNode(obs[i].featureOfInterest[1] + " " + obs[i].featureOfInterest[0]))
				point.appendChild(pos);
				shape.appendChild(point);
				ssf.appendChild(shape);
				featureOfInterest.appendChild(ssf);
				observation.appendChild(featureOfInterest);

				var result = doc.createElement("om:result");
				result.appendChild(doc.createTextNode(true));
				observation.appendChild(result);
				doc.schema = "http://schemas.opengis.net/om/2.0/observation.xsd";
				doc.documentElement.appendChild(observation)	
			}
			return doc;
		},

		isException: function(e) {
			if (e.documentElement.namespaceURI === ns.wps 
			 && e.documentElement.localName === "ExecuteResponse") {
				return false;
			}
			if (e.documentElement.namespaceURI === ns.ows 
			 && e.documentElement.localName === "ExceptionReport") {
				return true;
			}
			if (e.documentElement.namespaceURI === ns.om 
			 && e.documentElement.localName === "OM_ObservationCollection") {
				return false;
			}
			return true;
		},

		getOutput: function(response, name) {
			if (this.isException(response)) { return; }
			var outputs = response.getElementsByTagNameNS(ns.wps,"Output");
			for (var i = 0; i < outputs.length; ++i) {
				var o = outputs.item(i);
				if (o.getElementsByTagNameNS(ns.ows,"Identifier").item().textContent === name) {
					var ref = o.getElementsByTagNameNS(ns.wps, "Reference").item();
					if (ref) {
						return {
							"mimeType": ref.getAttribute("mimeType"),
							"xlink:href": ref.getAttribute("href"),
							"encoding": ref.getAttribute("encoding"),
							"schema": ref.getAttribute("schema")
						};
					} else {
						var data = o.getElementsByTagNameNS(ns.wps, "Data").item();
						if (data) {
							var literalData = data.getElementsByTagNameNS(ns.wps, "LiteralData").item();
							if (literalData) {
								return literalData.textContent;
							}
						}
					}
				}
			}
		},

		getOCsFromRandomSample: function(randomSample) {
			var i, a = [], v = randomSample.getElementsByTagNameNS(ns.un, "values"), l = v.length;
			for (i = 0; i < l; ++i) {
				a.push(v.getAttribute("href"));
			}
			return a.map(function(h) {
				return {
					"xlink:href": h,
					"mimeType": "application/x-om-u+xml",
					"encoding": "UTF-8",
					"schema": "http://schemas.opengis.net/om/2.0/observation.xsd"	
				};
			});
		}
	}
})();
