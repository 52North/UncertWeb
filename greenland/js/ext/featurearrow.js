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
 *
 * Extending Ext JS Library 3.4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
Ext.namespace('Ext.ux.VIS');
/*
 * Based on GeoExt.FeatureRenderer class, represents a simple triangle to point
 * to a specific feature
 */
Ext.ux.VIS.FeatureArrow = Ext.extend(GeoExt.FeatureRenderer, {

    startPos : null,

    endPos : null,

    startWidth : null,

    orientation : 'vertical',

    initComponent : function() {
    	Ext.ux.VIS.FeatureArrow.superclass.initComponent.apply(this, arguments);

        this.feature = this._createTriangle([[0,0],[0,50],[-50,25]]);

    	this.startPos = [ 0, 0 ];
    	this.endPos = [ 0, 0 ];
    	this.startWidth = 100;
    },

    _createTriangle: function(x) {
        var points = [
            new OpenLayers.Geometry.Point(x[0][0], x[0][1]),
            new OpenLayers.Geometry.Point(x[1][0], x[1][1]),
            new OpenLayers.Geometry.Point(x[2][0], x[2][1]),
            new OpenLayers.Geometry.Point(x[0][0], x[0][1])
        ];
        var ring = new OpenLayers.Geometry.LinearRing(coordinates);
        var polygon = new OpenLayers.Geometry.Polygon([ring]);
        return new OpenLayers.Feature.Vector(polygon);
    },

    setStartPosition : function(x, y) {
    	this.startPos = [x, y];
    },

    setStartWidth : function(w) {
    	this.startWidth = w;
    },

    setEndPosition : function(x, y) {
    	this.endPos = [x, y];
    },

    setOrientation : function(orientation) {
    	this.orientation = orientation;
    },

    _getOffsetForOrientation: function() {
        switch (this.orientation) {
            case 'vertical':
                return [0, this.startWidth];
            case 'horizontal':
                return [this.startWidth, 0];
        }
    },

    updateFeature : function() {
        var offset = this._getOffsetForOrientation();
    	var height = this.endPos[1] - this.startPos[1];
        this.setFeature(this._createTriangle([
            [this.startPos[0], height - this.startPos[1]],
            [this.startPos[0] + offset[0], height - this.startPos[1] - offset[1]],
            [this.endPos[0], height - this.endPos[1]]
        ]));
    }

});
