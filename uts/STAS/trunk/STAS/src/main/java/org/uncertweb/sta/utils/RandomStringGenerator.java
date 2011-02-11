package org.uncertweb.sta.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

public class RandomStringGenerator {

	private static final Character[] PRINTABLE = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
		'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
		's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
		'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
		'U', 'V', 'W', 'X', 'Y', 'Z', '!', '\"', '#', '$', '%', '&', '\'', '(',
		')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '?', '@', '[', '\\',
		']', '^', '_', '`', '{', '|', '}', '~'
	};
	
	private static final Character[] NUMBERS = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
	};
	
	private static final Character[] LOWER_CASE = {
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
	};
	
	private static final Character[] UPPER_CASE = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
	};
	
	private static final Character[] NON_ALPHA_NUM = {
		'!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.',
		'/', ':', ';', '<', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|',
		'}', '~'
	};

	private static final String ALGORITHM = "SHA1PRNG";
	private static RandomStringGenerator singleton;
	private final SecureRandom rand;

	public static RandomStringGenerator getInstance() {
		if (singleton == null) {
			singleton = new RandomStringGenerator();
		}
		return singleton;
	}
	
	private RandomStringGenerator() {
		try {
			rand = SecureRandom.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public String generate(int length) {
		return generate(length, true, true, true, false);
	}
	
	public String generate(int length, boolean digits, boolean uppercase,
			boolean lowercase, boolean nonalphanum) {
		ArrayList<Character[]> sources = new ArrayList<Character[]>();
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
	
	public static void main(String[] args) {
		for (int i = 0; i < 10; i++)
			System.out.println(RandomStringGenerator.getInstance().generate(20,
					true, true, true, false));
	}

}
