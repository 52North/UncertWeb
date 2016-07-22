package org.uncertweb.austalwps.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.n52.wps.server.observerpattern.ISubject;

/**
 * @author Michael C. Daconta, JavaWorld.com, 12/29/00
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 */
public class StreamGobbler extends Thread{
	
	private static Logger LOGGER = Logger.getLogger(StreamGobbler.class);
	InputStream is;
    String type;
    OutputStream os;
    ISubject subject;
	
    public StreamGobbler(InputStream is, String type)
    {
        this(is, type, null);
    }
    public StreamGobbler(InputStream is, String type, OutputStream redirect)
    {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }
    
    public void run()
    {
        try
        {
            PrintWriter pw = null;
            if (os != null)
                pw = new PrintWriter(os);
                
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
                if (pw != null)
                    pw.println(line);
                if(subject != null){
                	if(line.contains("Fertig berechnet")||line.contains("Progress")){
                		subject.update(line);
                	}
                }
                System.out.println(type + ">" + line);    
            }
            if (pw != null){
                pw.flush();
            	pw.close();
            }
        } catch (IOException ioe)
            {
            ioe.printStackTrace();  
            }
    }    
    
    public void setSubject(ISubject subject) {
		this.subject = subject;
	}

	
}
