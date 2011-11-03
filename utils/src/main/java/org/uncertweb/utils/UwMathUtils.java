package org.uncertweb.utils;

import java.util.Random;

public class UwMathUtils {
	/** RNG. */
	private static final Random random = new Random();

	/**
	 * Generates a random {@code double} between {@code min} and {@code max}.
	 * 
	 * @param min the minimum {@code double} (inclusive)
	 * @param max the maximum {@code double} (inclusive)
	 * @return a random {@code double} between {@code min} and {@code max}.
	 */
	public static double randomBetween(double min, double max) {
		return Math.min(min, max) + random.nextDouble() * Math.abs(max - min);
	}


	
	
	public static long gcd(long u, long v) {
		if (u == 0 || v == 0) {
			return u | v;
		}
		long k;
		for (k = 0; ((u | v) & 1) == 0; ++k) {
			u >>= 1;
			v >>= 1;
		}
		while ((u & 1) == 0) {
			u >>= 1;
		}
		do {
			while ((v & 1) == 0) {
				v >>= 1;
			}
			if (u < v) {
				v -= u;
			} else {
				long d = u - v;
				u = v;
				v = d;
			}
			v >>= 1;
		} while (v != 0);
		return u << k;
	}

}
