/**
 *
 */
package de.ifgi.airbase.feeder.io.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import de.ifgi.airbase.feeder.data.EEAMeasurementType;

/**
 * {@link FileFilter} to filter all for specific {@link EEAMeasurementType}s.
 *
 * @author Christian Autermann
 *
 */
public class MeasurementTypeFileFilter implements FileFilter {

	private Collection<EEAMeasurementType> types;

	/**
	 * Creates a new {@code FileFilter} for the specified
	 * {@code EEAMeasurementType}s.
	 *
	 * @param types
	 *            the {@code EEAMeasurementType}s
	 */
	public MeasurementTypeFileFilter(Collection<EEAMeasurementType> types) {
		this.types = types;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory())
			return false;
        return this.types.contains(EEAMeasurementType.getValue(pathname
        		.getName().split("\\.")[0].substring(17)));
	}

}