package org.uncertweb.aqms.util;

/**
 * exception thrown by RProcess
 * @author Katharina Henneboehl
 *
 */
public class RProcessException extends Exception {

	private static final long serialVersionUID = 1L;

	public RProcessException() {
		super();
	}
	
	public RProcessException(String message) {
		super(message);
	}
	
	public RProcessException(Exception cause) {
		super(cause);
	}
	
	public RProcessException(String message, Exception cause ) {
		super(message, cause);
	}

}

