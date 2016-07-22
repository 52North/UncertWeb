package org.uncertweb.api.gml;

import java.net.URI;

/**
 * class represents an GML identifier (ATTENTION: this is NOT the gml:id attribute that should only be used for referencing within XML documents)
 * 
 * @author staschc
 *
 */
public class Identifier {

	/**identifier*/
	private String identifier;
	
	/**codeSpace of the identifier*/
	private URI codeSpace;
	
	/**
	 * constructor
	 * 
	 * @param codeSpace
	 * 			reference to codespace of the identifier
	 * @param identifier
	 * 			identifier
	 */
	public Identifier(URI codeSpace,String identifier){
		setCodeSpace(codeSpace);
		setIdentifier(identifier);
	}
	
	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	/**
	 * @return the codeSpace
	 */
	public URI getCodeSpace() {
		return codeSpace;
	}
	/**
	 * @param codeSpace the codeSpace to set
	 */
	public void setCodeSpace(URI codeSpace) {
		this.codeSpace = codeSpace;
	}
	
	/**
	 * returns the codespace concatenated with "/" and the identifier
	 */
	public String toIdentifierString(){
		return this.codeSpace.toString()+"/"+this.identifier;
	}
}
