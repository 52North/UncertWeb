package org.uncertweb.api.om.exceptions;

/**
 * class represents an encoding exception when encoding O&M.
 * 
 * @author staschc
 *
 */
public class OMEncodingException extends Exception{

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
	public OMEncodingException(Throwable cause){
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
	public OMEncodingException(String message, Throwable cause){
		super(message, cause);
	}
	
	/**
	 * constructor
	 * 
	 * @param message
	 * 			exception message
	 */
	public OMEncodingException(String message){
		super(message);
	}

}
