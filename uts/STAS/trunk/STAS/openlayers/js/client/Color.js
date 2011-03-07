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
OpenLayers.Color = {
	RGB: OpenLayers.Class({
		CLASS_NAME: "OpenLayers.Color.RGB",
		HEX_DIGITS: '0123456789ABCDEF',
		initialize: function(red, green, blue) {
			this.r = red;
			this.g = green;
			this.b = blue;
		},
		toHex: function() {
			return '#' + this.hexify(this.r) 
					   + this.hexify(this.g) 
					   + this.hexify(this.b);
		},
		hexify: function(number) {
			var lsd = number % 16;
			var msd = (number - lsd) / 16;	
			return this.HEX_DIGITS.charAt(msd) + this.HEX_DIGITS.charAt(lsd);
		}
	}),
	HSV: OpenLayers.Class({
		CLASS_NAME: "OpenLayers.Color.HSV",
		initialize: function(hue,sat,val) {
			this.h = hue;
			this.s = sat;
			this.v = val;
		},
		toRGB: function() {
			var h = this.h / 360;
			var s = this.s / 100;
			var v = this.v / 100;
			var r, g, b;
			if (s == 0) {
				r = g = b = v;
			} else {
				h6 = h*6;
				i = Math.floor(h6);
				a = v*(1-s);
				b = v*(1-s*(h6-i));
				c = v*(1-s*(1-(h6-i)));
				switch (i) {
					case 0: r=v; g=c; b=a; break;
					case 1: r=b; g=v; b=a; break;
					case 2: r=a; g=v; b=c; break;
					case 3: r=a; g=b; b=v; break;
					case 4: r=c; g=a; b=v; break;
					case 5: r=v; g=a; b=b; break;
				}
			}
			return new OpenLayers.Color.RGB(r * 255, g * 255, b * 255);
		}
	})
};

