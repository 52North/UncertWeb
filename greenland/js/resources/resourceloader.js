/*
 * Copyright 2012 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Offers methods to retrieve layers from resource descriptions.
 */
VIS.ResourceLoader = {

    permalinkVersion : 3,

    /**
     * Transforms object with attributes at least url, mime (and vissUrl) stepwise
     * into proper OpenLayers.Layer subclass. Returned objects also form valid
     * config options for Ext.tree.TreeNode.
     */
    loadResourceOptions : function(resourceOptions, callback, scope) {
    	VIS.ResourceLoader.loadResourcePath(resourceOptions, [], callback, scope);
    },

    /**
     * Transforms object with attributes at least url, mime (and vissUrl) into
     * proper layer object following specified "path" of loaderId attributes.
     */
    loadResourcePath : function(resourceOptions, path, callback, scope) {
    	scope = scope || this;
    	var resourceLoader = resourceOptions.resourceLoader || 'root';
    	try {
    		VIS.ResourceLoader.resourceLoader[resourceLoader](resourceOptions, function(result) {
    			var i, r = result.length ? result : [ result ];
    			// Set loader path
    			for (i = 0; i < r.length; i++) {
    				if (resourceOptions.loaderIdPath && resourceOptions.loaderId) {
    					r[i].loaderIdPath = resourceOptions.loaderIdPath.concat(resourceOptions.loaderId);
    				} else {
    					r[i].loaderIdPath = [];
    				}
    			}

    			if (path.length === 0 || result instanceof Error) {
    				// Final result or error
    				callback.call(scope, result);
    			} else {
    				// Follow path
    				if (r.length == 1 && !r[0].loaderId) {
    					// Skip levels without loaderId if they have only a single
    					// element
    					VIS.ResourceLoader.loadResourcePath(r[0], path, callback, scope);
    				} else {
    					// Search for element with specific loaderId corresponding to
    					// path
    					for (i = 0; i < r.length; i++) {
    						if (r[i].loaderId == path[0]) {
    							VIS.ResourceLoader.loadResourcePath(r[i], path.slice(1), callback, scope);
    							return;
    						}
    					}

    					callback.call(scope, new Error('Could not find specified resource'));
    				}
    			}
    		});

    	} catch (e) {
    		callback(new Error(e));
    	}
    },

    /**
     * Returns the resourceOptions allowing to create this layer and the
     * corresponding "path" of loaderId attributes
     */
    getPermalinkObject : function(layer) {
    	var o = {
    		r : layer.resourceOptions,
    		p : layer.loaderIdPath
    	};

    	if (layer instanceof OpenLayers.Layer.VIS.WMSQ) {
    		// WMS-Q layer needs to store layer configuration in order to get added
    		// without user input
    		var layerConfig = {}, visualization;
    		var reqLayers = layer.visualization.requiredLayers[layer.visualization.requiredLayersType].layers;
    		for ( var layerKey in reqLayers) {
    			visualization = layer.visualization[layerKey];
    			layerConfig[layerKey] = {
    				name : visualization.name,
    				min : visualization.min,
    				max : visualization.max,
    				uom : visualization.uom
    			};

    			if (visualization.transformFuncString) {
    				// Store transform function if it was set by user
    				layerConfig[layerKey].transformFuncString = visualization.transformFuncString;
    			}
    		}
    		o.r.layers = layerConfig;
    		o.r.requiredLayersType = layer.visualization.requiredLayersType;
    	}

    	var settingsParcel = new VIS.SettingsParcel();
    	settingsParcel.writeInt(VIS.getMapIndex(layer)); // map index

    	if (layer.store) {
    		// Store layer information if it has store function
    		layer.store(settingsParcel);
    	}

    	o.s = settingsParcel.toString();
    	return o;
    },

    /**
     * Converts permalink object of layers into special JSON notation and appends
     * it as "perma" parameter to document.location.href
     */
    getPermalink : function(layers, storeViewport) {
    	if (!OpenLayers.Util.isArray(layers)) {
    		layers = [ layers ];
    	}

    	var permaObjects = [];
    	for ( var i = 0, layer; i < layers.length; i++) {
    		layer = layers[i];
    		if (layer instanceof OpenLayers.Layer.VIS.Vector || layer instanceof OpenLayers.Layer.VIS.Raster
    				|| layer instanceof OpenLayers.Layer.VIS.WMSQ || layer instanceof OpenLayers.Layer.VIS.WMS) {
    			permaObjects.push(VIS.ResourceLoader.getPermalinkObject(layer));
    		}
    	}

    	// pack as JSON
    	var json = null;
    	if (storeViewport) {
    		var settingsParcel = new VIS.SettingsParcel();
    		VIS.storeViewport(settingsParcel);
    		json = new OpenLayers.Format.JSON().write({
    			layers : permaObjects,
    			viewport : settingsParcel.toString(),
    			version : this.permalinkVersion
    		});
    	} else {
    		json = new OpenLayers.Format.JSON().write({
    			layers : permaObjects,
    			version : this.permalinkVersion
    		});
    	}

    	// make url shorter by replacing {, } and " with unencoded chars
    	json = json.replace(/{/g, '!').replace(/}/g, '*').replace(/"/g, "'");
    	var param = OpenLayers.Util.getParameterString({
    		perma : json
    	});
    	var url = document.location.href;
    	if (url.indexOf('?') != 0) {
    		url = url.split('?')[0];
    	}

    	return OpenLayers.Util.urlAppend(url, param);
    },

    /**
     * Loads resource as specified by permalink url parameter. Result is passed to
     * callback function
     */
    loadResourcesFromPermalink : function(param, callback) {
    	param = param.replace(/!/g, '{').replace(/\*/g, '}').replace(/'/g, '"');
    	var count = 0;

    	try {
    		var perma = new OpenLayers.Format.JSON().read(param);
    	} catch (e) {
    		callback.call(this, new Error('The permalink is incomplete'), 0, 0, 0);
    		return;
    	}
    	if (!perma.version || perma.version > this.permalinkVersion) {
    		callback.call(this, new Error('The permalink is not supported by this version of Greenland'), 0, 0, 0);
    		return;
    	}

    	var callbackIntercept = function(result) {
    		count++;
    		var settingsParcel = new VIS.SettingsParcel(this.s ? this.s : '', perma.version);
    		var mapIndex = settingsParcel.readInt();

    		if (result instanceof OpenLayers.Layer && result.restore) {
    			result.restore(settingsParcel);
    		}
    		callback.call(this, result, mapIndex, count, layers.length);
    	};

    	var layers = perma.layers ? perma.layers : perma;

    	var restoreLayers = function() {
    		for ( var i = 0; i < layers.length; i++) {
    			o = layers[i];
    			VIS.ResourceLoader.loadResourcePath(o.r, o.p, OpenLayers.Function.bind(callbackIntercept, o));
    		}

    	};

    	if (perma.viewport) {
    		VIS.restoreViewport(new VIS.SettingsParcel(perma.viewport), restoreLayers);
    	} else {
    		restoreLayers();
    	}

    	if (layers.length == 0) {
    		return false;
    	}
    },

    parseIntervals : function(intervals) {

    	function parseIsoInterval(isostr) {
    		var instances = [];
    		var periodRegExp = /^P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)D)?T?(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$/;
    		var funcMapping = [ 'Year', 'Month', 'Date', 'Hours', 'Minutes', 'Seconds' ];

    		intervalParts = isostr.split('/');
    		if (intervalParts.length >= 2) {
    			// Start & End point
    			var start = OpenLayers.Date.parse(intervalParts[0]);
    			var end = OpenLayers.Date.parse(intervalParts[1]);

    			if (intervalParts.length == 2) {
    				// Only two instances, no period
    				instances.push(start);
    				instances.push(end);
    			} else {

    				// Parse duration notation
    				var match = intervalParts[2].match(periodRegExp);
    				var current = new Date(start.getTime());
    				instances.push(new Date(current.getTime()));

    				// Add duration until end time reached, record each instance
    				while (current < end) {
    					for ( var i = 1; i <= funcMapping.length; i++) {
    						if (match[i]) {
    							current['set' + funcMapping[i - 1]](current['get' + funcMapping[i - 1]]() + parseInt(match[i]));
    						}
    					}
    					instances.push(new Date(current.getTime()));
    				}
    			}
    		}
    		return instances;
    	}

    	var instances = [], str;
    	for ( var i = 0; i < intervals.length; i++) {
    		str = OpenLayers.String.trim(intervals[i]);
    		if (str.indexOf('/') != -1) {
    			instances = instances.concat(parseIsoInterval(str));
    		} else {
    			instances.push(OpenLayers.Date.parse(str));
    		}
    	}

    	return instances;
    },

    // Functions responsible for loading resourceOptions
    resourceLoader : {

    	/**
    	 * Creates the root level for a resource
    	 */
    	root : function(resourceOptions, callback) {
    		var rootOptions;

    		switch (resourceOptions.mime) {
    		case 'application/x-om-u+json':
    		case 'application/xml':
    		case 'application/x-om-u+xml':
    			// Vector data
    			rootOptions = {
    				resourceLoader : 'vector_root',
    				text : resourceOptions.url
    			};
    			break;
    		case 'application/vnd.ogc.om+xml':
    		case 'application/netcdf':
    		case 'image/geotiff':
    		case 'application/vnd.org.uncertweb.viss.uncertainty-collection+json':
    			// VISS raster data
    			var resource = new OpenLayers.VIS.Resource(VIS.vissUrl, resourceOptions);
    			rootOptions = {
    				resource : resource,
    				resourceLoader : 'viss_root',
    				text : 'VISS ' + resourceOptions.url + (resourceOptions.request ? ' Custom Request' : ''),
    				iconCls : 'icon-servermap' // WMS
    			};
    			break;
    		case 'ncwms':
    			// WMS data
    			rootOptions = {
    				resourceLoader : 'ncwms_root',
    				text : 'ncWMS ' + resourceOptions.url,
    				iconCls : 'icon-serverraster' // WCS, because its used like a WCS
    			};
    			break;

    		case 'wms':
    			// WMS data
    			rootOptions = {
    				resourceLoader : 'wms_root',
    				text : 'WMS ' + resourceOptions.url,
    				iconCls : 'icon-servermap' // WMS
    			};
    			break;
    		case 'threddscatalog':
    			// THREDDS data server catalog
    			rootOptions = {
    				resourceLoader : 'thredds_catalog',
    				text : 'THREDDS Catalog ' + resourceOptions.url,
    				iconCls : 'icon-database'
    			};
    			break;
    		default:
    			callback(new Error('mime not supported'));
    			return;
    		}

    		callback(OpenLayers.Util.extend(rootOptions, resourceOptions));

    	},

    	/**
    	 * Appends TDS urls according to
    	 * http://www.unidata.ucar.edu/projects/THREDDS/tech/catalog/v1.0.2/InvCatalogSpec.html#constructingURLs
    	 *
    	 * @param url
    	 * @param path
    	 * @returns
    	 */
    	appendThreddsUrls : function(url, path) {
    		if (path[0] == "/") {
    			return url.split("/").slice(0, 3).join("/") + path;
    		} else if (path.indexOf('://') != -1) {
    			return path;
    		} else {
    			return url.split("/").slice(0, -1).join("/") + "/" + path;
    		}
    	},

    	/**
    	 * Creates level for each entry in catalog
    	 */
    	thredds_catalog : function(resourceOptions, callback) {
    		var resourceUrl = resourceOptions.url;
    		var queryUrl = (VIS.threddsProxy && resourceOptions.noProxy !== true) ? (VIS.threddsProxy + '?URL=' + resourceOptions.url)
    				: resourceOptions.url;

    		// Perform GET request on resourceUrl to parse collection data, inspect
    		// observed properties, etc.
    		OpenLayers.Request
    				.GET({
    					url : queryUrl,
    					success : function(resp) {
    						if (resp.status == 0) {
    							callback(new Error(
    									'Server seems to not support cross-origin resource sharing (CORS) or is not available'));
    							return;
    						}
    						var format = new OpenLayers.Format.XML(), doc = null;
    						if (!resp.responseXML || !resp.responseXML.documentElement) {
    							doc = format.read(resp.responseText);
    						} else {
    							doc = resp.responseXML;
    						}

    						doc = doc.documentElement;

    						// var name = format.getAttributeNS(doc, null, 'name');
    						var wmsServiceNames = {};

    						// Parses service section to find service types offering WMS and
    						// do get the base url for these services
    						function inspectServiceNodes(node) {
    							var serviceNodes = node.getElementsByTagName('service');
    							var wmsBase = null;
    							for ( var i = 0; i < serviceNodes.length; i++) {
    								var serviceNode = serviceNodes.item(i);
    								var serviceType = serviceNode.getAttribute('serviceType');
    								var serviceName = serviceNode.getAttribute('name');
    								var serviceBase = serviceNode.getAttribute('base');

    								if (serviceType == 'Compound') {
    									var wmsBaseCompound = inspectServiceNodes(serviceNode);
    									if (wmsBaseCompound != null) {
    										wmsServiceNames[serviceName] = wmsBaseCompound;
    									}
    								} else if (serviceType == 'WMS') {
    									wmsServiceNames[serviceName] = serviceBase;
    									wmsBase = serviceBase;
    								}
    							}

    							return wmsBase;
    						}

    						inspectServiceNodes(doc);

    						var catalogOptions = [];
    						var nodes = doc.childNodes;
    						for ( var i = 0; i < nodes.length; i++) {
    							var node = nodes.item(i);
    							if (node.nodeName == 'catalogRef') {
    								var catalogTitle = node.getAttribute('xlink:title');
    								var catalogHref = node.getAttribute('xlink:href');
    								catalogOptions.push(OpenLayers.Util.applyDefaults({
    									text : catalogTitle,
    									// loaderId : catalogHref,
    									resourceLoader : 'thredds_catalog',
    									url : this.appendThreddsUrls(resourceUrl, catalogHref),
    									iconCls : 'icon-database'
    								}, resourceOptions));
    							} else if (node.nodeName == 'dataset') {
    								var datasetName = node.getAttribute('name');
    								var datasetUrlPath = node.getAttribute('urlPath');
    								if (!datasetUrlPath) {
    									// Collection
    									catalogOptions.push(OpenLayers.Util.applyDefaults({
    										text : datasetName,
    										// loaderId : catalogHref,
    										resourceLoader : 'thredds_dataset_collection',
    										datasetNode : node,
    										wmsServiceNames : wmsServiceNames,
    										iconCls : 'icon-database'
    									}, resourceOptions));
    								}
    							}
    						}

    						callback(catalogOptions);
    					}.createDelegate(this),
    					failure : function(resp) {
    						// Error handling
    						callback(new Error(resp.responseText));
    					}
    				});
    	},

    	/**
    	 * Creates level for a dataset collection within a tds catalog
    	 */
    	thredds_dataset_collection : function(resourceOptions, callback) {
    		var datasetNode = resourceOptions.datasetNode;
    		var wmsServiceNames = resourceOptions.wmsServiceNames;
    		var resourceUrl = resourceOptions.url;
    		var format = new OpenLayers.Format.XML();

    		var catalogOptions = [];

    		var nodes = datasetNode.childNodes;
    		for ( var i = 0; i < nodes.length; i++) {
    			var node = nodes.item(i);
    			if (node.nodeName == 'catalogRef') {
    				var catalogTitle = node.getAttribute('xlink:title');
    				var catalogHref = node.getAttribute('xlink:href');
    				catalogOptions.push(OpenLayers.Util.applyDefaults({
    					text : catalogTitle,
    					// loaderId : catalogHref,
    					resourceLoader : 'thredds_catalog',
    					url : this.appendThreddsUrls(resourceUrl, catalogHref),
    					iconCls : 'icon-database'
    				}, resourceOptions));
    			} else if (node.nodeName == 'dataset') {
    				var datasetName = node.getAttribute('name');
    				var datasetUrlPath = node.getAttribute('urlPath');
    				if (!datasetUrlPath) {
    					// Collection
    					catalogOptions.push(OpenLayers.Util.applyDefaults({
    						text : datasetName,
    						// loaderId : catalogHref,
    						resourceLoader : 'thredds_dataset_collection',
    						datasetNode : node,
    						iconCls : 'icon-database'
    					}, resourceOptions));
    				} else {

    					var serviceNameNodes = node.getElementsByTagName('serviceName');
    					var wmsBase = null;
    					for ( var j = 0; j < serviceNameNodes.length; j++) {
    						wmsBase = wmsServiceNames[format.getChildValue(serviceNameNodes.item(j))];
    						if (wmsBase != null) {
    							break;
    						}
    					}

    					if (wmsBase == null) {
    						var temp = datasetNode.getElementsByTagName('metadata');
    						if (temp.length != 0) {
    							temp = temp[0].getElementsByTagName('serviceName');
    						}
    						if (temp.length != 0) {
    							wmsBase = wmsServiceNames[format.getChildValue(temp[0])];
    						}
    					}

    					if (wmsBase != null) {
    						// is a WMS Dataset -> attach new clean ncwms mime node
    						catalogOptions.push({
    							mime : 'ncwms',
    							text : datasetName,
    							// loaderId : catalogHref,
    							resourceLoader : 'ncwms_root',
    							url : this.appendThreddsUrls(this.appendThreddsUrls(resourceUrl, wmsBase), datasetUrlPath),
    							iconCls : 'icon-serverraster'
    						});
    					}
    				}
    			}
    		}
    		if (catalogOptions.length == 0) {
    			callback(new Error('No WMS resource'));
    		} else {
    			callback(catalogOptions);
    		}

    	},

    	/**
    	 * Creates level for each observed property of a vector resource
    	 */
    	vector_root : function(resourceOptions, callback) {
    		var formatClass, resourceUrl = resourceOptions.url;

    		// Get format class to handle resource data based on its
    		// mime type
    		switch (resourceOptions.mime) {
    		case 'application/x-om-u+json':
    			formatClass = OpenLayers.SOS.Format.JSOM;
    			break;
    		case 'application/xml':
    			formatClass = OpenLayers.SOS.Format.ObservationCollection;
    			break;
    		case 'application/x-om-u+xml':
    			formatClass = OpenLayers.SOS.Format.JSOM;
    			// Change url to use OM2->JSOM conversion service
    			resourceUrl = OpenLayers.Util.urlAppend(VIS.omConversionServiceUrl, 'url=' + resourceUrl
    					+ '&from=application/xml&to=application/json');
    			break;
    		default:
    			throw 'Invalid mime';
    		}

    		// Perform GET request on resourceUrl to parse collection data, inspect
    		// observed properties, etc.
    		var request = OpenLayers.Request
    				.GET({
    					url : resourceUrl,
    					success : function(resp) {
    						if (resp.status == 0) {
    							callback(new Error(
    									'Server seems to not support cross-origin resource sharing (CORS) or is not available'));
    							return;
    						}

    						// create instance of previously
    						// determined format class and read
    						// collection info using the resource
    						// url response
    						var collectionInfo;
    						try {
    							collectionInfo = new formatClass().readCollectionInfo(resp.responseText);
    						} catch (e) {
    							callback(new Error(e));
    							return;
    						}
    						var vectorRootOptions = [];
    						// collectionInfo contains information
    						// about observed properties and
    						// procedures of given vector resource
    						for ( var i = 0; i < collectionInfo.obsProps.length; i++) {

    							// Add node for each observed
    							// property
    							var obsProp = collectionInfo.obsProps[i];
    							vectorRootOptions.push(OpenLayers.Util.applyDefaults({
    								text : obsProp || 'Unkown',
    								loaderId : obsProp,
    								layerOptions : {
    									// include information required to create
    									// VIS.Vector layer
    									protocol : new OpenLayers.Protocol.HTTP({
    										url : resourceUrl,
    										format : new formatClass({
    											obsProp : obsProp
    										}), // instance of format class to handle
    										// specific mime,
    										// filters by observed property

    										read : function(options) {
    											// very simple override of read function
    											// to reuse request
    											var resp = new OpenLayers.Protocol.Response({
    												requestType : 'read'
    											});
    											resp.priv = request; // set
    											// already completed request object
    											this.handleRead(resp, options);
    										}
    									}),
    									// Set previously acquired procedure info to
    									// show procedure parameter
    									procedures : collectionInfo.procedures.slice(0)
    								},
    								collectionInfo : collectionInfo, // include
    								// collection information
    								resourceLoader : 'vector_collection'
    							}, resourceOptions));
    						}

    						callback(vectorRootOptions);
    					}.createDelegate(this),
    					failure : function(resp) {
    						// Error handling
    						callback(new Error(resp.responseText));
    					}
    				});
    	},

    	/**
    	 * creates level for all possible visualizations for a observed property of
    	 * a vector collection resource
    	 */
    	vector_collection : function(resourceOptions, callback) {

    		// Adds nodes for all possible visualizations. Each node
    		// needs to have a
    		// getLayerOptions method to provide a visualization
    		// and resultValue
    		// instance for the new layer to create. This method
    		// ensures that multiple
    		// layers may get created from this single node, since
    		// it creates new
    		// instances on every call.

    		var observations = resourceOptions.collectionInfo.observations;
    		var visualizationMap = {};

    		// Cycle through all observations and register all
    		// possible visualizations
    		// depending on an observation.
    		var timeValueArray, value;
    		for ( var i = 0; i < observations.length; i++) {
    			timeValueArray = observations[i].attributes.timeValueArray;
    			for ( var j = 0; j < timeValueArray.length; j++) {
    				visualization = null;
    				value = timeValueArray[j][1];
    				if (value.length && value.length > 0) {
    					// Realizations
    					if (typeof value[0] === 'string') {
    						// Categorical realizations
    						visualizationMap['Mode'] = {
    							title : 'Mode',
    							visualizationClass : OpenLayers.VIS.Symbology.CategoricalVector,
    							resultValueClass : OpenLayers.VIS.ResultValue.Mode
    						};
    						visualizationMap['ModeProb'] = {
    							title : 'Mode Probability',
    							visualizationClass : OpenLayers.VIS.Symbology.NumericVector,
    							visualizationOptions : {
    								uom : '%'
    							},
    							resultValueClass : OpenLayers.VIS.ResultValue.ModeProbability
    						};
    					} else if (typeof value[0] === 'number') {
    						// Numeric realizations
    						visualizationMap['Mean'] = {
    							title : 'Mean',
    							visualizationClass : OpenLayers.VIS.Symbology.NumericVector,
    							resultValueClass : OpenLayers.VIS.ResultValue.Mean
    						};
    					}
    				} else if (typeof value === 'string') {
    					// TextObservation
    				} else if (value.getClassName && value.getClassName().match('.*Distribution$')) {
    					// Distribution
    					visualizationMap['Mean'] = {
    						title : 'Mean',
    						visualizationClass : OpenLayers.VIS.Symbology.NumericVector,
    						resultValueClass : OpenLayers.VIS.ResultValue.Mean
    					};

    					visualizationMap['ExcProb'] = {
    						title : 'Exceedance Probability',
    						excludeFromCustomVisualization : true,
    						visualizationClass : OpenLayers.VIS.Symbology.NumericVector,
    						visualizationOptions : function() {
    							// Function to return new style
    							// instance
    							return {
    								uom : '%',
    								styler : {
    									bounds : new OpenLayers.VIS.Styler.ExceedanceIntervals()
    								}
    							};
    						},
    						resultValueClass : OpenLayers.VIS.ResultValue.ExceedanceProbability
    					};

    				} else if (value instanceof VIS.StatisticsValue) {
    					// Statistics value
    					switch (value.statisticsType) {
    					case 'Mean':
    						// Mean handled by the distribution Mean
    						// visualization
    						visualizationMap['Mean'] = {
    							title : 'Mean',
    							visualizationClass : OpenLayers.VIS.Symbology.NumericVector,
    							resultValueClass : OpenLayers.VIS.ResultValue.Mean
    						};
    						break;
    					default:
    						// Generic statistics type resultValue
    						visualizationMap[value.getIdentifier()] = {
    							title : value.getTitle(),
    							visualizationClass : value.isNumeric() ? OpenLayers.VIS.Symbology.NumericVector
    									: OpenLayers.VIS.Symbology.CategoricalVector,
    							visualizationOptions : {
    								uom : value.uom
    							},
    							resultValueClass : OpenLayers.VIS.ResultValue.Statistics,
    							resultValueOptions : {
    								statisticsType : value.statisticsType
    							}
    						};
    						break;
    					}

    				}
    			}
    		}

    		// Create node options for all possible visualizations.
    		// Most important is
    		// the creation of the getLayerOptions function which
    		// creates and
    		// initializes the required visualization and
    		// resultValue objects. This
    		// method is bound to each registered visualization
    		// individually.

    		// Common options for layer nodes
    		var commonLayerOptions = OpenLayers.Util.applyDefaults({
    			type : 'vector_layer',
    			resourceLoader : 'vector_layer',
    			leaf : true,
    			iconCls : 'icon-vector'
    		}, resourceOptions);

    		var vectorLayerOptions = [];
    		var customVisualizationMap = {};
    		for ( var key in visualizationMap) {
    			vectorLayerOptions.push({
    				text : visualizationMap[key].title,
    				getLayerOptions : function() {
    					// will be called in the scope of a
    					// single visualizationMap entry

    					// get visualization and resultValue
    					// options, if supplied as function
    					// use its result
    					var visualizationOptions = this.visualizationOptions || {};
    					if (visualizationOptions.call) {
    						// Use function
    						visualizationOptions = visualizationOptions.call(this);
    					} else {
    						// "Clone"
    						visualizationOptions = OpenLayers.Util.extend({}, visualizationOptions);
    					}
    					var resultValueOptions = this.resultValueOptions || {};
    					if (resultValueOptions.call) {
    						// use function
    						resultValueOptions = resultValueOptions.call(this);
    					} else {
    						// "Clone"
    						resultValueOptions = OpenLayers.Util.extend({}, resultValueOptions);
    					}

    					return OpenLayers.Util.extend({
    						visualization : new this.visualizationClass(visualizationOptions || {}),
    						resultValue : new this.resultValueClass(resultValueOptions || {}),
    						strategies : [ new OpenLayers.Layer.VIS.Strategy.FeatureProjection() ]
    					}, resourceOptions.layerOptions);
    				}.createDelegate(visualizationMap[key])
    			});

    			if (visualizationMap[key].visualizationClass == OpenLayers.VIS.Symbology.NumericVector
    					&& visualizationMap[key].excludeFromCustomVisualization !== true) {
    				// Reuse this visualization for custom layer if
    				// it is both, numeric and not excluded
    				customVisualizationMap[key] = visualizationMap[key];
    			}
    		}

    		// Create node options for special custom visualization
    		for ( var key in customVisualizationMap) {
    			// foreach to check whether map has entries

    			// has candidates for a custom layer -> add it
    			vectorLayerOptions.push({
    				text : 'Custom',
    				getLayerOptions : function() {
    					// The special custom resultValue
    					// object receives all
    					// possible resultValue instances
    					var resultValueMap = {};
    					for ( var key in customVisualizationMap) {
    						var cv = customVisualizationMap[key];

    						var resultValueOptions = OpenLayers.Util.extend({}, cv.resultValueOptions);
    						if (resultValueOptions.call)
    							resultValueOptions = resultValueOptions.call(this);
    						resultValueMap[key] = new cv.resultValueClass(resultValueOptions || {});
    					}

    					return OpenLayers.Util.extend({
    						visualization : new OpenLayers.VIS.Symbology.NumericVector(),
    						resultValue : new OpenLayers.VIS.ResultValue.Custom({
    							resultValueMap : resultValueMap
    						}),
    						strategies : [ new OpenLayers.Layer.VIS.Strategy.FeatureProjection() ]

    					}, resourceOptions.layerOptions);
    				}
    			});
    			break;// Never iterate, only to check if map has entries
    		}

    		// Add nodes based on the collected node options
    		for ( var i = 0; i < vectorLayerOptions.length; i++) {
    			vectorLayerOptions[i] = OpenLayers.Util.extend(OpenLayers.Util.extend({}, commonLayerOptions),
    					vectorLayerOptions[i]);
    			vectorLayerOptions[i].loaderId = '' + i;
    		}

    		callback(vectorLayerOptions);
    	},

    	/**
    	 * Loads level for all datasets of a VISS resource
    	 */
    	viss_root : function(resourceOptions, callback) {
    		var resource = resourceOptions.resource;

    		resource.getResource(function(resourceInfo) {
    			if (resourceInfo instanceof Error) {
    				// callback receives Error object in
    				// case of exceptions
    				callback(resourceInfo);
    				return;
    			}

    			resource.getDataSets(function(dataSets) {
    				if (dataSets instanceof Error) {
    					// callback receives Error
    					// object in case of
    					// exceptions
    					callback(dataSets);
    					return;
    				}
    				var dataSetOptions = [];
    				for ( var i = 0, len = dataSets.length; i < len; i++) {
    					// Add node for each data set
    					dataSetOptions.push(OpenLayers.Util.extend(OpenLayers.Util.extend({}, resourceOptions), {
    						text : dataSets[i].phenomenon || dataSets[i].id,
    						// use phenomenon if available
    						resourceLoader : 'viss_dataset',
    						loaderId : dataSets[i].id,
    						dataSet : dataSets[i],
    						// include data set information as sent by VISS
    						iconCls : 'icon-raster',
    						leaf : false
    					}));
    				}
    				callback(dataSetOptions);
    			});
    		});
    	},

    	/**
    	 * Creates level for all visualizers of a VISS dataset
    	 */
    	viss_dataset : function(resourceOptions, callback) {
    		// var self = this; // variable never read
    		var dataSet = resourceOptions.dataSet;

    		// Load data set
    		dataSet.getDataSet(function(info) {
    			if (info instanceof Error) {
    				// callback receives Error object in case of
    				// exceptions
    				callback(info);
    				return;
    			}
    			resourceOptions.text = info.phenomenon;
    		});

    		// get visualizers of VISS data set
    		dataSet.getVisualizers(function(visualizers) {
    			if (visualizers instanceof Error) {
    				// callback receives Error object in
    				// case of exceptions
    				callback(visualizers);
    				return;
    			}
    			var visualizerOptions = [];
    			for ( var i = 0, len = visualizers.length; i < len; i++) {
    				// add node for each data set
    				visualizerOptions.push(OpenLayers.Util.extend(OpenLayers.Util.extend({}, resourceOptions), {
    					text : visualizers[i].id,
    					loaderId : visualizers[i].id,
    					visualizer : visualizers[i], // include
    					// visualizer info as sent by VISS
    					iconCls : 'icon-raster-visualization',
    					leaf : true,
    					resourceLoader : 'viss_layer'
    				}));
    			}
    			callback(visualizerOptions);
    		});
    	},

    	/**
    	 * Loads level for all layers in ncWMS ('datasets')
    	 */
    	ncwms_root : function(resourceOptions, callback) {
    		// Get capabilities
    		OpenLayers.Request
    				.GET({
    					url : resourceOptions.url,
    					params : {
    						SERVICE : 'WMS',
    						VERSION : '1.3.0',
    						REQUEST : 'GetCapabilities'
    					},
    					success : function(resp) {
    						var respStatus = resp.status;
    						resp = resp.responseXML || resp.responseText;
    						var capabilities;
    						try {
    							capabilities = new OpenLayers.Format.WMSCapabilities({
    								version : '1.3.0',
    								profile : 'ncWMS'
    							}).read(resp);
    						} catch (e) {
    							callback(new Error(e));
    							return;
    						}
    						if (!capabilities.capability) {
    							if (respStatus == 0) {
    								callback(new Error(
    										'Server seems to not support cross-origin resource sharing (CORS) or is not available'));
    							} else {
    								callback(new Error('Invalid capabilities response'));
    							}
    							return;
    						}

    						if (capabilities.capability.nestedLayers.length == 0) {
    							callback(new Error('No Layers'));
    						} else {
    							var layerOptions = [];
    							var layers = capabilities.capability.nestedLayers[0].nestedLayers;
    							for ( var i = 0; i < layers.length; i++) {
    								layerOptions.push(OpenLayers.Util.applyDefaults({
    									text : layers[i].title,
    									loaderId : layers[i].name || layers[i].title,
    									resourceLoader : 'ncwms_dataset',
    									wmsLayer : layers[i],
    									iconCls : 'icon-raster',
    									leaf : false,
    									capabilities : capabilities
    								}, resourceOptions));

    							}
    							callback(layerOptions);
    						}

    					},
    					failure : function(resp) {
    						callback(new Error(resp.responseText));
    					}
    				});

    	},

    	/**
    	 * Loads level for a dataset of WMS
    	 */
    	ncwms_dataset : function(resourceOptions, callback) {
    		var i;
    		var wmsLayer = resourceOptions.wmsLayer;

    		var commonLayerOptions = OpenLayers.Util.applyDefaults({
    			type : 'ncwms_layer',
    			resourceLoader : 'ncwms_layer',
    			iconCls : 'icon-raster-visualization',
    			leaf : true,
    			wmsLayer : wmsLayer
    		}, resourceOptions);

    		var layerNames = [];
    		var layerOptions = [];

    		// Special ncWMS visualizations
    		var visualizations = [
    				[ OpenLayers.Layer.VIS.WMSQ.ColorRange, 'Color Range' ],
    				[ OpenLayers.Layer.VIS.WMSQ.Whitening, 'Whitening' ],
    				[ OpenLayers.Layer.VIS.WMSQ.Contour, 'Isolines' ],
    				[ OpenLayers.Layer.VIS.WMSQ.Glyphs, 'Glyphs' ],
    				[ OpenLayers.Layer.VIS.WMSQ.ExceedanceProbability, 'Exceedance Probability' ],
    				[ OpenLayers.Layer.VIS.WMSQ.ConfidenceInterval, 'Confidence Interval' ] ];

    		for (i = 0; i < visualizations.length; i++) {
    			layerOptions.push({
    				text : visualizations[i][1],
    				layerClass : OpenLayers.Layer.VIS.WMSQ,
    				requiredLayers : visualizations[i][0].prototype.requiredLayers,
    				// Layer config options from base prototype
    				visualizationClass : visualizations[i][0],
    				getLayerOptions : function() {
    					var layers = {};
    					for ( var key in this.requiredLayers[this.requiredLayersType].layers) {
    						if (!this.layers[key]) {
    							throw 'No layer settings for ' + key;
    						}
    						layers[key] = this.layers[key];
    					}
    					layers.requiredLayersType = this.requiredLayersType;
    					// include requiredLayersType in visualization
    					return {
    						visualization : new this.visualizationClass(layers)
    					};
    				}
    			});
    		}

    		for (i = 0; i < layerOptions.length; i++) {
    			layerOptions[i] = OpenLayers.Util.applyDefaults(layerOptions[i], commonLayerOptions);
    			layerOptions[i].loaderId = '' + i;
    		}

    		if (wmsLayer.nestedLayers) {
    			// Handle nested layers, they can again have nested quality layers ->
    			// add dataset node for every nested layer with nested layers
    			for (i = 0; i < wmsLayer.nestedLayers.length; i++) {
    				var nestedLayer = wmsLayer.nestedLayers[i];
    				if (nestedLayer.nestedLayers && nestedLayer.nestedLayers.length !== 0) {
    					layerOptions.push(OpenLayers.Util.applyDefaults({
    						text : nestedLayer.title,
    						loaderId : nestedLayer.name || nestedLayer.title,
    						resourceLoader : 'ncwms_dataset',
    						wmsLayer : nestedLayer,
    						iconCls : 'icon-raster',
    						leaf : false
    					}, resourceOptions));
    				}
    			}
    		}

    		callback(layerOptions);
    	},

    	/**
    	 * Loads level for all layers in WMS
    	 */
    	wms_root : function(resourceOptions, callback) {
    		// Get capabilities
    		OpenLayers.Request
    				.GET({
    					url : (VIS.wmsCapabilitiesProxy && resourceOptions.noProxy !== true) ? (VIS.wmsCapabilitiesProxy + '?URL=' + resourceOptions.url)
    							: resourceOptions.url,
    					params : {
    						SERVICE : 'WMS',
    						VERSION : '1.1.1',
    						REQUEST : 'GetCapabilities'
    					},
    					success : function(resp) {
    						var respStatus = resp.status;
    						resp = resp.responseXML || resp.responseText;
    						var capabilities;
    						try {
    							capabilities = new OpenLayers.Format.WMSCapabilities().read(resp);
    						} catch (e) {
    							callback(new Error(e));
    							return;
    						}
    						if (!capabilities.capability) {
    							if (respStatus == 0) {
    								if (resourceOptions.noProxy === true) {
    									// Loading fails if capabilities request fails even when not
    									// using proxy
    									callback(new Error(
    											'Server seems to not support cross-origin resource sharing (CORS) or is not available'));
    								} else {
    									// try to load again without proxy, if proxy loading failed
    									resourceOptions.noProxy = true;
    									VIS.ResourceLoader.resourceLoader.wms_root.call(this, resourceOptions, callback);
    								}
    							} else {
    								callback(new Error('Invalid capabilities response'));
    							}
    							return;
    						}

    						if (capabilities.capability.nestedLayers.length == 0) {
    							callback(new Error('No Layers'));
    						} else {
    							var layerOptions = [];
    							var layers = capabilities.capability.layers;
    							for ( var i = 0; i < layers.length; i++) {
    								layerOptions.push(OpenLayers.Util.applyDefaults({
    									text : layers[i].title,
    									loaderId : layers[i].title || layers[i].name,
    									resourceLoader : 'wms_layer',
    									wmsLayer : layers[i],
    									supportedSrs : layers[i].srs || layers[i].crs,
    									iconCls : 'icon-raster',
    									leaf : true,
    									capabilities : capabilities,
    									layerClass : OpenLayers.Layer.VIS.WMS,
    									getParamOptions : function() {
    										return {
    											layers : this.wmsLayer.name,
    											styles : ''
    										};
    									}
    								}, resourceOptions));
    							}
    							callback(layerOptions);
    						}

    					},
    					failure : function(resp) {
    						callback(new Error(resp.responseText));
    					}
    				});
    	},

    	ncwms_layer : function(resourceOptions, callback) {
    		var layerOptions = resourceOptions.getLayerOptions ? resourceOptions.getLayerOptions() : {};

    		// var availSrs = resourceOptions.wmsLayer.srs ||
    		// resourceOptions.wmsLayer.crs;

    		// var mapProjection = resourceOptions.requiredProjection
    		// || new OpenLayers.Projection('EPSG:3857');
    		// var layerProjection = null, proj = null;

    		// Find compatible projection (usually 3875 or 900913)
    		// for ( var srs in availSrs) {
    		// proj = new OpenLayers.Projection(srs);
    		// if (mapProjection.equals(proj)) {
    		// layerProjection = proj;
    		// break;
    		// }
    		// }

    		// if (!layerProjection) {
    		// if (resourceOptions.selectProjectionCallback) {
    		// layerProjection = resourceOptions.selectProjectionCallback(availSrs);
    		//
    		// // TODO return if no selection?
    		// }
    		// }
    		// if (!layerProjection) {
    		// throw new Error('No SRS');
    		// }

    		// if (!layerProjection) {
    		// // No compatible projection found
    		// if ('EPSG:4326' in availSrs) {
    		// // Fallback to 4326, pseudo reprojection by still using 3857 grid,
    		// // but overriding request parameters to use corresponding 4326 bounds
    		// layerProjection = new OpenLayers.Projection('EPSG:3857');
    		//
    		// if (parseFloat(resourceOptions.capabilities.version) >= 1.3) {
    		// // 4326 is reversed if WMS version >= 1.3.0
    		// layerOptions.reverseAxisOrder = function() {
    		// return true;
    		// };
    		// } else {
    		// layerOptions.reverseAxisOrder = function() {
    		// return false;
    		// };
    		// }
    		//
    		// layerOptions.getURL = function(bounds) {
    		// // transform bounds but do not change original ones
    		// bounds = bounds.clone();
    		// var proj = new OpenLayers.Projection('EPSG:4326');
    		// bounds.transform(this.map.getProjectionObject(), proj);
    		//
    		// return resourceOptions.layerClass.prototype.getURL.apply(this,
    		// arguments);
    		// };
    		// layerOptions.getFullRequestString = function(newParams, altUrl) {
    		// // Directly use EPSG:4326 in request string
    		// var value = 'EPSG:4326';
    		// if (parseFloat(this.params.VERSION) >= 1.3) {
    		// this.params.CRS = value;
    		// } else {
    		// this.params.SRS = value;
    		// }
    		//
    		// if (typeof this.params.TRANSPARENT == "boolean") {
    		// newParams.TRANSPARENT = this.params.TRANSPARENT ? "TRUE" : "FALSE";
    		// }
    		//
    		// return OpenLayers.Layer.Grid.prototype.getFullRequestString.apply(this,
    		// arguments);
    		// };
    		// layerOptions.warning = 'This WMS is not compatible with '
    		// + mapProjection.toString()
    		// + '. Greenland will reproject this resource, which may result in less
    		// accurate visualizations.';
    		// } else {
    		// // Even 4326 not available
    		// throw new Error('No SRS compatible with ' + mapProjection.toString());
    		// }
    		// }

    		var layer = new resourceOptions.layerClass('Test', resourceOptions.url, OpenLayers.Util.extend({
    			transparent : true,
    			styles : 'boxfill/greyscale',
    			version : resourceOptions.capabilities.version,
    		}, resourceOptions.getParamOptions ? resourceOptions.getParamOptions() : {}), OpenLayers.Util.extend({
    			opacity : 0.8,
    			// projection : layerProjection,
    			resourceOptions : {
    				mime : resourceOptions.mime,
    				url : resourceOptions.url
    			},
    			wmsLayer : resourceOptions.wmsLayer,
    			supportedSrs : resourceOptions.wmsLayer.srs || resourceOptions.wmsLayer.crs,
    			capabilities : resourceOptions.capabilities
    		}, layerOptions));

    		callback(layer);
    	},

    	viss_layer : function(resourceOptions, callback) {

    		// Callback for async getVisualization call
    		var visualizationCallback = function(visualization) {
    			if (visualization instanceof Error) {
    				callback(visualization);
    				return;
    			}

    			var layer = new OpenLayers.Layer.VIS.Raster('Raster', visualization, {
    				opacity : 0.7,
    				transitionEffect : 'resize',
    				removeBackBufferDelay : 0,
    				className : 'olLayerGridSingleTile', // disable fade effects
    				resourceOptions : {
    					mime : resourceOptions.mime,
    					url : resourceOptions.url
    				}
    			});

    			callback(layer);
    		};

    		resourceOptions.visualizer.getVisualization(visualizationCallback.createDelegate(this));
    	},

    	vector_layer : function(resourceOptions, callback) {
    		var layerOptions = resourceOptions.getLayerOptions();

    		var layer = new OpenLayers.Layer.VIS.Vector('Vector', OpenLayers.Util.extend({
    			projection : new OpenLayers.Projection('EPSG:3857'), // TODO
    			opacity : 0.8,
    			transitionEffect : 'resize',
    			resourceOptions : {
    				mime : resourceOptions.mime,
    				url : resourceOptions.url
    			}
    		}, layerOptions));

    		callback(layer);
    	},

    	wms_layer : function(resourceOptions, callback) {
    		this.ncwms_layer(resourceOptions, callback);
    	}
    }
};