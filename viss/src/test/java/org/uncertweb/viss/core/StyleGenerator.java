package org.uncertweb.viss.core;

import static org.uncertweb.utils.UwXmlUtils.defaultOptions;
import static org.uncertweb.utils.UwXmlUtils.Namespace.SLD;

import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.sld.ColorMapDocument.ColorMap;
import net.opengis.sld.ColorMapEntryDocument.ColorMapEntry;
import net.opengis.sld.NamedLayerDocument.NamedLayer;
import net.opengis.sld.RasterSymbolizerDocument.RasterSymbolizer;
import net.opengis.sld.StyledLayerDescriptorDocument;
import net.opengis.sld.StyledLayerDescriptorDocument.StyledLayerDescriptor;

import org.apache.xmlbeans.XmlCursor;
import org.uncertweb.utils.UwCollectionUtils;


public class StyleGenerator {
	
	public static interface Color {
		public String toHex();
	}
	
	public static class RGB implements Color {
		private static final String HEX = "0123456789ABCDEF";
		private int r,g,b;
		public RGB(int r, int g, int b) {
			testValue(r);
			testValue(g);
			testValue(b);
		}
		private void testValue(int v) {
			if (v >= 256) {
				throw new IllegalArgumentException();
			}
		}
		
		@Override
		public String toHex() {
			return new StringBuffer(7)
				.append("#")
				.append(hexify(this.r))
				.append(hexify(this.g))
				.append(hexify(this.b))
				.toString();
		}
		
		private static String hexify(int i) {
			int lsd = i % 16;
			int msd = (i - lsd) / 16;
			return HEX.charAt(msd) + "" + HEX.charAt(lsd);
		}
		
		@Override
		public String toString() {
			return toHex();
		}
	}
	
	
	public static class HSV implements Color {
		private int h, s, v;
		
		public HSV(int h, int s, int v) {
			this.h = h;
			this.s = s;
			this.v = v;
		}
		
		public RGB toRGB(){
			double h = this.h/360;
			double s = this.s/100;
			double v = this.v/100;
			double r = 0, g = 0, b = 0;
			if (s == 0) {
				r = g = b = v;
			} else {
				double h6 = h * 6;
				int i = (int) Math.floor(h6);
				double x = v * (1 - s);
				double y = v * (1 - s * (h6 - i));
				double z = v * (1 - s * (1 - (h6 - i)));
				switch(i) {
				case 0: r = v; g = z; b = x; break;
				case 1: r = y; g = v; b = x; break;
				case 2: r = x; g = v; b = z; break;
				case 3: r = x; g = y; b = v; break;
				case 4: r = z; g = x; b = v; break;
				case 5: r = v; g = x; b = y; break;
				}
			}
			return new RGB((int) (r * 255), 
						   (int) (g * 255), 
						   (int) (b * 255));
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
	
	private static final Color LESS = new RGB(  0, 0, 0);
	private static final Color MORE = new RGB(255, 0, 0);
	
	private int hueMin = 90;
	private int hueMax = 15;
	
	private double min;
	private double max;
	private int intervals;
	
	
	
	public Color getColor(double v) {
		if (this.intervals <= 1 || v  < this.min) {
			return LESS;
		}
		if (v >= max) {
			return MORE;
		}
		double valueIntervalsSize = (this.max - this.min)/(this.intervals - 1);
		double hueIntervalSize = (this.hueMin - this.hueMax)/(this.intervals - 1);
		double segment = Math.floor((v- this.min)/valueIntervalsSize);
		int hue = (int) (hueMin - segment * hueIntervalSize);
		return new HSV(hue, 100, 100);
	}
	
	public static void main(String[] agrs) {
	
		System.out.println(new StyleGenerator().getSld().xmlText(defaultOptions()));
		
	}
	
	public StyledLayerDescriptorDocument getSld() {
		StyledLayerDescriptorDocument doc = StyledLayerDescriptorDocument.Factory.newInstance();
		StyledLayerDescriptor sld = doc.addNewStyledLayerDescriptor();
		sld.setVersion("1.0.0");
		NamedLayer nl = sld.addNewNamedLayer();
		nl.setName("defaultStyle");

		ColorMap map = ((RasterSymbolizer) nl
							.addNewUserStyle()
							.addNewFeatureTypeStyle()
							.addNewRule()
							.addNewSymbolizer()
							.substitute(SLD.q("RasterSymbolizer"), RasterSymbolizer.type))
								.addNewColorMap();

		XmlCursor c = map.newCursor();
		c.setAttributeText(new QName("type"), "intervals");
		c.dispose();
		
		class MapEntry {
			Color c;
			double o;
			double q;
			
			public MapEntry(Color c, double o, double q) {
				this.c = c;
				this.o = o;
				this.q = q;
			}
		}
		
		List<MapEntry> entries = UwCollectionUtils.list(new MapEntry(new RGB(60, 60, 60), .8, 15)); //TODO
		
		for (MapEntry me  : entries) {
			ColorMapEntry e = map.addNewColorMapEntry();
			e.setColor(me.c.toHex());
			e.setOpacity(me.o);
			e.setQuantity(me.q);
		}
		
		
		return doc;
	}
	
	
}