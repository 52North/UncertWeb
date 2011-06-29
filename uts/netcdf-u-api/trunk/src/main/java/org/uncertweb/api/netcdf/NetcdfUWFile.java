package org.uncertweb.api.netcdf;

import java.util.ArrayList;
import java.util.List;

import org.uncertml.UncertML;
import org.uncertml.sample.Realisation;
import org.uncertweb.api.netcdf.exception.NetcdfUWException;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetcdfUWFile {

    public final static String CONVENTIONS_ATTRIBUTE_KEY = "Conventions";

    public final static String CONVENTIONS_ATTRIBUTE_VALUE = "UW-1.0";

    public final static String REALISATION_DIMENSION_KEY = "realisation";

    public final static String PRIMARY_VARIABLES_KEY = "primary_variables";

    public final static String ANCILLARY_VARIABLES_KEY = "ancillary_variables";

    public final static String REF_ATTRIBUTE_KEY = "ref";

    private NetcdfFile netcdfFile;

    public NetcdfFile getNetcdfFile() {
        return netcdfFile;
    }

    public NetcdfUWFile(NetcdfFile netcdf) throws NetcdfUWException {
        this.netcdfFile = netcdf;
        Attribute conventions = null;
        try {
            conventions = netcdf.findGlobalAttributeIgnoreCase(CONVENTIONS_ATTRIBUTE_KEY);
        } catch (NullPointerException e) {} // Workaround to fix a bug in the API
        
        if (conventions == null) {
            throw new NetcdfUWException("The 'Conventions' global attribute is not present");
        }
        if (!conventions.getStringValue().contains("UW-1.")) {
            throw new NetcdfUWException("The 'Conventions' global attribute does not contain a UW-1.X identifier");
        }
    }

    public boolean hasPrimaryVariables() {
        Attribute primaryVariableAttr = netcdfFile.findGlobalAttributeIgnoreCase(PRIMARY_VARIABLES_KEY);
        if (primaryVariableAttr == null) {
            return false;
        } else {
            return primaryVariableAttr.getValues().getSize() > 0 ? true : false;
        }
    }

    /**
     * @return the first of the primary variables.
     * @throws NetcdfUWException if none is defined.
     */
    public Variable getPrimaryVariable() throws NetcdfUWException {
        try {
            Attribute primaryVariableAttr = netcdfFile.findGlobalAttributeIgnoreCase(PRIMARY_VARIABLES_KEY);
            String primaryVariableName = primaryVariableAttr.getStringValue().split(" ")[0];
            return netcdfFile.findVariable(primaryVariableName);
        } catch (ArrayIndexOutOfBoundsException aioube) {
            throw new NetcdfUWException("Error retrieving the primary variable: the primary variable attribute is defined empty.");
        } catch (NullPointerException e) {
            throw new NetcdfUWException("Error occurred retrieving the primary variable: missing primary variable attribute " + PRIMARY_VARIABLES_KEY);
        }
    }

    /**
     * method returns list containing the ancillary variables of primary
     * variable
     * 
     * @return list containing the ancillary variables of primary variable
     * @throws Exception if extracting the ancillary variables fails
     */
    public List<Variable> getAncillaryVariables() throws NetcdfUWException {
        try {
            List<Variable> ancillaryVariables = null;
            Attribute ancVarAttr = getPrimaryVariable().findAttribute(ANCILLARY_VARIABLES_KEY);
            String[] names = ancVarAttr.getStringValue().split(" ");
            ancillaryVariables = new ArrayList<Variable>(names.length);
            for (String name : names) {
                ancillaryVariables.add(netcdfFile.findVariable(name));
            }
            return ancillaryVariables;
        } catch (NullPointerException e) {
            throw new NetcdfUWException("Error occurred retrieving the ancillary variables:" + e.getMessage());
        }
    }

    /**
     * TODO
     * 
     * @return
     */
    public Variable[] getPrimaryVariables() {
        throw new UnsupportedOperationException();
    }

    /**
     * Finds the dimension containing the realisations
     * 
     * @return
     */
    public Dimension findRealisationDimension() {
        for (Dimension dim : netcdfFile.getDimensions()) {
            Variable var = netcdfFile.findVariable(dim.getName());
            if (var != null) {
                Attribute attr = var.findAttribute(REF_ATTRIBUTE_KEY);
                if (attr != null && attr.getStringValue().equals(UncertML.getURI(Realisation.class))) {
                    return dim;
                }
            }
        }
        return null;
    }
    
    /**
     * Finds the statistics variables
     * @return
     */
    public List<Variable> findStatisticsVariables() {
        return findVariablesWithRefContaining(UncertML.STATISTIC_URI);
    }
  
    private List<Variable> findVariablesWithRefContaining(String content) {
        List<Variable> result = new ArrayList<Variable>();
        for (Variable variable : netcdfFile.getVariables()) {
            Attribute ref = variable.findAttribute(REF_ATTRIBUTE_KEY);
            if (ref != null && ref.getStringValue().contains(content)) {
                result.add(variable);
            }
        }
        return result;
    }
}
