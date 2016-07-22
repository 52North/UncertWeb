/**
 *
 */
package org.uncertweb.aqms.util;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * extends org.Rosuda.REngine.RServe.RConnection by evaluation Methods that forward R error messages
 * @author Jochen Bisier
 *
 */
public class ExtendedRConnection extends RConnection {

	private Logger LOG = Logger.getLogger(ExtendedRConnection.class);

	/**
	 * make a new local connection on default port (6311)
	 * @throws RserveException if connecting fails
	 */
	public ExtendedRConnection() throws RserveException {
		super();
	}

	/**
	 * make a new connection to specified host and given port. Make sure you check isConnected() to ensure the connection was successfully created.
	 * @param host host name/IP
	 * @param port TCP port
	 * @throws RserveException if connecting fails
	 */
	public ExtendedRConnection(String host, int port) throws RserveException {
		super(host, port);
	}

	/**
	 * make a new connection to specified host on default port (6311)
	 * @param host host name/IP
	 * @throws RserveException if connecting fails
	 */
	public ExtendedRConnection(String host) throws RserveException {
		super(host);
	}

	/**
	 * tries to evaluate a R command via Rserve and returns the result (forwards error messages from R)
	 * @param cmd the command to evaluate (assignments must use operator '<-')
	 * @return a R Expression representing the result of the command
	 * @throws RProcessException if the evaluation failed
	 */
	public REXP tryEval(String cmd) throws RProcessException {
		String tryCmd = "try("+cmd+")";
		LOG.debug("send R command: "+tryCmd);
		REXP result = null;
		try {
			result = super.eval(tryCmd);
			if (result.isString()&&result.hasAttribute("class")&&result.getAttribute("class").asString().equals("try-error")){
				throw new RProcessException(result.asString());
			}
		} catch (RserveException e) {
			throw new RProcessException(e.getMessage(),e);
		} catch (REXPMismatchException e) {
			throw new RProcessException(e.getMessage(),e);
		}
		return result;
	}

	/**tries to evaluate a R command via Rserve (forwards error messages from R)
	 * @param cmd the command to evaluate (assignments must use operator '<-')
	 * @throws RProcessException if the evaluation failed
	 */
	public void tryVoidEval(String cmd) throws RProcessException{
		tryVoidEval("catched",cmd);
	}

	/**tries to evaluate a R command via Rserve and stores the result in a R variable (forwards error messages from R)
	 * @param var the R variable to store the result
	 * @param cmd the command to evaluate (assignments must use operator '<-')
	 * @throws RProcessException if the evaluation failed
	 */
	public void tryVoidEval(String var, String cmd) throws RProcessException{
		String tryCmd = var+" <- try("+cmd+")";
		LOG.debug("send R command: "+tryCmd);
		try {
			super.voidEval(tryCmd);
			if (super.eval("class("+var+")").asString().equals("try-error")){
				throw new RProcessException(super.eval(var).asString());
			}
		} catch (REXPMismatchException e) {
			throw new RProcessException(e.getMessage(),e);
		} catch (RserveException e) {
			throw new RProcessException(e.getMessage(),e);
		}
	}

}
