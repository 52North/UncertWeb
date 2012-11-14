package org.uncertweb.wps.albatross.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

/**
 * @author s_voss13
 *
 */
public class WorkspaceCleanerThread implements Callable<Void> {
	
	private Set<Pair<File, Long>> fileSet;
	private Long interruptTime;
	
	private static WorkspaceCleanerThread instance = new WorkspaceCleanerThread();
	
	private WorkspaceCleanerThread(){
		
		this.fileSet = new HashSet<Pair<File,Long>>();
	}
	
	public static WorkspaceCleanerThread getInstance(){
		
		return instance;
	}
	
	public synchronized boolean addFileSet(Set<Pair<File,Long>> files){
		
		return this.fileSet.addAll(files);
	}
	
	/**
	 * 
	 * @param processInterruptTime time until the files will be removed - in minutes
	 */
	public synchronized void setInterruptTime(int processInterruptTime){
		
		this.interruptTime = TimeUnit.MILLISECONDS.convert(processInterruptTime,TimeUnit.MINUTES);
	}

	@Override
	public synchronized Void call() throws Exception {
		
		synchronized (fileSet) {
			
			Iterator<Pair<File,Long>> iterator = fileSet.iterator();
			
			while(iterator.hasNext()){
				
				Pair<File,Long> currentPair = iterator.next();
				
				//its time to remove the files
				if((System.currentTimeMillis() - currentPair.getRight()) > this.interruptTime){
					
					try {
						FileUtils.deleteDirectory(currentPair.getLeft());
						iterator.remove();
					} catch (IOException e) {
						throw new Exception("Error while deleting directory:" +currentPair.getLeft());
					}
				}
				
				

			}
			
		}
		return null;	
	}
}
