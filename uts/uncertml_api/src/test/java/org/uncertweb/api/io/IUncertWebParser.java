package org.uncertweb.api.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * common interface for parsing of data items in UncertWeb project
 * 
 * @author staschc
 *
 */
public interface IUncertWebParser {

    public Object parse(String uncertml) throws IOException;

    public Object parse(InputStream stream) throws IOException;

    public Object parse(File file) throws IOException;

    public Object parse(Reader reader) throws IOException;

}
