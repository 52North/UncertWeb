package org.uncertweb.ems.extension.profiles;

public abstract class AbstractActivity {

	protected String name;
	
	public AbstractActivity(String description){	
		this.name = description;
	}
	
	public String toString(){
		return name;
	}
}
