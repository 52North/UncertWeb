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
package org.uncertweb.sta.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Class to generate random Strings. Used to be a passwort generator
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 *
 */
public class RandomStringGenerator {

	/* @formatter off */

	/**
	 * All printable characters.
	 */
	private static final Character[] PRINTABLE = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
		'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
		's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
		'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
		'U', 'V', 'W', 'X', 'Y', 'Z', '!', '\"', '#', '$', '%', '&', '\'', '(',
		')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '?', '@', '[', '\\',
		']', '^', '_', '`', '{', '|', '}', '~'
	};

	/**
	 * Number characters.
	 */
	private static final Character[] NUMBERS = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
	};

	/**
	 * Lower case characters.
	 */
	private static final Character[] LOWER_CASE = {
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
	};

	/**
	 * Upper case characters.
	 */
	private static final Character[] UPPER_CASE = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
	};

	/**
	 * Printable, non alphanumeric characters.
	 */
	private static final Character[] NON_ALPHA_NUM = {
		'!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.',
		'/', ':', ';', '<', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|',
		'}', '~'
	};

	/* @formatter on */

	/**
	 * The algorithm used to initialize the {@link SecureRandom}.
	 */
	private static final String ALGORITHM = "SHA1PRNG";

	/**
	 * Single instance of this class.
	 */
	private static RandomStringGenerator singleton;

	/**
	 * The RNG.
	 */
	private final SecureRandom rand;

	/**
	 * Returns the lazy created singleton.
	 *
	 * @return the singleton.
	 */
	public static RandomStringGenerator getInstance() {
		if (singleton == null) {
			singleton = new RandomStringGenerator();
		}
		return singleton;
	}

	/**
	 * Private constructor. We are a singleton!
	 */
	private RandomStringGenerator() {
		try {
			rand = SecureRandom.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Shortcut for generating a string with upper case, lower case and digits
	 * of given length. Equivalent to
	 *
	 * <pre>
	 * generate(length, true, true, true, false);
	 * </pre>
	 *
	 * @param length the length of the generated string.
	 * @return the generated string.
	 */
	public String generate(int length) {
		return generate(length, true, true, true, false);
	}

	/**
	 * Generates a random string. Every component type has the even chance to be
	 * chosen and every character within the component has the equal opportunity
	 * to be chosen.
	 *
	 * @param length the length of the generated string
	 * @param digits if the string should contain digits
	 * @param uppercase if the string should contain upper case characters
	 * @param lowercase if the string should contain lower case characters
	 * @param nonalphanum if the string should contain non alphanumeric, but
	 *            printable, characters
	 * @return the generated string
	 */
	public String generate(int length, boolean digits, boolean uppercase,
			boolean lowercase, boolean nonalphanum) {
		ArrayList<Character[]> sources = new ArrayList<Character[]>(5);
		if (digits) {
			sources.add(NUMBERS);
		}
		if (uppercase) {
			sources.add(UPPER_CASE);
		}
		if (lowercase) {
			sources.add(LOWER_CASE);
		}
		if (nonalphanum) {
			sources.add(NON_ALPHA_NUM);
		}
		if (sources.isEmpty()) {
			sources.add(PRINTABLE);
		}
		Character[][] src = sources.toArray(new Character[0][]);
		char[] password = new char[length];
		for (int i = 0; i < length; i++) {
			int s = rand.nextInt(src.length);
			password[i] = src[s][rand.nextInt(src[s].length)];
		}
		return new String(password);
	}

}
