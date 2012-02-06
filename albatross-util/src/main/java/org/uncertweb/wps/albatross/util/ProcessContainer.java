package org.uncertweb.wps.albatross.util;

/**
 * @author s_voss13
 *
 */
public class ProcessContainer {
	
	private Process process;
	private boolean isTerminatedBySystem = false;
	
	public ProcessContainer(Process process) {

		this.process = process;
	}

	public boolean isTerminatedBySystem() {
		return isTerminatedBySystem;
	}
	public Process getProcess() {
		return process;
	}
}
