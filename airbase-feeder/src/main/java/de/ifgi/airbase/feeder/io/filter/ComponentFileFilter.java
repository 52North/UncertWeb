package de.ifgi.airbase.feeder.io.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashSet;

public class ComponentFileFilter implements FileFilter{


	private HashSet<Integer> components = null;

	public ComponentFileFilter(Collection<Integer> comps) {
		this.components = new HashSet<Integer>(comps);
	}

	@Override
	public boolean accept(File pathname) {
		return pathname.isFile() && this.components.contains(Integer.valueOf(pathname.getName().split("\\.")[0].substring(7, 12)));
	}

}
