----------------------------------------------------------------------------------------------------
-- This SQL script updates an already existing U-SOS database. 
-- author:       Martin Kiesow
-- last changes: 2013-01-31
----------------------------------------------------------------------------------------------------

--------------------------------------------------
-- Add Uncertainty Type: Probability
--------------------------------------------------
DROP TABLE IF EXISTS u_probability CASCADE;

-- table: u_probability
-- represents the probability uncertainty type
-- prob_id maps u_uncertainty uncertainty_values_id
CREATE TABLE u_probability (
  prob_id INTEGER NOT NULL,
  gt NUMERIC,
  lt NUMERIC,
  ge NUMERIC,
  le NUMERIC,
  prob_values  NUMERIC[] NOT NULL,
  PRIMARY KEY (prob_id)
);

-- foreign keys for u_probability table
ALTER TABLE u_probability ADD FOREIGN KEY (prob_id) REFERENCES u_uncertainty (uncertainty_values_id) ON UPDATE CASCADE;

-- check uncertainty types
ALTER TABLE u_uncertainty DROP CONSTRAINT u_uncertainty_uncertainty_type_check;
ALTER TABLE u_uncertainty ADD CHECK (uncertainty_type IN ('norm_dist', 'mean', 'real', 'ran_sam', 'sys_sam', 'unk_sam', 'prob'));
