package org.uncertweb.ems.extension.profiles;

import org.uncertweb.ems.util.ActivityMapping;

/**
 * Stores information about the visited microenvironment
 * @author LydiaGerharz
 *
 */
public class Microenvironment extends AbstractActivity{

	private static String[] meTypes = new String[]{"car","bus","train", "home", "otherindoor",
		"work", "outdoor", "restaurant", "disco", "pub"};

	public Microenvironment(String description) {
		super(description);
	}

}
