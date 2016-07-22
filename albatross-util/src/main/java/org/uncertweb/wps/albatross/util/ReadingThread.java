package org.uncertweb.wps.albatross.util;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/**
 * 
 * @author s_voss13
 *
 */
public class ReadingThread implements Callable<Void> {
	
    private final InputStream inputStream;
    private final String name;

    public ReadingThread(InputStream inputStream, String name) {
      this.inputStream = inputStream;
      this.name = name;
    }

    @Override
	public Void call() throws Exception {
      try {
        BufferedReader in = new BufferedReader(
            new InputStreamReader(inputStream));
        for (String s = in.readLine(); s != null; s = in.readLine()) {
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
	return null;
    }
  }