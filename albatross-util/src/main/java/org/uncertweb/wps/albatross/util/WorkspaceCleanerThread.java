package org.uncertweb.wps.albatross.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

/**
 * @author s_voss13
 *
 */
public class WorkspaceCleanerThread implements Callable<Void> {
	
	private Set<File> filesSet;
	
	public WorkspaceCleanerThread(Set<File> filesSet) {

		this.filesSet = filesSet;
	}

	@Override
	public Void call() throws Exception {
		
		synchronized (filesSet) {
			
			Iterator<File> iterator = filesSet.iterator();
			
			while(iterator.hasNext()){
				
				File currentFile = iterator.next();
				
				try {
					FileUtils.deleteDirectory(currentFile);
					iterator.remove();
				} catch (IOException e) {
					e.printStackTrace();
				}

				
			}
			
		}
		return null;	
	}
}
