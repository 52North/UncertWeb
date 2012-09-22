package org.uncertweb.ems.exceptions;

/**
 * Class representing exception during the overlay or indoor model processes in the EMS
 * @author LydiaGerharz
 *
 */
public class EMSProcessingException extends RuntimeException{
private static final long serialVersionUID = 1L;
	
	/**
	 * constructor
	 * 
	 * @param cause
	 * 			cause for this exception
	 */
	public EMSProcessingException(Throwable cause){
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
	public EMSProcessingException(String message, Throwable cause){
		super(message, cause);
	}
	
	/**
	 * constructor
	 * 
	 * @param message
	 * 			exception message
	 */
	public EMSProcessingException(String message){
		super(message);
	}
}
