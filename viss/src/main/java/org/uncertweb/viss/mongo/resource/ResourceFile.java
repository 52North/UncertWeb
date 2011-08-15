package org.uncertweb.viss.mongo.resource;

import java.io.File;

import com.google.code.morphia.annotations.Embedded;

// can't use href as key in mongo db as points are not allowed....
@Embedded
public class ResourceFile {
	private File file;
	private String href;
	
	public ResourceFile() {}
	
	public ResourceFile(File file, String href) {
		this.file = file;
		this.href = href;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
}