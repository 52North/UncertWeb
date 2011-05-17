package org.uncertweb.api.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * common interface for encoding data items in UncertWeb
 * 
 * @author staschc
 *
 */
public interface IUncertWebEncoder {

    public String encode(Object element) throws IOException;

    public void encode(Object element, File file) throws IOException;

    public void encode(Object element, OutputStream stream) throws IOException;

    public void encode(Object element, Writer writer) throws IOException;

}
