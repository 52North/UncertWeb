package org.uncertweb.api.netcdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uncertml.IUncertainty;
import org.uncertml.UncertML;
import org.uncertml.distribution.IDistribution;
import org.uncertml.sample.ISample;
import org.uncertml.sample.Realisation;
import org.uncertml.statistic.IStatistic;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;

import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

public class NetcdfUWFileWriteable extends NetcdfUWFile {

    private NetcdfFileWriteable netcdfFileWritable;

    public NetcdfFileWriteable getNetcdfFileWritable() {
        return netcdfFileWritable;
    }

    public NetcdfUWFileWriteable(NetcdfFileWriteable netcdfWritable) throws NetcdfUWException {
        super(netcdfWritable);
        this.netcdfFileWritable = netcdfWritable;
    }

    public static void addUWToConventions(NetcdfFileWriteable netcdfWritable) throws NetcdfUWException {
        try {
            addValueToAttribute(CONVENTIONS_ATTRIBUTE_VALUE, CONVENTIONS_ATTRIBUTE_KEY, null, netcdfWritable);
        } catch (IOException ioe) {
            throw new NetcdfUWException("Error occured while setting Conventions global variable", ioe);
        }
    }

    public void addVariableToAncillary(Variable toBeAdded, Variable addTo) throws NetcdfUWException {
        try {
            boolean initialMode = netcdfFileWritable.isDefineMode();
            if (!initialMode) {
                netcdfFileWritable.setRedefineMode(true);
            }
            Attribute ancillary = addTo.findAttributeIgnoreCase(ANCILLARY_VARIABLES_KEY);
            if (ancillary == null) {

            }

            netcdfFileWritable.setRedefineMode(initialMode);
        } catch (IOException ioe) {
            throw new NetcdfUWException("Error occured while setting variable " + toBeAdded.getName() + " as ancillary of "
                    + addTo.getName(), ioe);
        }
    }

    public static NetcdfFileWriteable createNew(String location) throws NetcdfUWException {
        return NetcdfUWFileWriteable.createNew(location, true);
    }

    public static NetcdfFileWriteable createNew(String location, boolean fill) throws NetcdfUWException {

        try {
            NetcdfFileWriteable ncFile;
            ncFile = NetcdfFileWriteable.createNew(location, fill);
            addUWToConventions(ncFile);
            ncFile.create();
            return ncFile;
        } catch (IOException ioe) {
            throw new NetcdfUWException("Error occurred while creating file " + location, ioe);
        }
    }

    public void setPrimaryVariable(Variable variable) throws NetcdfUWException {
        try {
            boolean initialMode = netcdfFileWritable.isDefineMode();
            if (!initialMode) {
                netcdfFileWritable.setRedefineMode(true);
            }

            if (netcdfFileWritable.findGlobalAttribute(PRIMARY_VARIABLES_KEY) != null) {
                netcdfFileWritable.deleteGlobalAttribute(PRIMARY_VARIABLES_KEY);
            }
            netcdfFileWritable.addGlobalAttribute(PRIMARY_VARIABLES_KEY, variable.getName());
            netcdfFileWritable.setRedefineMode(initialMode);
        } catch (IOException ioe) {
            throw new NetcdfUWException("Error occurred while setting primary variable " + variable.getName(), ioe);
        }
    }

    /**
     * Gotcha: if another statistics is defined as primary, this method adds this one as ancillary.
     * 
     * @param varName
     * @param dataType
     * @param dims
     * @param statisticClass
     * @return
     * @throws NetcdfUWException
     */
    public Variable addStatisticVariable(String varName, DataType dataType, List<Dimension> dims, Class<? extends IStatistic> statisticClass) throws NetcdfUWException{
        Variable result;
        try {

            // v = addUncertainVariable(varName, dataType, dims);
            result = addUncertainVariable(varName, dataType, dims, statisticClass);

            if(hasPrimaryVariables()) {
                // ifdef primary: getPrimary, check isStatistic, add this to ancillary
                addValueToAttribute(varName, ANCILLARY_VARIABLES_KEY, getPrimaryVariable().getName(), netcdfFileWritable);
            } else {
                // ifndef primary: add v to primary
                setPrimaryVariable(result);
            }

        } catch (IOException ioe) {
            throw new NetcdfUWException("Error occurred while adding sample variable " + varName, ioe);
        }
        
        return result;
    }

    public Variable addStatisticVariable(Variable sourceVariable, Class<? extends IStatistic> statisticClass) throws NetcdfUWException {
        return addStatisticVariable(sourceVariable.getName(), sourceVariable.getDataType(), sourceVariable.getDimensions(), statisticClass);
    }

    public Variable addDistributionVariable(String varName, DataType dataType, List<Dimension> dims,
            Class<? extends IDistribution> distributionClass) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Variable addDistributionVariable(Variable sourceVariable, Class<? extends IDistribution> distributionClass) {
        return addDistributionVariable(sourceVariable.getName(), sourceVariable.getDataType(), sourceVariable.getDimensions(),
                distributionClass);
    }

     public Variable addParameterVariable(String varName, DataType dataType,
     List<Dimension> dims, IDistribution.IParameter parameter) {
     // ifndef primary: exception
     // getPrimary, check isDistribution (from URI)
     // check parameter is consistent with distribution (from URI)
     // check dims is consistent with distribution shape
     // v = addUncertainVariable(varName, dataType, dims);
     // getPrimary, add this to its ancillary
     return null;
     }
     
    /**
     * Add a sample variable to the file. The new dimension is inserted as first
     * (slowest position).
     * 
     * @param varName name of Variable, must be unique with the file.
     * @param dataType type of underlying element
     * @param dims list of Dimensions for the variable, must already have been
     *        added. Use a list of length 0 for a scalar variable.
     * @param sampleClass the Java class describing the sample uncertainty type
     * @param realisationSize the size of the new realisation dimension
     * @return the Variable that has been added
     * @throws Exception
     */
    public Variable addSampleVariable(String varName, DataType dataType, List<Dimension> dims, Class<? extends ISample> sampleClass,
            int realisationSize) throws NetcdfUWException {

        Variable result;
        try {

            // ifdef primary: exception
            if (hasPrimaryVariables()) {
                throw new IllegalStateException("Another Primary variable has been already declared");
            }

            // d = addDimension(REALISATION_DIMENSION_KEY, realisationSize)
            Dimension realisationDimension = addUncertainDimension(REALISATION_DIMENSION_KEY, realisationSize);
            List<Dimension> dimList = new ArrayList<Dimension>(dims.size() + 1);
            dimList.add(realisationDimension);
            


            // v = addUncertainVariable(varName, dataType, dims + d);
            dimList.addAll(dims);
            result = addUncertainVariable(varName, dataType, dimList, sampleClass);

            // add to primary
            setPrimaryVariable(result);

        } catch (IOException ioe) {
            throw new NetcdfUWException("Error occurred while adding sample variable " + varName, ioe);
        } catch (InvalidRangeException ire) {
            throw new NetcdfUWException("Error occurred while adding sample variable " + varName, ire);
        }

        return result;
    }

    /**
     * Add a sample variable to the file, copying name, dataType and dimensions
     * from the sourceVariable.
     * 
     * @param varName name of Variable, must be unique with the file.
     * @param dataType type of underlying element
     * @param dims list of Dimensions for the variable, must already have been
     *        added. Use a list of length 0 for a scalar variable.
     * @param sampleClass the Java class describing the sample uncertainty type
     * @param realisationSize the size of the new realisation dimension
     * @return the Variable that has been added
     * @throws NetcdfUWException
     */
    public Variable addSampleVariable(Variable sourceVariable, Class<? extends ISample> sampleClass, int realisationSize)
            throws NetcdfUWException {
        return addSampleVariable(sourceVariable.getName(), sourceVariable.getDataType(), sourceVariable.getDimensions(), sampleClass,
                realisationSize);
    }

    private Dimension addUncertainDimension(String realisationDimensionName, int realisationSize) throws IOException, InvalidRangeException {
        boolean initialMode = netcdfFileWritable.isDefineMode();
        if (!initialMode) {
            netcdfFileWritable.setRedefineMode(true);
        }

        Dimension result = netcdfFileWritable.addDimension(realisationDimensionName, realisationSize);
        List<Dimension> dimList = new ArrayList<Dimension>();
        dimList.add(result);

        // add dimension variable
        Variable realisationVariable = addUncertainVariable(REALISATION_DIMENSION_KEY, DataType.INT, dimList, Realisation.class);

        // fill dimension variable
        ArrayInt array = new ArrayInt(new int[] { realisationSize });
        for (int i = 0; i < realisationSize; i++) {
            array.setInt(i, i + 1);
        }
        
        netcdfFileWritable.setRedefineMode(false);
        netcdfFileWritable.write(realisationVariable.getName(), array);
        
        netcdfFileWritable.setRedefineMode(initialMode);
        return result;
    }

    private Variable addUncertainVariable(String varName, DataType dataType, List<Dimension> dims, Class<? extends IUncertainty> uncertainty)
            throws IOException {
        boolean initialMode = netcdfFileWritable.isDefineMode();
        if (!initialMode) {
            netcdfFileWritable.setRedefineMode(true);
        }

        Variable result = netcdfFileWritable.addVariable(varName, dataType, dims);
        result.addAttribute(new Attribute("ref", UncertML.getURI(uncertainty)));

        netcdfFileWritable.setRedefineMode(initialMode);
        return result;
    }

    private static void addValueToAttribute(String newValue, String attributeName, String variableName, NetcdfFileWriteable ncFileW) throws IOException {
        boolean initialMode = ncFileW.isDefineMode();
        if (!initialMode) {
            ncFileW.setRedefineMode(true);
        }
        Attribute attribute = null;
        Variable variable = null;
        boolean isAttributeGlobal = variableName == null;
        try {
            if (isAttributeGlobal) {
                attribute = ncFileW.findGlobalAttribute(attributeName);
            } else {
                variable = ncFileW.findVariable(variableName);
                attribute = variable.findAttribute(attributeName);
            }
        } catch (NullPointerException e) {
        }

        String oldValue = "";
        if (attribute != null) {
            oldValue = attribute.getStringValue() + " ";
            if (isAttributeGlobal) {
                ncFileW.deleteGlobalAttribute(attributeName);
            } else {
                ncFileW.deleteVariableAttribute(variableName, attributeName);
            }
        }
        if (isAttributeGlobal) {
            ncFileW.addGlobalAttribute(attributeName, oldValue + newValue);
        } else {
            ncFileW.addVariableAttribute(variableName,attributeName, oldValue + newValue);
        }
        ncFileW.setRedefineMode(initialMode);
    }
}
