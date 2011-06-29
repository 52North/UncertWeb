package org.uncertweb.api.netcdf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertml.sample.RandomSample;
import org.uncertml.statistic.Mean;
import org.uncertml.statistic.StandardDeviation;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;

import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * TestCase for testing NetcdfUWFileWritable wrapper; reads uncertain NetCDF example
 * files from folder src/test/resources and tests the methods provided
 * 
 * @author staschc, angelini
 */
public class NetcdfUWFileWritableTest extends TestCase {

    private final Logger logger = LoggerFactory.getLogger(NetcdfUWFileWritableTest.class);

    public void setUp() {
    }

    public void testCreateRandomSamples() throws NetcdfUWException {
        String filename = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString() + ".nc";

        logger.info("Creating random samples dataset: " + filename);


        NetcdfFileWriteable ncFile = NetcdfUWFileWriteable.createNew(filename);
        
        NetcdfUWFileWriteable ncUWFile = new NetcdfUWFileWriteable(ncFile);

        ncUWFile.addSampleVariable("sample", DataType.FLOAT, new ArrayList<Dimension>(), RandomSample.class, 10);

        boolean hasPrimary = ncUWFile.hasPrimaryVariables();
        assertTrue(hasPrimary);

        Variable primaryVar = ncUWFile.getPrimaryVariable();
        assertEquals("sample", primaryVar.getName());
        logger.info("Primary variable: " + primaryVar.getName());

        Dimension dimension = ncUWFile.findRealisationDimension();
        assertEquals(10, dimension.getLength());
        logger.info("Realisation dimension: " + dimension.getName());

    }
    
    public void testCreateStatistics() throws NetcdfUWException {
        String filename = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString() + ".nc";

        logger.info("Creating statistics dataset: " + filename);


        NetcdfFileWriteable ncFile = NetcdfUWFileWriteable.createNew(filename);
        
        NetcdfUWFileWriteable ncUWFile = new NetcdfUWFileWriteable(ncFile);

        ncUWFile.addStatisticVariable("mean", DataType.FLOAT, new ArrayList<Dimension>(), Mean.class);
        ncUWFile.addStatisticVariable("stdDev", DataType.FLOAT, new ArrayList<Dimension>(), StandardDeviation.class);

        boolean hasPrimary = ncUWFile.hasPrimaryVariables();
        assertTrue(hasPrimary);

        Variable primaryVar = ncUWFile.getPrimaryVariable();
        assertEquals("mean", primaryVar.getName());
        logger.info("Primary variable: " + primaryVar.getName());

        List<Variable> statisticsVariables = ncUWFile.findStatisticsVariables();
        assertEquals(2, statisticsVariables.size());
        logger.info("Statistic variables: " + statisticsVariables.get(0).getName() + " " + statisticsVariables.get(1).getName());

    }

}
