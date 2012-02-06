package org.uncertweb.wps.albatross.util;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author s_voss13
 *
 */
public class ProcessMonitorThread implements Callable<Void>{
	
	private Set<Pair<Process, Long>> processSet;
	private Long interruptTime;
	
	/**
	 * 
	 * @param processSet set of processes
	 * @param interruptTime time until the thread will be interrupted - in minutes
	 */
	public ProcessMonitorThread(Set<Pair<Process, Long>> processSet, Long interruptTime) {
		
		this.processSet = processSet;
		this.interruptTime = TimeUnit.MILLISECONDS.convert(interruptTime,TimeUnit.MINUTES);
	}

	@Override
	public Void call() throws Exception {
		
		synchronized (processSet) {
			
			Iterator <Pair<Process, Long>> iterator = processSet.iterator();
			
			while(iterator.hasNext()){
				
				Pair<Process, Long> currentPair = iterator.next();
				
				//destroy process and remove from set
				if((System.currentTimeMillis() - currentPair.getRight()) > this.interruptTime){
					
					currentPair.getLeft().destroy();
					iterator.remove();
					
					throw new RuntimeException("Process was destroyed by the system. Try again.");
				}
				
				//process terminated correct, remove it from set
				if(currentPair.getLeft().exitValue() == 0){
					
					iterator.remove();
					
				}
			}
			
		}
		return null;
	}

}
