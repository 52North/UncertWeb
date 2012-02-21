package org.uncertweb.viss.core.resource;

import java.util.List;

public class UncertaintyCollection {
	
	private List<UncertaintyReference> references;

	public List<UncertaintyReference> getReferences() {
		return references;
	}

	public void setReferences(List<UncertaintyReference> references) {
		this.references = references;
	}
}
