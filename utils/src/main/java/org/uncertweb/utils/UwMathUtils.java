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
package org.uncertweb.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class UwMathUtils {
	/** RNG. */
	private static final Random random = new Random();
	
	
	public static double roundDouble(double number, int digits){
		BigDecimal bd = new BigDecimal(number).setScale(digits, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}

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

	/**
	 * calculates the greatest common divisor of {@code u} and {@code v}.
	 */
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
