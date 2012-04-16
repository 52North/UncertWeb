package org.uncertweb.wps.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.LocalAlgorithmRepository;

public class FTPUtil {

	private static Logger logger = Logger.getLogger(FTPUtil.class);

	private String host;
	private int port;
	private String usr;
	private String pwd;
	private String incomingFolder;
	private String resultsFolder;

	static boolean abort = false;
	static boolean finished = false;

	public FTPUtil() {
		Property[] propertyArray = WPSConfig.getInstance()
				.getPropertiesForRepositoryClass(
						LocalAlgorithmRepository.class.getCanonicalName());
		for (Property property : propertyArray) {
			// check the name and active state
			if (property.getName().equalsIgnoreCase("ftpHost")
					&& property.getActive()) {
				host = property.getStringValue();
			} else if (property.getName().equalsIgnoreCase("ftpPort")
					&& property.getActive()) {
				port = Integer.parseInt(property.getStringValue());
			} else if (property.getName().equalsIgnoreCase("ftpUser")
					&& property.getActive()) {
				usr = property.getStringValue();
			} else if (property.getName().equalsIgnoreCase("ftpPwd")
					&& property.getActive()) {
				pwd = property.getStringValue();
			}else if (property.getName().equalsIgnoreCase("incomingFolder")
					&& property.getActive()) {
				incomingFolder = property.getStringValue();
			}else if (property.getName().equalsIgnoreCase("resultsFolder")
					&& property.getActive()) {
				resultsFolder = property.getStringValue();
			}
		}
	}

	/**
	 * list files of results directory of user ftp connection defined in wps config
	 */
	public String[] list() throws IOException {
		FTPClient ftpClient = new FTPClient();
		String[] filenameList;

		try {
			ftpClient.connect(host, port);
			ftpClient.login(usr, pwd);
			filenameList = ftpClient.listNames("/" + resultsFolder);
			ftpClient.logout();
		} finally {
			ftpClient.disconnect();
		}

		return filenameList;
	}

	/**
	 * 
	 * @return
	 */
	public boolean download(String localResultFile, String remoteSourceFile)
			throws IOException {
		FTPClient ftpClient = new FTPClient();
		FileOutputStream fos = null;
		boolean resultOk = true;

		try {
			ftpClient.connect(host, port);
			logger.debug(ftpClient.getReplyString());
			
			resultOk &= ftpClient.login(usr, pwd);
			logger.debug(ftpClient.getReplyString());
			
			fos = new FileOutputStream(localResultFile);
			resultOk &= ftpClient.retrieveFile(resultsFolder + "/" + remoteSourceFile, fos);
			logger.debug(ftpClient.getReplyString());
			
			resultOk &= ftpClient.logout();
			logger.debug(ftpClient.getReplyString());
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {/* nothing to do */
			}
			ftpClient.disconnect();
		}

		return resultOk;
	}

	/**
	 * 
	 * @return
	 */
	public boolean upload(String localSourceFile, String remoteResultFile)
			throws IOException {
		FTPClient ftpClient = new FTPClient();
		FileInputStream fis = null;
		boolean resultOk = true;

		try {
			ftpClient.connect(host, port);
			logger.debug(ftpClient.getReplyString());

			resultOk &= ftpClient.login(usr, pwd);
			logger.debug(ftpClient.getReplyString());

			fis = new FileInputStream(localSourceFile);
			resultOk &= ftpClient.storeFile(incomingFolder + "/" + remoteResultFile, fis);
			logger.debug(ftpClient.getReplyString());

			resultOk &= ftpClient.logout();
			logger.debug(ftpClient.getReplyString());

		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {/* nothing to do */
			}
			ftpClient.disconnect();
		}

		return resultOk;
	}
}
