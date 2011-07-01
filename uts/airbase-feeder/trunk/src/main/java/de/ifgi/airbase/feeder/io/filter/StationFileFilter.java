package de.ifgi.airbase.feeder.io.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

public class StationFileFilter implements FileFilter {
	private Set<String> stations;

	public StationFileFilter(Set<String> stations) {
		this.stations = stations;
	}

	@Override
	public boolean accept(File pathname) {
		if (pathname.isFile()) {
			String station = pathname.getName().substring(0, 7);
			for (String s : this.stations) {
				if (station.matches(s))
					return true;
			}
		} 
		return false;
	}

}
