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
 * Visualization to extract contour lines from ncWMS tiles by applying a
 * marching squares algorithm.
 */
OpenLayers.Layer.VIS.WMSQ.Contour = OpenLayers.Class(OpenLayers.Layer.VIS.WMSQ.Vector, {
    requiredLayers : {
    	'default' : {
    		layers : {
    			valueLayer : {
    				title : 'Value Layer',
    				description : 'Variable to analyze'
    			}
    		}
    	}
    },

    valueLayer : null,

    initialize : function(options) {
    	options.valueLayer.styler = {
    		bounds : new OpenLayers.VIS.Styler.EqualIntervals()
    	};

    	options.styler = {
    		strokeColor : VIS.createPropertyArray([ new OpenLayers.VIS.Styler.Color({
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
    		}) ], {
    			fieldLabel : 'Color Scheme',
    			attribute : '_level'
    		}),
    		strokeWidth : new OpenLayers.VIS.Styler.StrokeWidth()
    	// TODO opacity
    	};

    	options.layerOptions = [ options.valueLayer ];

    	// General parameters specific for this visualization.
    	options.parameters = {
    		step : {
    			fieldLabel : 'Contouring Quality',
    			value : 2,
    			minimum : 1,
    			maximum : 50,
    			type : 'integer',
    			description : 'Contouring step value (Test)',
    			action : function(value) {
    				if (value >= 1) {
    					this.events.triggerEvent('change', {
    						property : 'symbology'
    					});
    				}
    			},
    			scope : this,
    			required : true
    		},
    		group : 'Result'
    	};

    	OpenLayers.Layer.VIS.WMSQ.Vector.prototype.initialize.apply(this, arguments);

    	// Styler needs bounds for legend
    	this.styler.bounds = this.valueLayer.styler.bounds;
    },

    setLayer : function(layer) {
    	OpenLayers.Layer.VIS.WMSQ.Vector.prototype.setLayer.call(this, layer);

    	this.layer.tileOptions.shouldDraw = function() {
    		if (!(this.bounds.toBBOX() in this.layer.visualization.featureCache) || this.forceRedraw) {
    			return this.layer.tileClass.prototype.shouldDraw.call(this);
    		} else {
    			return false;
    		}
    	};
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
    		legendSymbolType : 'Line'
    	});

    	OpenLayers.Layer.VIS.WMSQ.Vector.prototype.update.call(this);
    },

    /**
     * Called by each completely loaded tile. Extracts contours for each contour
     * level
     *
     * @param canvas
     * @param merger
     * @param ctx
     * @param tile
     */
    fillCanvas : function(canvas, merger, ctx, tile) {

    	var features = [];
    	var levels = this.valueLayer.styler.bounds.getInts();
    	var contours, contour, lineString, level;

    	for ( var i = levels.length - 1; i >= 0; i--) {
    		// Find contours for each level
    		level = levels[i][0];

    		contours = this.findContour(merger, level, tile);

    		for ( var j = 0; j < contours.length; j++) {
    			// for each contour line of a level
    			contour = contours[j];
    			for ( var k = 0; k < contour.length; k++) {
    				// for each point of a contour line, transform to
    				// OpenLayers.Geometry.Point
    				contour[k] = new OpenLayers.Geometry.Point(contour[k][0], contour[k][1]);
    			}

    			// Build OpenLayers.Geometry.LineString
    			lineString = new OpenLayers.Geometry.LineString(contour);
    			// lineString.simplify(5); // Douglas-Peucker
    			// TODO account for actual resolution

    			features.push(new OpenLayers.Feature.Vector(lineString, {
    				_level : level
    			}));
    		}

    	}
    	this.addFeatures(features, tile.bounds);

    },

    /**
     * Actual contouring function.
     *
     * @param merger
     * @param level
     * @param tile
     * @returns {Array}
     */
    findContour : function(merger, level, tile) {
    	var width = tile.size.w, height = tile.size.h;
    	var contourIndexChannel = 2;
    	// image component to use for contour indices, blue channel
    	var gx = gy = this.parameters.step.value;
    	var layer = this.valueLayer;

    	// Set contour indices
    	for ( var y = 0; y < height; y += gy) {
    		for ( var x = 0; x < width; x += gx) {
    			contourIndex = 0;
    			// LSB
    			if (layer.getValue(merger, x - gx, y - gy) > level)
    				contourIndex |= 1;
    			if (layer.getValue(merger, x, y - gy) > level)
    				contourIndex |= 2;
    			if (layer.getValue(merger, x - gx, y) > level)
    				contourIndex |= 4;
    			// MSB
    			if (layer.getValue(merger, x, y) > level)
    				contourIndex |= 8;

    			layer.setComponent(merger, x, y, contourIndexChannel, contourIndex);
    		}
    	}

    	// Direction table mapping contour index to step direction
    	var directionTable = [//
    	[ 1, 0 ], [ 0, -1 ], [ 1, 0 ], //
    	[ 1, 0 ], [ -1, 0 ], [ 0, -1 ],//
    	[ 0, 0 ], // ambiguous case
    	[ 1, 0 ], [ 0, 1 ], [ 0, 0 ], // ambiguous case
    	[ 0, 1 ], [ 0, 1 ], [ -1, 0 ],//
    	[ 0, -1 ], [ -1, 0 ] ];

    	for ( var i = 0; i < directionTable.length; i++) {
    		directionTable[i] = [ directionTable[i][0] * gx, directionTable[i][1] * gy ];
    	}

    	var isoLines = [], isoLine, point, currentIsoLine, x, y, dx, dy, pdx, pdy;

    	var step = [ NaN, NaN ], previousStep, currentPos;

    	for ( var ys = gy; ys < height; ys += gy) {
    		for ( var xs = gx; xs < width; xs += gx) {
    			contourIndex = layer.getComponent(merger, xs, ys, contourIndexChannel);
    			if (contourIndex == 0 || contourIndex >= 15)
    				continue;

    			// Optimization, march northward edges beginning at the bottom
    			if ((directionTable[contourIndex][0] < 0 || directionTable[contourIndex][1] < 0)
    					&& ys < height - 1 - gy && xs < width - 1 - gy)
    				continue;

    			currentIsoLine = [];
    			currentPos = [ xs, ys ];
    			previousStep = [ NaN, NaN ];

    			do {
    				contourIndex = layer.getComponent(merger, currentPos[0], currentPos[1], 2);
    				if (contourIndex == 0) {
    					// Cell already visited
    					if (currentIsoLine.length > 0)
    						currentIsoLine.push(currentPos.slice(0));
    					break;
    				}

    				// Get next step
    				if (contourIndex == 6) {
    					// Ambiguous case
    					step[0] = previousStep[1] < 0 - 1 ? -gx : gx;
    					step[1] = 0;
    				} else if (contourIndex == 9) {
    					// Ambiguous case
    					step[0] = 0;
    					step[1] = previousStep[0] > 0 ? -gy : gy;
    				} else {
    					step = directionTable[contourIndex].slice(0);
    					// Clear contour index, do not visit again
    					layer.setComponent(merger, currentPos[0], currentPos[1], contourIndexChannel, 0);
    				}

    				if (step[0] != previousStep[0] || step[1] != previousStep[1]) {
    					// Add point to line if direction changes
    					currentIsoLine.push(currentPos.slice(0));
    					previousStep = step.slice(0);

    					// TODO interpolation, accounting for actual resolution
    				}

    				currentPos[0] += step[0];
    				currentPos[1] += step[1];

    				if (currentPos[0] <= 0 || currentPos[0] >= width || currentPos[1] <= 0
    						|| currentPos[1] >= height) {
    					// End line if step leaves image
    					currentIsoLine.push([ currentPos[0] - step[0], currentPos[1] - step[1] ]);
    					break;
    				}

    			} while (xs != currentPos[0] || ys != currentPos[1]
    					|| (currentIsoLine.push(currentPos.slice(0)) && false));

    			addLine = true;
    			currentLastPoint = currentIsoLine[currentIsoLine.length - 1];
    			for ( var i = 0; i < isoLines.length; i++) {
    				l = isoLines[i];
    				lastPoint = l[l.length - 1];
    				if (Math.abs(currentIsoLine[0][0] - lastPoint[0]) <= gx
    						&& Math.abs(currentIsoLine[0][1] - lastPoint[1]) <= gy) {
    					isoLines[i] = l.concat(currentIsoLine);
    					addLine = false;
    					break;
    				}

    				if (Math.abs(l[0][0] - currentLastPoint[0]) <= gx
    						&& Math.abs(l[0][1] - currentLastPoint[1]) <= gy) {
    					isoLines[i] = currentIsoLine.concat(l);
    					addLine = false;
    					break;
    				}
    			}
    			if (addLine)
    				isoLines.push(currentIsoLine);

    		}
    	}

    	var widthRatio = tile.bounds.getWidth() / width;
    	var heightRatio = tile.bounds.getHeight() / height;
    	for ( var i = 0; i < isoLines.length; i++) {
    		isoLine = isoLines[i];
    		for ( var j = 0; j < isoLine.length; j++) {
    			point = isoLine[j];
    			point[0] = tile.bounds.left + point[0] * widthRatio;
    			point[1] = tile.bounds.top - point[1] * heightRatio;
    		}
    	}

    	return isoLines;
    },

    getTitle : function() {
    	return 'Contouring Test, ' + this.valueLayer.name;
    }

});
