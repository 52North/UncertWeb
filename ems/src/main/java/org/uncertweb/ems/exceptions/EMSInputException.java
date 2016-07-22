package org.uncertweb.ems.exceptions;

/**
 * Class representing exception for the inputs in the EMS
 * @author LydiaGerharz
 *
 */
public class EMSInputException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	
	/**
	 * constructor
	 * 
	 * @param cause
	 * 			cause for this exception
	 */
	public EMSInputException(Throwable cause){
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
	public EMSInputException(String message, Throwable cause){
		super(message, cause);
	}
	
	/**
	 * constructor
	 * 
	 * @param message
	 * 			exception message
	 */
	public EMSInputException(String message){
		super(message);
	}

}
