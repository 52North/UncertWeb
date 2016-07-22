/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ifgi.airbase.feeder.io.sos.http;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class RequestFailedException extends Exception {

    public RequestFailedException(String message) {
        super(message);
    }

}
