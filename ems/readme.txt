######### EMS readme.txt #########

#### Package structure ####
org.uncertweb.ems
- EMSalgorithm: contains the algorithm of the WPS and manages the inputs, model runs and outputs

org.uncertweb.ems.data.exposure
-ExposureValue: Data structure for the exposure value

org.uncertweb.ems.data.profile
- Data structures for the human activity data

org.uncertweb.ems.exceptions
- Exception types for EMS inputs and processes

org.uncertweb.ems.exposuremodel
- Main model for the overlay procedure. Uses the org.n52.wps.util.r.process classes for the RServe connection.

org.uncertweb.ems.extension
- These packages include the indoor model extension for the model, parameters and profiles for the EMS which is only partially implemented.

org.uncertweb.ems.io
- OMProfileParser: Reads OM inputs and creates Geometry Profiles for the exposure model. It also performs the matching of the general time in the human activity data to the specific times in the NetCDF-U file if necessary.
- OMProfileGenerator: Creates OM Documents from the exposure profiles for the service output

org.uncertweb.ems.util
- Different classes for Mappings, Constants and tests.

#### File output ###
The results of the EMS are written as XML files to the Apache Tomcat /public folder (EMSalgorithm.java, line 225-231).
The service also writes the OM.csv file from the OM2CSVencoder to the temp directory of the OS. As the name is always the same (OM.xml) it will be overwritten by each model run.

#### R dependencies ####
The Outdoor Model requires RServe and a local R Script file (overlay_utils.R, located in \src\main\resources\org\uncertweb\ems\exposuremodel)
This file needs to be copied to a local directory and referenced in the wps_config.xml Resources property (Currently C:\\WebResources\\)

