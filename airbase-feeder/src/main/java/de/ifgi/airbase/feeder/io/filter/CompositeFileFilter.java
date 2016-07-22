package de.ifgi.airbase.feeder.io.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.LinkedList;

public class CompositeFileFilter implements FileFilter {
	private Collection<FileFilter> ff = new LinkedList<FileFilter>();

	public CompositeFileFilter(FileFilter... ff) {
		if (ff != null) {
			for (FileFilter f : ff) {
				addFilter(f);
			}
		}
	}


	public void addFilter(FileFilter f) {
		this.ff.add(f);
	}

	@Override
	public boolean accept(File pathname) {
		for (FileFilter f : this.ff) {
			if (!f.accept(pathname)) {
				return false;
			}
		}
		return true;
	}
}
