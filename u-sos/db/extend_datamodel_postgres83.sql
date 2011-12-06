----------------------------------------------------------------------------------------------------
-- SQL script for extending the datamodel to accept observations with uncertainties
-- author:       Martin Kiesow
-- last changes: 2011-10-31
----------------------------------------------------------------------------------------------------

--------------------------------------------------
-- Drop tables
--------------------------------------------------
DROP TABLE IF EXISTS obs_unc CASCADE;
DROP TABLE IF EXISTS u_uncertainty CASCADE;
DROP TABLE IF EXISTS u_value_unit CASCADE;
DROP TABLE IF EXISTS u_normal CASCADE;
DROP TABLE IF EXISTS u_mean CASCADE;
DROP TABLE IF EXISTS u_mean_values CASCADE;

--------------------------------------------------
-- Create tables
--------------------------------------------------
-- table: obs_unc
-- represents an n:m relation between observation and uncertainty
CREATE TABLE obs_unc (
  observation_id INTEGER NOT NULL,
  uncertainty_id INTEGER NOT NULL,
  gml_identifier VARCHAR(100) NOT NULL,
  PRIMARY KEY (observation_id, uncertainty_id)
);

-- table: u_uncertainty
-- represents a super type of the different uncertainty types
CREATE TABLE u_uncertainty (
  uncertainty_id SERIAL NOT NULL,
  uncertainty_values_id SERIAL NOT NULL,
  uncertainty_type VARCHAR (100) NOT NULL,
  value_unit_id INTEGER NOT NULL,
  PRIMARY KEY (uncertainty_id),
  UNIQUE (uncertainty_values_id)
);

-- table: u_value_unit
-- represents the uncertainty's value unit
CREATE TABLE u_value_unit (
  value_unit_id SERIAL NOT NULL,
  value_unit TEXT UNIQUE NOT NULL,
  PRIMARY KEY (value_unit_id)
);

-- table: u_normal
-- represents the normal distribution uncertainty type
-- normal_id maps u_uncertainty uncertainty_values_id
CREATE TABLE u_normal (
  normal_id INTEGER NOT NULL,
  mean NUMERIC NOT NULL,
  var NUMERIC NOT NULL,
  PRIMARY KEY (normal_id, mean, var)
);

-- table: u_mean
-- represents the mean uncertainty type
-- mean_id maps u_uncertainty uncertainty_values_id
CREATE TABLE u_mean (
  mean_id INTEGER NOT NULL,
  mean_values_id INTEGER NOT NULL,
  PRIMARY KEY (mean_id, mean_values_id)
);

-- table: u_mean_values
-- represents a list of mean values
CREATE TABLE u_mean_values (
  mean_values_id SERIAL NOT NULL,
  mean_value NUMERIC NOT NULL,
  PRIMARY KEY (mean_values_id),
  UNIQUE (mean_value)
);


--------------------------------------------------
-- Add indices
--------------------------------------------------


--------------------------------------------------
-- Add references and foreign keys
--------------------------------------------------
-- foreign keys for obs_unc table
ALTER TABLE obs_unc ADD FOREIGN KEY (observation_id) REFERENCES observation ON UPDATE CASCADE;
ALTER TABLE obs_unc ADD FOREIGN KEY (uncertainty_id) REFERENCES u_uncertainty ON UPDATE CASCADE;

-- foreign keys for u_uncertainty table
ALTER TABLE u_uncertainty ADD FOREIGN KEY (value_unit_id) REFERENCES u_value_unit ON UPDATE CASCADE;

-- foreign keys for u_normal table
ALTER TABLE u_normal ADD FOREIGN KEY (normal_id) REFERENCES u_uncertainty (uncertainty_values_id) ON UPDATE CASCADE;

-- foreign keys for u_mean and u_mean_values table
ALTER TABLE u_mean ADD FOREIGN KEY (mean_id) REFERENCES u_uncertainty (uncertainty_values_id) ON UPDATE CASCADE;
ALTER TABLE u_mean ADD FOREIGN KEY (mean_values_id) REFERENCES u_mean_values (mean_values_id) ON UPDATE CASCADE;

--------------------------------------------------
-- Add and alter table constraints
--------------------------------------------------
-- add 'uncertaintyType' to checked phenomenons
-- original check constraint will be replaced
ALTER TABLE phenomenon DROP CONSTRAINT phenomenon_valuetype_check;
ALTER TABLE phenomenon ADD CHECK (valuetype IN ('uncertaintyType', 'booleanType', 'countType', 'textType', 'categoryType', 'numericType', 'isoTimeType', 'spatialType', 'commonType','externalReferenceType'));
