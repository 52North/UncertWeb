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
// calculates the greatest common divisior of two numbers... don't want to know how...
OpenLayers.Util.gcd = function(u,v){
	if (u == 0 || v==0 ) {
		return u | v;
	}
	var k;
	for (k = 0;((u | v) & 1) == 0; ++k){
		u >>= 1; v>>=1;
	}
	while ((u & 1) == 0) {
		u>>=1;
	}
	do{
		while ((v & 1) == 0) {
			v >>= 1;
		}
		if (u < v) {
			v -= u;
		} else {
			var d = u - v;
			u = v;
			v = d;
		}
		v >>= 1;
	} while (v != 0);
	return u << k;
};

OpenLayers.Util.analyzeFeatures = function (features) {

	function getValueMinMax(v) {
		if (typeof(v) === 'number') {
			return [v, v];
		} else if (v.getClassName && v.getClassName().match('.*Distribution$')) {
			var m = v.getMean(); return [m, m];
		} else if (v.length) {
			var m = [ Number.POSITIVE_INFINITY,
					  Number.NEGATIVE_INFINITY ];
			for (var k = 0; k < values[j][1].length; k++) {
				if (v[k] < m[0]) { m[0] = v[k]; }
				if (v[k] > m[1]) { m[1] = v[k]; }
			}
			return m;
		} else {
			throw "Unsupported Type " + v;
		}
	}

	var meta = {
		proposedTitle: null,
		uom: null,
		containsProbabilities: false,
		probabilityConstraint: null,
		min: Number.POSITIVE_INFINITY,
		max: Number.NEGATIVE_INFINITY,
		time: {
			min: Number.POSITIVE_INFINITY,
			max: Number.NEGATIVE_INFINITY,
			step: Number.NaN
		}
	}

	for (var i = 0; i < features.length; i++) {
		var values = features[i].getValues();
		if (!meta.uom) {
			meta.uom = features[i].getUom();
		}

		for (var j = 0; j < values.length; j++) {
			var t = values[j][0], v = values[j][1];
			if (v.probability) {
				meta.containsProbabilities = true;
				if (!meta.probabilityConstraint) {
					meta.probabilityConstraint = v.constraint;
				} else if (meta.probabilityConstraint !== v.constraint) {
					throw 'Mixed constraints are not supporeted: "'
						+ meta.probabilityConstraint + '" != "'
						+ v.constraint + '".';
				}
			} else if (meta.containsProbabilities) {
				throw 'Probabilities mixed with normal values are not supported.';
			} else try {
				// check value extent
				var mm = getValueMinMax(v)
				if (mm[0] < meta.min) {
					meta.min = mm[0];
				}
				if (mm[1] > meta.max) {
					meta.max = mm[1];
				}
			} catch(e) {
				meta.failed = e;
				return meta;
			}

			// check temporal extent
			if (t.length == 1) {
				t = [t[0], t[0]];
			}
			if (t[0] < meta.time.min) {
				meta.time.min = t[0];
			}
			if (t[1] > meta.time.max) {
				meta.time.max = t[1];
			}

			// check step between times
			if (j < values.length-1) {
				// just check the distance between the begins of intervals
				var curStep = values[j+1][0][0] - values[j][0][0];
				if (isNaN(meta.time.step) || curStep < meta.time.step) {
					meta.time.step = curStep;
				}
			}
		}
	}
	if (isNaN(meta.time.step)) {
		meta.time.step = 0;
	}
	if (meta.probabilityConstraint) {
		meta.proposedTitle = meta.probabilityConstraint;
	} else {
		meta.proposedTitle = 'Request@' + new Date().toGMTString();
	}
	return meta;
}
