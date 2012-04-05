package org.uncertweb.wps.albatross.util;

import java.util.HashSet;
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
	
	private static ProcessMonitorThread instance = new ProcessMonitorThread();
	
	private ProcessMonitorThread(){
		
		processSet = new HashSet<Pair<Process,Long>>();
		interruptTime = 0l;
		
	}
	
	public static ProcessMonitorThread getInstance(){
		
		return instance;
	}
	
	public synchronized boolean addProcessSet(Set<Pair<Process, Long>> process){
		
		return this.processSet.addAll(process);
	}
	
	/**
	 * 
	 * @param processInterruptTime time until the thread will be interrupted - in minutes
	 */
	public synchronized void setInterruptTime(int processInterruptTime){
		
		this.interruptTime = TimeUnit.MILLISECONDS.convert(processInterruptTime,TimeUnit.MINUTES);
	}
	
	@Override
	public synchronized Void call() throws Exception {
		
		synchronized (processSet) {
			
			Iterator <Pair<Process, Long>> iterator = processSet.iterator();
			
			while(iterator.hasNext()){
				
				Pair<Process, Long> currentPair = iterator.next();
				
				//destroy process and remove from set
				if((System.currentTimeMillis() - currentPair.getRight()) > this.interruptTime){
					
					currentPair.getLeft().destroy();
					iterator.remove();
					
					//throw new RuntimeException("Process was destroyed by the system. Try again.");
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
