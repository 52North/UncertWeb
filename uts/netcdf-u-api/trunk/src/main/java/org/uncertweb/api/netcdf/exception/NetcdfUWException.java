package org.uncertweb.api.netcdf.exception;

public class NetcdfUWException extends Exception {
    
    public NetcdfUWException() {
        super();
    }
    
    public NetcdfUWException(String message, Throwable t) {
        super(message, t);
    }
    
    public NetcdfUWException(String message) {
        super(message);
    }
    
    public NetcdfUWException(Throwable t) {
        super(t);
    }

}
