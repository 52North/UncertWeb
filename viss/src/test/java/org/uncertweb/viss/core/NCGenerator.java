package org.uncertweb.viss.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.uncertweb.utils.UwCollectionUtils;

public class NCGenerator {
	
	interface Creator {
		double create(double old);
	}

	static Creator mean = new Creator() {
		public double create(double mean) {
			return mean
					+ (3.0 * r.nextDouble() * (r.nextDouble() < 0.5 ? 1.0
							: -1.0));
		}
	};

	static Creator variance = new Creator() {
		public double create(double variance) {
			return Math.abs(variance
					+ (r.nextDouble() * (r.nextDouble() < 0.5 ? 1.0 : -1.0)));
		}
	};

	static final String BASE_DIR = "/home/auti/biotemp/";
	static final Random r = new Random();

	static final int TIMES = 6;
	
	public static void main(String[] args) throws Exception {
		
		File f = new File(BASE_DIR + "biotemp-data.cdl");
		if (f.exists()) 
			f.delete();
		OutputStream out = new FileOutputStream(f);
		IOUtils.copy(new FileInputStream(BASE_DIR + "biotemp-data.cdl.head"), out);
		IOUtils.writeLines(UwCollectionUtils.list("mean = "), "\n", out);
		printValues(out, BASE_DIR + "biotemp-mean", mean);
		IOUtils.writeLines(UwCollectionUtils.list("variance = "), "\n", out);
		printValues(out, BASE_DIR + "biotemp-variance", variance);
		IOUtils.copy(new FileInputStream(BASE_DIR + "biotemp-data.cdl.tail"), out);
		IOUtils.closeQuietly(out);
	}

	static void printValues(OutputStream out, String source, Creator c) throws IOException {
		double[] vars = parseDoubles(source);
		double[] doubles = new double[TIMES * vars.length];
		int count = 0;
		for (int i = 0; i < TIMES; ++i)
			for (int j = 0; j < vars.length; ++j)
				doubles[count++] = (Math.abs(vars[j] + 999.0) < 0.01) ? vars[j] : c.create(vars[j]);
				
		System.err.println("Generated "+ count + " values...");
		IOUtils.writeLines(makeLines(doubles), "\n", out);
	}
	
	static final double[] parseDoubles(String filename) throws FileNotFoundException, IOException {
		String[] s = ((String) IOUtils.readLines(new FileInputStream(filename)).get(0)).split(",");
		double[] vars = new double[s.length];
		for (int i = 0; i < s.length; ++i)
			vars[i] = Double.parseDouble(s[i]);
		return vars;
	}


	static List<String> makeLines(double[] doubles) {
		List<String> lines = UwCollectionUtils.list();
		StringBuilder line = new StringBuilder();
		for (int i = 0; i < doubles.length; ++i) {
			line.append(String.valueOf(doubles[i]));
			line.append((i != doubles.length-1) ? "," : ";");
			if (line.length() >= 80) {
				lines.add(line.toString());
				line = new StringBuilder();
			}
		}
		if (line.length() > 0) 
			lines.add(line.toString());
		return lines;
	}
}