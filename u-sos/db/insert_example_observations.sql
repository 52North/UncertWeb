----------------------------------------------------------------------------------------------------
-- SQL script for inserting example observations with uncertainties
-- author:       Martin Kiesow
-- last changes: 2011-10-20
----------------------------------------------------------------------------------------------------

--------------------------------------------------
-- Insert observations
--------------------------------------------------

-- sample phenomenon
INSERT INTO phenomenon VALUES ('urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel', 'gauge height', 'cm','numericType');
INSERT INTO phenomenon VALUES ('urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed', 'water speed', 'm/s','textType');

--sample offering
INSERT INTO offering VALUES ('GAUGE_HEIGHT','The water level in a river');
INSERT INTO offering VALUES ('WATER_SPEED','The waterspeed at a gauge in a river');

-- sample featureofinterest
INSERT INTO feature_of_interest (feature_of_interest_id, feature_of_interest_name, feature_of_interest_description, geom, feature_type, schema_link)
	VALUES ('foi_1001', 'ALBER', 'Albersloh', GeometryFromText('POINT(7.52 52.90)', 4326),'sa:SamplingPoint', 'http://xyz.org/reference-url2.html');
--	VALUES ('foi_1001', 'ALBER', 'Albersloh', GeometryFromText('POINT(7.52 52.90)', 4326),'sams:SF_SpatialSamplingFeature', 'http://xyz.org/reference-url2.html');
--	VALUES ('foi_1001', 'ALBER', 'Albersloh', GeometryFromText('POINT(7.52 52.90)', 4326),'sams:SF_SamplingPoint', 'http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPointl');
INSERT INTO feature_of_interest (feature_of_interest_id, feature_of_interest_name, feature_of_interest_description, geom, feature_type, schema_link)
	VALUES ('foi_2001', 'PADER', 'Paderborn', GeometryFromText('POINT(8.76667 51.7167)', 4326),'sa:SamplingPoint', 'http://xyz.org/reference-url2.html');

-- sample procedure
INSERT INTO procedure VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1', 'standard/uw-sensor-1.xml', 'text/xml;subtype="SensorML/1.0.1"');
INSERT INTO procedure VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2', 'standard/uw-sensor-2.xml', 'text/xml;subtype="SensorML/1.0.1"');
INSERT INTO procedure VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-3', 'standard/uw-sensor-3.xml', 'text/xml;subtype="SensorML/1.0.1"');


---- sample relationships between phenomena, procedures and features of interest

-- sample phen_off relationship
-- INSERT INTO phen_off VALUES ('urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT');
INSERT INTO phen_off VALUES ('urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED');

-- sample foi_off relationship
INSERT INTO foi_off VALUES ('foi_1001','GAUGE_HEIGHT');
INSERT INTO foi_off VALUES ('foi_2001','WATER_SPEED');

-- sample proc_phen relationship
INSERT INTO proc_phen VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel');
INSERT INTO proc_phen VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel');
INSERT INTO proc_phen VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-3','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed');

-- sample proc_foi relationship
INSERT INTO proc_foi VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1','foi_1001');
INSERT INTO proc_foi VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2','foi_1001');
INSERT INTO proc_foi VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-3','foi_2001');

-- sample proc_off relationship
INSERT INTO proc_off VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1','GAUGE_HEIGHT');
INSERT INTO proc_off VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2','GAUGE_HEIGHT');
INSERT INTO proc_off VALUES ('urn:ogc:object:feature:Sensor:IFGI:uw-sensor-3','WATER_SPEED');


---- sample uncertainty values

-- sample value units
INSERT INTO u_value_unit (value_unit_id, value_unit) VALUES (1, 'cm');
INSERT INTO u_value_unit (value_unit_id, value_unit) VALUES (2, 'm/s');

-- sample uncertainty
INSERT INTO u_uncertainty (uncertainty_id, uncertainty_values_id, uncertainty_type, value_unit_id)
	VALUES (1, 1, 'norm_dist',1);
INSERT INTO u_uncertainty (uncertainty_id, uncertainty_values_id, uncertainty_type, value_unit_id)
	VALUES (2, 2, 'norm_dist', 1);
INSERT INTO u_uncertainty (uncertainty_id, uncertainty_values_id, uncertainty_type, value_unit_id)
	VALUES (3, 3, 'norm_dist', 1);
INSERT INTO u_uncertainty (uncertainty_id, uncertainty_values_id, uncertainty_type, value_unit_id)
	VALUES (4, 4, 'mean', 2);
INSERT INTO u_uncertainty (uncertainty_id, uncertainty_values_id, uncertainty_type, value_unit_id)
	VALUES (5, 5, 'mean', 2);

-- sample normal distribution values
INSERT INTO u_normal (normal_id, mean, standardDeviation) VALUES (1, 9.123, 1.23);
INSERT INTO u_normal (normal_id, mean, standardDeviation) VALUES (1, 9.456, 1.45);
INSERT INTO u_normal (normal_id, mean, standardDeviation) VALUES (1, 9.789, 1.67);
INSERT INTO u_normal (normal_id, mean, standardDeviation) VALUES (2, 8.654, 2.34);
INSERT INTO u_normal (normal_id, mean, standardDeviation) VALUES (2, 8.123, 2.89);
INSERT INTO u_normal (normal_id, mean, standardDeviation) VALUES (3, 7.345, 3.33);

-- sample mean values
INSERT INTO u_mean_values (mean_values_id, mean_value) VALUES (1, 10);
INSERT INTO u_mean_values (mean_values_id, mean_value) VALUES (2, 11);
INSERT INTO u_mean_values (mean_values_id, mean_value) VALUES (3, 12);
INSERT INTO u_mean_values (mean_values_id, mean_value) VALUES (4, 9);
INSERT INTO u_mean_values (mean_values_id, mean_value) VALUES (5, 11);

INSERT INTO u_mean (mean_id, mean_values_id) VALUES (4, 1);
INSERT INTO u_mean (mean_id, mean_values_id) VALUES (4, 2);
INSERT INTO u_mean (mean_id, mean_values_id) VALUES (4, 3);
INSERT INTO u_mean (mean_id, mean_values_id) VALUES (4, 4);
INSERT INTO u_mean (mean_id, mean_values_id) VALUES (5, 4);
INSERT INTO u_mean (mean_id, mean_values_id) VALUES (5, 5);


---- sample observation values

-- gauge height values and observations (incl. relationship)
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value)
	VALUES ('2008-04-01 17:44', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT','50.0');
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 1, 'testObs01');

INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value)
	VALUES ('2008-04-01 17:45', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT', '40.2');
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 1, 'testObs02');

INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value)
	VALUES ('2008-04-01 17:46', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT', '70.4');
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 1, 'testObs03');

INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value)
	VALUES ('2008-04-01 17:47', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT', '60.5');
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 2, 'testObs04');

INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value)
	VALUES ('2008-04-01 17:48', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT', '45.456');
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 2, 'testObs05');

INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value)
	VALUES ('2008-04-01 17:49', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1', 'foi_1001','urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel','GAUGE_HEIGHT', '110.1213');
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 3, 'testObs06');


-- water speed values and observations
INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value)
	VALUES ('2008-04-01 17:44', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', '10.1');
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 4, 'testObs07');

INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value)
	VALUES ('2008-04-01 17:45', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', '12.0');
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 4, 'testObs08');

INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value)
	VALUES ('2008-04-01 17:46', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', '10.5');
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 4, 'testObs09');

INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value)
	VALUES ('2008-04-01 17:47', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', '9.2');
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 5, 'testObs10');

INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value)
	VALUES ('2008-04-01 17:51', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', null);
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 5, 'testObs11');

INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,text_value)
	VALUES ('2008-04-01 17:43', 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2', 'foi_2001','urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed','WATER_SPEED', 11.2);
INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier)
	VALUES (currval(pg_get_serial_sequence('observation','observation_id')), 5, 'testObs12');


-- insert links from observation to uncertainty manually
-- (observation_id has to be modified)

-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (11, 1, 'testObs01');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (12, 1, 'testObs02');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (13, 1, 'testObs03');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (14, 2, 'testObs04');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (15, 2, 'testObs05');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (16, 3, 'testObs06');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (17, 4, 'testObs07');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (18, 4, 'testObs08');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (19, 4, 'testObs09');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (20, 5, 'testObs10');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (21, 5, 'testObs11');
-- INSERT INTO obs_unc (observation_id, uncertainty_id, gml_identifier) VALUES (22, 5, 'testObs12');



