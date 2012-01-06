package org.uncertweb.wps.util;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 
 * @author s_voss13
 *
 */
public class ReadingThread extends Thread {
    private final InputStream inputStream;
    private final String name;

    public ReadingThread(InputStream inputStream, String name) {
      this.inputStream = inputStream;
      this.name = name;
    }

    public void run() {
      try {
        BufferedReader in = new BufferedReader(
            new InputStreamReader(inputStream));
        for (String s = in.readLine(); s != null; s = in.readLine()) {
         System.out.println(name + ": " + s);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }