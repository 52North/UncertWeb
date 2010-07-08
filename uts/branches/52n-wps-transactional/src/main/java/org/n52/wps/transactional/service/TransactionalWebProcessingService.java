package org.n52.wps.transactional.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.handler.TransactionalExceptionHandler;
import org.n52.wps.transactional.handler.TransactionalRequestHandler;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.ITransactionalRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.n52.wps.transactional.response.TransactionalResponse;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TransactionalWebProcessingService extends HttpServlet{
	private static Logger LOGGER = Logger.getLogger(TransactionalWebProcessingService.class);
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		LOGGER.info("Inbound HTTP-POST DeployProcess Request. " + new Date());
		TransactionalResponse response = null;
		try {
			InputStream is = req.getInputStream();
			//System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
			DocumentBuilderFactory documentBuiloderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder= documentBuiloderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(is);

			String requestType = document.getFirstChild().getNodeName();
			ITransactionalRequest request = null;
			if (requestType == null) {
				throw new ExceptionReport("Request not valid",
						ExceptionReport.OPERATION_NOT_SUPPORTED);
			} else if (requestType.equals("DeployProcessRequest")) {
				request = new DeployProcessRequest(document);
			} else if (requestType.equals("UnDeployProcessRequest")) {
				request = new UndeployProcessRequest(document);
			} else {
				throw new ExceptionReport("Request type unknown ("
						+ requestType
						+ ") Must be DeployProcess or UnDeployProcess",
						ExceptionReport.OPERATION_NOT_SUPPORTED);
			}

			LOGGER.info("Request type: " + requestType);
			response = TransactionalRequestHandler.handle(request);
			if (response == null) {
				throw new ExceptionReport("bug! An error has occurred while "
						+ "processing the request: " + requestType,
						ExceptionReport.NO_APPLICABLE_CODE);
			} else {
				String rootTag = (request instanceof DeployProcessRequest) ? "DeployProcessResponse"
						: "UnDeployProcessResponse";
				PrintWriter writer = res.getWriter();
				writer.write("<" + rootTag + ">");
				writer.write("<Result success=\"true\">");
				writer.write(response.getMessage());
				writer.write("</Result>");
				writer.write("</" + rootTag + ">");
				writer.flush();
				writer.close();
				LOGGER.info("Request handled successfully: " + requestType);
			}
		} catch (ParserConfigurationException e) {
			TransactionalExceptionHandler.handleException(res.getWriter(),
					new ExceptionReport("An error has occurred while "
							+ "building the XML parser",
							ExceptionReport.NO_APPLICABLE_CODE));
		} catch (SAXException e) {
			TransactionalExceptionHandler.handleException(res.getWriter(),
					new ExceptionReport("An error has occurred while "
							+ "parsing the XML request",
							ExceptionReport.NO_APPLICABLE_CODE));
		} catch (ExceptionReport exception) {
			TransactionalExceptionHandler.handleException(res.getWriter(),
					exception);
		} catch (Throwable t) {
			TransactionalExceptionHandler.handleException(res.getWriter(),
					new ExceptionReport("Unexpected error",
							ExceptionReport.NO_APPLICABLE_CODE));
		}
	}
}
