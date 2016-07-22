package org.uncertweb.api.om.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.uncertweb.api.om.exceptions.OMEncodingException;

/**
 * contains helper methods for writing and reading strings from/to streams, files, etc.
 *
 * @author staschc
 *
 */
public class IOUtil {

	public static boolean writeString2File(String s, File f) throws OMEncodingException{
		FileWriter fw;
		try {
			fw = new FileWriter(f);
		BufferedWriter writer = new BufferedWriter(fw);
        writer.write(s);
        writer.flush();
		} catch (IOException e) {
			throw new OMEncodingException(e);
		}
        return true;
	}

	public static boolean writeString2OutputStream(String s, OutputStream os) throws OMEncodingException{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        try {
			bw.write(s);
			bw.flush();
        } catch (IOException e) {
			throw new OMEncodingException(e);
		}
        return true;
	}
}
