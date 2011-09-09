----------------------------------------------------------------------------------------------------
-- Example database request to get observations with uncertainty information
-- author:       Martin Kiesow
-- last changes: 2011-09-07
----------------------------------------------------------------------------------------------------


----------------------------------------------------------------------------------------------------
-- Request without uncertainty information
-- built by
-- org.n52.sos.ds.pgsql.PGSQLGetObservationDAO.queryObservation(SosGetObservationRequest, Connection)
----------------------------------------------------------------------------------------------------

-- select clause
SELECT iso_timestamp(observation.time_stamp) AS time_stamp,
	observation.text_value,
	observation.observation_id,
    observation.numeric_value,
    observation.spatial_value,
    observation.mime_type,
    observation.offering_id,
    observation.procedure_id,
    phenomenon.phenomenon_id,
    phenomenon.phenomenon_description,
    phenomenon.unit,
    phenomenon.valuetype,
    feature_of_interest.feature_of_interest_name,
    feature_of_interest.feature_of_interest_id,
    feature_of_interest.feature_type,
    SRID(feature_of_interest.geom) AS foi_srid,
    SRID(observation.spatial_value) AS value_srid,

    domain_feature.domain_feature_id,
    domain_feature.domain_feature_name,
    domain_feature.feature_type,
    SRID(domain_feature.geom) AS df_srid,

    -- if (quality)
    quality.quality_type,
    quality.quality_name,
    quality.quality_unit,
    quality.quality_value,

-- add geometry column to list
	AsText(observation.spatial_value) AS value_geom,
	AsText(feature_of_interest.geom) AS foi_geom,
	AsText(domain_feature.geom) AS df_geom

-- natural join of tables
FROM (observation NATURAL INNER JOIN phenomenon NATURAL INNER JOIN feature_of_interest
	LEFT OUTER JOIN obs_df ON obs_df.observation_id = observation.observation_id
	LEFT OUTER JOIN domain_feature ON obs_df.domain_feature_id = domain_feature.domain_feature_id

	-- if (quality)
	LEFT JOIN quality ON quality.observation_id = observation.observation_id
	)
WHERE

-- mandatory observedProperty parameters
	(observation.phenomenon_id = 'urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel'
	--	OR observation.phenomenon_id = 'urn:ogc:def:phenomenon:OGC:1.0.30:waterspeed'
	)

-- mandatory offering parameter
	AND (offering_id = 'GAUGE_HEIGHT')
    -- AND (offering_id = 'WATER_SPEED')
    -- AND (offering_id = 'GAUGE_HEIGHT' OR offering_id = 'WATER_SPEED')

-- optional feature of interest parameter
	AND (feature_of_interest.feature_of_interest_id = 'foi_1001'
	OR domain_feature.domain_feature_id = 'foi_1001'
	OR feature_of_interest.feature_of_interest_id = 'foi_2001'
	OR domain_feature.domain_feature_id = 'foi_2001')

-- optional domain feature parameter

-- optional procedures
	AND (observation.procedure_id = 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1'
	OR observation.procedure_id = 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-2'
	OR observation.procedure_id = 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-3')

-- optional temporal filter

-- optional parameter for Result

-- optional spatial filter parameter


----------------------------------------------------------------------------------------------------
-- simple version
----------------------------------------------------------------------------------------------------

SELECT iso_timestamp(observation.time_stamp) AS time_stamp,
	observation.text_value,
	observation.observation_id,
    observation.numeric_value,
    observation.spatial_value,
    observation.mime_type,
    observation.offering_id,
    observation.procedure_id,
    phenomenon.phenomenon_id,
    phenomenon.phenomenon_description,
    phenomenon.unit,
    phenomenon.valuetype,
    feature_of_interest.feature_of_interest_name,
    feature_of_interest.feature_of_interest_id,
    feature_of_interest.feature_type,
    SRID(feature_of_interest.geom) AS foi_srid,
    SRID(observation.spatial_value) AS value_srid,

    domain_feature.domain_feature_id,
    domain_feature.domain_feature_name,
    domain_feature.feature_type,
    SRID(domain_feature.geom) AS df_srid,

    -- if (quality)
    quality.quality_type,
    quality.quality_name,
    quality.quality_unit,
    quality.quality_value,

	AsText(observation.spatial_value) AS value_geom,
	AsText(feature_of_interest.geom) AS foi_geom,
	AsText(domain_feature.geom) AS df_geom

FROM (observation NATURAL INNER JOIN phenomenon NATURAL INNER JOIN feature_of_interest
	LEFT OUTER JOIN obs_df ON obs_df.observation_id = observation.observation_id
	LEFT OUTER JOIN domain_feature ON obs_df.domain_feature_id = domain_feature.domain_feature_id

	-- if (quality)
	LEFT JOIN quality ON quality.observation_id = observation.observation_id
	)
WHERE
	(observation.phenomenon_id = 'urn:ogc:def:phenomenon:OGC:1.0.30:waterlevel')
	AND (offering_id = 'GAUGE_HEIGHT')

-- optional
	AND (feature_of_interest.feature_of_interest_id = 'foi_1001'
	OR domain_feature.domain_feature_id = 'foi_1001')

-- optional
	AND (observation.procedure_id = 'urn:ogc:object:feature:Sensor:IFGI:uw-sensor-1')