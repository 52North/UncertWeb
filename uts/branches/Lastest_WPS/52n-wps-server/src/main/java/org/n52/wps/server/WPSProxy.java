/**
* 52°North WPS OpenLayers Client
*
* for using WPS-based processes in browser-based applications.
* Copyright (C) 2010
* Janne Kovanen, Finnish Geodetic Institute
* Raphael Rupprecht, Institute for Geoinformatics
* 52North GmbH
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
* 
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package org.n52.wps.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
* This is a blind proxy that we use to get around browser
* restrictions, whic prevent the JavaScript from loading pages from
* external domains. The proxy supports only http and https, 
* but it can load any content type. 
* 
* It supports request using the GET and POST methods.
* 
* @author Janne Kovanen, Finnish Geodetic Institute (FGI)
*/
public class WPSProxy extends HttpServlet {
	// Universal version identifier for a Serializable class. Should be used 
	// here, because HttpServlet implements the java.io.Serializable
	private static final long serialVersionUID = -8572304465265790593L;
	private static final Logger LOGGER = Logger.getLogger(WPSProxy.class);
	private static final boolean CHECK_ALLOWED_HOSTS = true;
	public static String PROPERTY_NAME_HOST_NAMES = "allowedHosts";
	public static String PROPERTY_NAME_HOST_PORTS = "allowedPorts";
//	private String[] allowedHosts;
//	private int[] allowedPorts;
	
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		BasicConfigurator.configure(); // Needed for the log4j to function!
//		LOGGER.info("Web Processing Service Proxy initializing...");
//		String hosts = ProxyConfiguration.getInstance().getProperty(
//				PROPERTY_NAME_HOST_NAMES);
//		this.allowedHosts = hosts.split(",");
//		String[] ports = ProxyConfiguration.getInstance().getProperty(
//				PROPERTY_NAME_HOST_PORTS).split(",");
//		this.allowedPorts = new int[ports.length];
//		for(int i=0; i<ports.length; i++) {
//			this.allowedPorts[i] = Integer.parseInt(ports[i]);
//		}
		LOGGER.info("Web Processing Service Proxy up and running!");
	}

	protected void doGet(final HttpServletRequest request, 
			final HttpServletResponse response) 
			throws ServletException, IOException {
		// First filtering unsupported requests!
//		String url = URLDecoder.decode(request.getParameter("url"), "UTF-8");
		LOGGER.debug("GET request arrived to the proxy!");
		
//		if(!isAcceptedTransferProtocol(url)) {
//			// The server does not support the functionality required to fulfill 
//			// the request. This is the appropriate response when the server 
//			// does not recognize the request method and is not capable of 
//			// supporting it for any resource.
//			response.sendError(501);
//			return;
//		}
//		//Removing the http:// or https:// and the rest!
//		String host = url.replace('\\', '/').split("/")[2];
//		// Compare hosts and ports.
//		if(CHECK_ALLOWED_HOSTS) {
//			if(!isAcceptedHost(host)) {
//				// The server, while acting as a gateway or proxy, received an 
//				// invalid response from the upstream server it accessed in 
//				// attempting to fulfill the request.
//				LOGGER.debug("Invalid host: " + host);
//				response.sendError(502);
//				return;
//			}
//		}
		
		// Redirect the request! NO actual redirecting 
		// (HttpServletRequest.sendRedirect()) 
		// or forwarding (RequestDispatcher.forward())!!!
		try {
//			// Create an URL-object.
//	        final URL hostUrl = new URL(url);//protocol, host, hostPort, "");
//	        // Open a connection to the URL.
//	        final HttpURLConnection conn = (HttpURLConnection)hostUrl.openConnection();
//	        // We need to use GET!
//	        conn.setRequestMethod("GET");
//	        // Set output capability on the URLConnection. Has to be false for GET!
//	        conn.setDoOutput(false);
//	        // Set input capability on the URLConnection. 
//	        conn.setDoInput(true);
//	        // Get the response from the connection!
//	        final BufferedReader hostReader = new BufferedReader(
//	        		new InputStreamReader(
//	        				conn.getInputStream()));
			
			//TODO: dynamically create WSDL. Wait for Richard's solution.
			
			URL wsdlFileURL = new URL(
					"http://localhost:8080/wps4/examples/wps0.wsdl");
			
	        final BufferedReader hostReader = new BufferedReader(new InputStreamReader(wsdlFileURL.openStream()));
						
	        // Create a writer back to the client.
	        final OutputStreamWriter clientWriter = new OutputStreamWriter(
	        		response.getOutputStream());
	        String line="";
	        while ((line = hostReader.readLine()) != null) {
	        	clientWriter.write(line);
	        }
	        clientWriter.flush();
	        clientWriter.close();
	        hostReader.close();
	    } catch (final Exception e) {
	    	LOGGER.error(e.getMessage());
	    	response.sendError(500);
	    }
	}
	
	protected void doPost(final HttpServletRequest request, 
			final HttpServletResponse response) 
			throws ServletException, IOException {
		LOGGER.debug("POST request arrived to the proxy!");
		
		// First filtering unsupported requests!
		final String url = request.getParameter("url");
		if(!isAcceptedTransferProtocol(url)) {
			// The server does not support the functionality required to fulfill 
			// the request. This is the appropriate response when the server 
			// does not recognize the request method and is not capable of 
			// supporting it for any resource.
			response.sendError(501);
			return;
		}
		//Removing the http:// or https:// and the rest!
		String host = url.replace('\\', '/').split("/")[2];
		// Compare hosts and ports!
		if(CHECK_ALLOWED_HOSTS) {
			if(!isAcceptedHost(host)) {
				// The server, while acting as a gateway or proxy, received an 
				// invalid response from the upstream server it accessed in 
				// attempting to fulfill the request.
				LOGGER.debug("Invalid host: " + host);
				response.sendError(502);
				return;
			}
		}

		// Redirect the request! NO actual redirecting 
		// (HttpServletRequest.sendRedirect()) 
		// or forwarding (RequestDispatcher.forward())!!!
		try {
			// Create an URL-object.
	        final URL hostUrl = new URL(url);
	        // Open a connection to the URL.
	        final HttpURLConnection conn = (HttpURLConnection)hostUrl.openConnection();
	        // We need to use POST!
	        conn.setRequestMethod("POST");
	        // Set output capability on the URLConnection. 
	        conn.setDoOutput(true);
	        // SOAP support
	        if(request.getHeader("SOAPAction") != null) { 
	        	conn.setRequestProperty("SOAPAction", 
	        			request.getHeader("SOAPAction"));
	        }
	        final OutputStreamWriter hostWriter = new OutputStreamWriter(
	        		conn.getOutputStream());
	        // Send the data.
	        final BufferedReader clientReader = new BufferedReader(
                    new InputStreamReader(
                    		request.getInputStream()));
	        String line;
	        while ((line = clientReader.readLine()) != null) {
	    	    if(!line.contains("&amp;")){
	    	    	line = line.replace("&", "&amp;");
	    	    }
	    	    hostWriter.write(line);
	        }
	        // Flush the output writer to the host.
	        hostWriter.flush();
	        hostWriter.close();
	        // Close the input stream from the client.
	        clientReader.close();
	        // Get the response from the connection!
	        final BufferedReader hostReader = new BufferedReader(
	        		new InputStreamReader(
	        				conn.getInputStream()));
	        // Create a writer back to the client.
	        final OutputStreamWriter clientWriter = new OutputStreamWriter(
	        		response.getOutputStream());
	        while ((line = hostReader.readLine()) != null) {
	        	System.out.println(line);
	        	clientWriter.write(line);
	        }
	        clientWriter.flush();
	        clientWriter.close();
	        hostReader.close();
	        response.setStatus(HttpServletResponse.SC_OK);
	    } catch (final Exception e) {
	    	LOGGER.error(e.getMessage());
	    	response.sendError(500);
	    }
	}

	private boolean isAcceptedHost(String host) {
		// Compare hosts and ports!
//		boolean hostAccepted = false;
//		for(int i=0; i<this.allowedHosts.length; i++) {
//			if(host.split(":")[0].equals(this.allowedHosts[i])) {
//				if(host.split(":").length>1) {
//					final int port = Integer.parseInt(host.split(":")[1]);
//					for(int j=0; j<this.allowedPorts.length; j++) {
//						if(port == this.allowedPorts[j]) {
//							// hostPort = port;
//							host = host.split(":")[0];
//							hostAccepted = true;
//						}
//					}
//				} else {
//					// Port is not given (is 80).
//					hostAccepted = true;
//				}
//			}
//		}
		return true;
	}
	
	private boolean isAcceptedTransferProtocol(String url) {
		if((url.length() < 8) || 
				(!url.substring(0,7).equals("http://") && 
				!url.substring(0,8).equals("https://")))
			return false;
		return true;
	}
	
	protected void service(final HttpServletRequest req, final HttpServletResponse res) 
			throws ServletException, IOException {
		super.service(req, res);
	}
	
	public void destroy() {
		super.destroy();
	}
}
