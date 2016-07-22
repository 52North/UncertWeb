package de.ifgi.airbase.feeder.io.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * A {@link FileFilter} that filters by extension.
 *
 * @author Christian Autermann
 *
 */
public class FileExtensionFilter implements FileFilter {
	private String extension = ".";

	/**
	 * Creates a new {@code FileExtensionFilter}.
	 *
	 * @param extension
	 *            the extension
	 */
	public FileExtensionFilter(String extension) {
		this.extension += extension;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(File pathname) {
		return pathname.isFile() && pathname.getName().endsWith(this.extension);
	}
}
