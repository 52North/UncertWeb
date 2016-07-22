package de.ifgi.airbase.feeder.io.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * {@link FileFilter} which filters by a specified prefix.
 *
 * @author Christian Autermann
 *
 */
public class PrefixFileFilter implements FileFilter {
	private String prefix;

	/**
	 * Creates a new {@link PrefixFileFilter}.
	 *
	 * @param prefix
	 *            the prefix
	 */
	public PrefixFileFilter(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(File pathname) {
		return pathname.isFile() && pathname.getName().startsWith(this.prefix);
	}

}