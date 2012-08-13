package org.uncertweb.ems.data.profiles;

public abstract class AbstractActivity {

	protected String name;
	
	public AbstractActivity(String description){	
		this.name = description;
	}
	
	public String toString(){
		return name;
	}
}
