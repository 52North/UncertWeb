package org.uncertweb.api.netcdf;

import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.api.netcdf.NetcdfUWFile;

import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import junit.framework.TestCase;

/**
 * TestCase for testing NetcdfUWFile wrapper; reads uncertain NetCDF example
 * files from folder src/test/resources and tests the methods provided
 * 
 * @author staschc, angelini
 */
public class NetcdfUWFileTest extends TestCase {

    private final Logger logger = LoggerFactory.getLogger(NetcdfUWFileTest.class);

    private String pathToExamples = "src/test/resources/";

    public void setUp() {
    }

    /**
     * reads the example file biotemperature_randomSamples.nc and tests methods
     * provided by NetcdfUWFile class
     */
    public void testBiotemperatureRandomSamples() {
        String filename = pathToExamples + "biotemperature_randomSamples.nc";
        logger.info("Testing " + filename);

        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filename);
            NetcdfUWFile ncUWFile = new NetcdfUWFile(ncfile);
            
            boolean hasPrimary = ncUWFile.hasPrimaryVariables();
            assertTrue(hasPrimary);
            
            Variable primaryVar = ncUWFile.getPrimaryVariable();
            assertEquals("biotemperature", primaryVar.getName());
            logger.info("Primary variable: " + primaryVar.getName());
            
            Dimension dimension = ncUWFile.findRealisationDimension();
            assertEquals(10, dimension.getLength());
            logger.info("Realisation dimension: " + dimension.getName());
        } catch (IOException ioe) {
            logger.error("trying to open " + filename + " " + ioe, ioe);
        } catch (Exception e) {
            logger.error("error while creating netcdfUWFile" + e.getMessage(), e);
        }

    }

    /**
     * reads the example file biotemperature_normalDistr.nc and tests methods
     * provided by NetcdfUWFile class
     */
    public void testBiotemperatureNormalDistr() {
        String filename = pathToExamples + "biotemperature_normalDistr.nc";
        logger.info("Testing " + filename);

        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filename);
            NetcdfUWFile ncUWFile = new NetcdfUWFile(ncfile);
            
            boolean hasPrimary = ncUWFile.hasPrimaryVariables();
            assertTrue(hasPrimary);
            
            Variable primaryVar = ncUWFile.getPrimaryVariable();
            assertEquals("biotemperature", primaryVar.getName());
            logger.info("Primary variable: " + primaryVar.getName());
            Iterator<Variable> ancVarIter = ncUWFile.getAncillaryVariables().iterator();
            while (ancVarIter.hasNext()){
            	logger.info("Ancillary variable: "+ancVarIter.next().getName());
            }
        } catch (IOException ioe) {
            logger.error("trying to open " + filename + " " + ioe, ioe);
        } catch (Exception e) {
            logger.error("error while creating netcdfUWFile" + e.getMessage(), e);
        }

    }
    


    /**
     * reads the example file mahalanobian_stats.nc and tests methods
     * provided by NetcdfUWFile class
     */
    public void testMahalanobianStats() {
        String filename = pathToExamples + "mahalanobian_stats.nc";
        logger.info("Testing " + filename);

        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filename);
            NetcdfUWFile ncUWFile = new NetcdfUWFile(ncfile);
            
            boolean hasPrimary = ncUWFile.hasPrimaryVariables();
            assertTrue(hasPrimary);
            
            Variable primaryVar = ncUWFile.getPrimaryVariable();
            assertEquals("mahalanobian_mean", primaryVar.getName());
            logger.info("Primary variable: " + primaryVar.getName());
        } catch (IOException ioe) {
            logger.error("trying to open " + filename + " " + ioe, ioe);
        } catch (Exception e) {
            logger.error("error while creating netcdfUWFile" + e.getMessage(), e);
        }

    }
    
    /**
     * reads the example file mahalanobian_unknownSamples.nc and tests methods
     * provided by NetcdfUWFile class
     */
    public void testMahalanobianUnknownSamples() {
        String filename = pathToExamples + "mahalanobian_unknownSamples.nc";
        logger.info("Testing " + filename);

        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filename);
            NetcdfUWFile ncUWFile = new NetcdfUWFile(ncfile);
            
            boolean hasPrimary = ncUWFile.hasPrimaryVariables();
            assertTrue(hasPrimary);
            
            Variable primaryVar = ncUWFile.getPrimaryVariable();
            assertEquals("mahalanobian_samples", primaryVar.getName());
            logger.info("Primary variable: " + primaryVar.getName());
            Dimension dimension = ncUWFile.findRealisationDimension();
            assertEquals(10, dimension.getLength());
            logger.info("Realisation dimension: " + dimension.getName());
        } catch (IOException ioe) {
            logger.error("trying to open " + filename + " " + ioe, ioe);
        } catch (Exception e) {
            logger.error("error while creating netcdfUWFile" + e.getMessage(), e);
        }

    }
    
}
