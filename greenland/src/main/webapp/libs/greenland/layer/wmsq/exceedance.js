/*
 * Copyright 2012 52�North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Simple visualization for ncWMS layer applying a colorrange to a single nested
 * layer.
 */
OpenLayers.Layer.WMSQ.ExceedanceProbability = OpenLayers.Class(OpenLayers.Layer.WMSQ.Visualization,
		{
			requiredLayers : {
				// Each configuration has a distirbutionClass field and a
				// getExceedanceprob function. These will be used to fill the pixel
				// array.
				normal : {
					title : 'Normal Distribution',
					layers : {
						meanLayer : {
							title : 'Mean Layer',
							description : 'Mean',
							uncertainty : {
								'normal#mean' : true
							}
						},
						sdLayer : {
							title : 'Standard Deviation Layer',
							description : 'Standard Deviation',
							uncertainty : {
								'normal#sd' : true, // Wenn es das gibt
								'normal#variance' : function(x) {
									return Math.sqrt(x);
								}
							}
						}
					},
					distributionClass : NormalDistribution, // jStat class
					getExceedanceProb : function(merger, x, y, distribution, threshold) {
						var mean = this.meanLayer.getValue(merger, x, y);
						var sd = this.sdLayer.getValue(merger, x, y);
						if (mean == null || sd == null) {
							return null;
						}

						// distribution will be a reused NormalDistirbution instance
						distribution._mean = mean;
						distribution._sigma = sd;

						return (1 - distribution.cumulativeDensity(threshold)) * 100;
					}

				},
				logNormal : {
					title : 'Log-Normal Distribution',
					layers : {
						locLayer : {
							title : 'Location Layer',
							description : 'Location',
							uncertainty : 'lognormal#location'
						},
						scaleLayer : {
							title : 'Scale Layer',
							description : 'Scale',
							uncertainty : 'lognormal#scale'
						}
					},
					distributionClass : LogNormalDistribution, // jStat class
					getExceedanceProb : function(merger, x, y, distribution, threshold) {
						var loc = this.locLayer.getValue(merger, x, y);
						var scale = this.scaleLayer.getValue(merger, x, y);
						if (loc == null || scale == null) {
							return null;
						}

						// distribution will be a reused LogNormalDistribution instance
						distribution._location = loc;
						distribution._scale = scale;

						return (1 - distribution.cumulativeDensity(threshold)) * 100;
					}
				}
			},

			initialize : function(options) {

				options.layerOptions = [];

				for ( var layerKey in this.requiredLayers[options.requiredLayersType].layers) {
					options[layerKey].styler = {
						bounds : [ new OpenLayers.VIS.Styler.Continuous(),
								new OpenLayers.VIS.Styler.EqualIntervals() ]
					};
					options.layerOptions.push(options[layerKey]);
				}

				options.styler = {
					fillColor : [ new OpenLayers.VIS.Styler.Color({
						predefinedColors : [ // 
						[ [ 120, 100, 100 ], [ 0, 100, 100 ] ], // Green-Red
						[ [ 30, 20, 100 ], [ 0, 100, 100 ] ], // Orange-Red
						[ [ 60, 20, 100 ], [ 120, 100, 80 ] ], // Yellow-Green
						[ [ 0, 100, 100 ], [ 359, 100, 100 ] ] // All
						],
						title : 'Multi Hue'
					}), new OpenLayers.VIS.Styler.Color({
						predefinedColors : [ // 
						[ [ 0, 0, 100 ], [ 0, 100, 100 ] ], // Red
						[ [ 30, 0, 100 ], [ 30, 100, 100 ] ], // Orange
						[ [ 120, 0, 100 ], [ 120, 100, 80 ] ], // Green
						[ [ 240, 0, 100 ], [ 240, 100, 80 ] ], // Blue
						[ [ 270, 0, 100 ], [ 270, 100, 80 ] ], // Purple
						[ [ 0, 0, 100 ], [ 0, 0, 0 ] ] // Gray
						],
						title : 'Single Hue'
					}) ],
					strokeWidth : {
						// Only for legend
						getValue : function() {
							return 0;
						}
					},
					bounds : [ new OpenLayers.VIS.Styler.Continuous(),
							new OpenLayers.VIS.Styler.EqualIntervals() ],
					opacity : new OpenLayers.VIS.Styler.Opacity()
				};
				options.styler.bounds.fixedMinValue = 0;
				options.styler.bounds.fixedMaxValue = 100;

				options.options = {
					threshold : {
						value : 0,
						type : 'number',
						description : 'Threshold for Exceedance Probability'
					}
				};

				OpenLayers.Layer.WMSQ.Visualization.prototype.initialize.apply(this, arguments);

			},

			update : function() {
				// Use OpenLayers.VIS.Symbology.Vector.updateLegend for setting
				// legendInfos by providing special context using this layer's styler
				// objects
				if (!this.legendInfos)
					this.legendInfos = [];
				OpenLayers.VIS.Symbology.Vector.prototype.updateLegend.call({
					legendInfos : this.legendInfos,
					styler : this.styler,
					events : this.events,
					legendSymbolType : 'Polygon'
				});

				OpenLayers.Layer.WMSQ.Visualization.prototype.update.call(this);
			},

			/**
			 * Applied for each tile, uses a color range for each pixel/value
			 * 
			 * @param imageData
			 * @param merger
			 */
			fillPixelArray : function(imageData, merger) {
				var cpa = imageData.data;
				var w = imageData.width;
				var reqLayers = this.requiredLayers[this.requiredLayersType];
				var distribution = new reqLayers.distributionClass();
				var threshold = this.options.threshold.value;
				var value;
				for ( var y = 0; y < imageData.height; y++)
					for ( var x = 0; x < w; x++) {

						value = reqLayers.getExceedanceProb.call(this, merger, x, y, distribution, threshold);
						if (value == null) {
							// No value -> transparent
							cpa[y * w * 4 + x * 4 + 3] = 0;
							continue;
						}

						rgb = this.styler.fillColor.getValueObject(value).toRGB();

						cpa[y * w * 4 + x * 4] = rgb.r;
						cpa[y * w * 4 + x * 4 + 1] = rgb.g;
						cpa[y * w * 4 + x * 4 + 2] = rgb.b;
						cpa[y * w * 4 + x * 4 + 3] = 255;
					}
			},

			getTitle : function() {
				return 'Exceedance Probability' + this.layerOptions[0].name;
			}

		});