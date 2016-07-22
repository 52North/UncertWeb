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
package org.uncertweb.viss;


import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.sld.ColorMapDocument.ColorMap;
import net.opengis.sld.ColorMapEntryDocument.ColorMapEntry;
import net.opengis.sld.NamedLayerDocument.NamedLayer;
import net.opengis.sld.RasterSymbolizerDocument;
import net.opengis.sld.RasterSymbolizerDocument.RasterSymbolizer;
import net.opengis.sld.StyledLayerDescriptorDocument;
import net.opengis.sld.StyledLayerDescriptorDocument.StyledLayerDescriptor;

import org.apache.xmlbeans.XmlCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwXmlUtils;



public class StyleGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(StyleGenerator.class);

	private static final Color LESS = new RGB(  0, 0, 0);
	private static final Color MORE = new RGB(255, 0, 0);

	private final int hueMin = 90;
	private final int hueMax = 15;

	private final double min;
	private final double max;
	private final int intervals;
	private final double hueIntervalSize;

	public StyleGenerator(double min, double max, int intervals) {
		this.min = min; this.max = max; this.intervals = intervals;
		this.hueIntervalSize = (this.hueMin - this.hueMax)/(this.intervals - 1);
	}

	public Color getColor(double v) {
		if (this.intervals <= 1 || v  < this.min) {
			return LESS;
		}
		if (v >= max) {
			return MORE;
		}
		Color c = getColor(getSegment(v));
		LOG.debug("getColor({}) = {}", v, c);
		return c;
	}

	private int getSegment(double value) {
		return (int) Math.floor((value - this.min)/((this.max - this.min)/(this.intervals - 1)));
	}

	public Color getColor(int segment) {
		if (segment < 0) {
			LOG.debug("Returning LESS for {}", segment);
			return LESS;
		}
		if (segment >= this.intervals-1) {
			LOG.debug("Returning MORE for {}", segment);
			return MORE;
		}
		int hue = (int) (hueMin - segment * hueIntervalSize);
		LOG.debug("Hue: {}", hue);
		return new HSV(hue, 100, 100);
	}

	public StyledLayerDescriptorDocument getSld() {
        RasterSymbolizerDocument newInstance = RasterSymbolizerDocument.Factory.newInstance();
        RasterSymbolizer rasterSymbolizer = newInstance.addNewRasterSymbolizer();
		ColorMap map = rasterSymbolizer.addNewColorMap();


		XmlCursor c = map.newCursor();
		c.setAttributeText(new QName("type"), "intervals");
		c.dispose();

        class MapEntry {
            final Color c;
            final double o;
            final double q;

            MapEntry(Color c, double o, double q) {
                this.c = c;
                this.o = o;
                this.q = q;
            }
		}

		List<MapEntry> entries = UwCollectionUtils.list();
		entries.add(new MapEntry(getColor(Double.NEGATIVE_INFINITY), .8, Double.NEGATIVE_INFINITY));

		double sep = ((this.max - this.min)/(this.intervals - 1));
		for (double i = this.min; i < this.max; i+=sep) {
			entries.add(new MapEntry(getColor(i), .8, i));
		}
		entries.add(new MapEntry(getColor(this.max), .8, this.max));

		for (MapEntry me  : entries) {
			ColorMapEntry e = map.addNewColorMapEntry();
			e.setColor(me.c.toHex());
			e.setOpacity(me.o);
			e.setQuantity(me.q);
		}

        StyledLayerDescriptorDocument doc = StyledLayerDescriptorDocument.Factory.newInstance();
		StyledLayerDescriptor sld = doc.addNewStyledLayerDescriptor();
		sld.setVersion("1.0.0");
		NamedLayer nl = sld.addNewNamedLayer();
		nl.setName("defaultStyle");
        nl.addNewUserStyle().addNewFeatureTypeStyle().addNewRule().set(newInstance);
		return doc;
	}

	public static void main(String[] agrs) {
		System.out.println(new StyleGenerator(10,300,10).getSld().xmlText(UwXmlUtils.defaultOptions()));
	}
    public static interface Color {
        public String toHex();
    }
    public static class RGB implements Color {
        private static final String HEX = "0123456789ABCDEF";
        private final int r,g,b;
        public RGB(int r, int g, int b) {
            testValue(this.r = r);
            testValue(this.g = g);
            testValue(this.b = b);
            LOG.debug("RGB({},{},{})", new Object[] { r, g, b });
        }

        private void testValue(int v) {
            if (v > 255) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public String toString() {
            return toHex();
        }

        @Override
        public String toHex() {
            String s = new StringBuffer(7)
                    .append("#")
                    .append(hexify(this.r))
                    .append(hexify(this.g))
                    .append(hexify(this.b))
                    .toString();
            LOG.debug("hexify() = {}", s);
            return s;
        }

        private static String hexify(int i) {
            int lsd = i % 16;
            int msd = (i - lsd) / 16;
            return HEX.charAt(msd) + "" + HEX.charAt(lsd);
        }

    }
    public static class HSV implements Color {
        private final int h, s, v;

        public HSV(int h, int s, int v) {
            this.h = h;
            this.s = s;
            this.v = v;
        }

        public RGB toRGB(){
            final double h = this.h/ 60;
            final double s = this.s/100;
            final double v = this.v/100;
            double r = 0, g = 0, b = 0;
            if (s == 0) {
                r = g = b = v;
            } else {
                final int i = (int) Math.floor(h);
                final double x = v*(1-s);
                final double y = v*(1-s*(h-i));
                final double z = v*(1-s*(1-(h-i)));
                switch (i) {
                    case 0: r=v;g=z;b=x; break;
                    case 1: r=y;g=v;b=x; break;
                    case 2: r=x;g=v;b=z; break;
                    case 3: r=x;g=y;b=v; break;
                    case 4: r=z;g=x;b=v; break;
                    case 5: r=v;g=x;b=y; break;
                }
            }
            r *= 255; g *= 255; b *= 255;
            LOG.debug("HSV({},{},{})", new Object[] { this.h, this.s, this.v });
            LOG.debug("RGB({},{},{})", new Object[] { r, g, b });
            return new RGB((int) r, (int) g, (int) b);
        }

        @Override
        public String toHex() {
            return toRGB().toHex();
        }

        @Override
        public String toString() {
            return toHex();
        }
    }


}
