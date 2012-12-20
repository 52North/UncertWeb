----------------------------------------------------------------------------------------------------
-- This SQL script updates an already existing U-SOS database. Use it only in combination with
-- databases created/extended by extend_datamodel_postgres83.sql revision 1949. 
-- author:       Martin Kiesow
-- last changes: 2012-11-30
----------------------------------------------------------------------------------------------------

--------------------------------------------------
-- Random sample, systematic sample, unknown sample
--------------------------------------------------

-- alter table: u_realisation
-- optional ID and method description are added
ALTER TABLE u_realisation ADD id VARCHAR(100) DEFAULT 'single';
ALTER TABLE u_realisation ADD sampling_method_description TEXT;

-- id is added to the primary key
ALTER TABLE u_realisation DROP CONSTRAINT u_realisation_pkey;
ALTER TABLE u_realisation ADD PRIMARY KEY (realisation_id, id);