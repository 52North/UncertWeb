package org.uncertweb.api.om.exceptions;


/**
 * Class representing a parsinjg exception when parsing O&M.
 * 
 * @author staschc
 *
 */
public class OMParsingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * constructor
	 * 
	 * @param cause
	 * 			cause for this exception
	 */
	public OMParsingException(Throwable cause){
		super(cause);
	}
	
	/**
	 * constructor
	 * 
	 * @param message
	 * 			message for this exception
	 * @param cause
	 * 			cause for this exception
	 */
	public OMParsingException(String message, Throwable cause){
		super(message, cause);
	}
	
	/**
	 * constructor
	 * 
	 * @param message
	 * 			exception message
	 */
	public OMParsingException(String message){
		super(message);
	}

}
