-- README!
-- This script is the exact same copy of V1_14__update_function_get_analysis.sql.
-- Reason for re-adding get_analysis function is because after the latest migration, which included all scripts up until V1_15, get_analysis function
-- was removed from DB, possible reason is that in script V1_15__specimen_type_add_enum.sql,
-- the "DROP TYPE _specimen_type CASCADE;" statement drops get_analysis function as well.
-- Please note in the future, when adding new enum values, instead of following V1_15__specimen_type_add_enum.sql,
-- use this query:
-- ALTER TYPE name ADD VALUE [ IF NOT EXISTS ] new_enum_value [ { BEFORE | AFTER } neighbor_enum_value ]
create or replace function get_analysis(studyId character varying, analysisState analysis_state[], pageLimit integer, pageOffset integer)
returns table (
	-- Analysis table
	id character varying, study_id character varying, type analysis_type, state analysis_state,
	analysis_schema_id integer, analysis_data_id integer, created_at timestamp,updated_at timestamp,
	-- File table
	file_id character varying, file_analysis_id character varying, file_study_id character varying, name text,
	size bigint, md5 character, access access_type, file_type text,
	data_type character varying, info json,
	-- Sampleset table
	analysis_id character varying, sampleset_sample_id character varying,
	-- Sample table
	sample_id character varying,
	sample_specimen_id character varying, sample_submitter_id text, legacy_sample_type legacy_sample_type,
	sample_type sample_type, matched_normal_submitter_sample_id character varying,
	-- Specimen table
	specimen_id character varying, specimen_donor_id character varying, submitter_specimen_id text, specimen_class specimen_class,
	legacy_specimen_type legacy_specimen_type, specimen_type specimen_type, tissue_source tissue_source_type, tumour_normal_designation tumour_normal_designation_type,
	-- Donor table
	donor_donor_id character varying, submitter_donor_id text, gender gender,
	donor_study_id character varying,
	-- info table
	donor_info json,sample_info json, specimen_info json
) as
$$
	begin
         -- How this script works: analysis_file_join is the result of joining Analysis table with File table, and getting file info from Info table.
		 -- take the result of analysis_file_join and join Sampleset, Sample, Specimen, and Donor table as well as the info columns.
		 RETURN QUERY
	     SELECT 			   analysis_file_sampleset_join.*,
		 					   sample.id           				AS sample_id,
							   sample.specimen_id  				AS sample_specimen_id,
							   sample.submitter_id 				AS sample_submitter_id,
							   sample.legacy_type  				AS legacy_sample_type,
							   sample.type         				AS sample_type,
							   sample.matched_normal_submitter_sample_id,
							   specimen.id           AS specimen_id,
							   specimen.donor_id     AS specimen_donor_id,
							   specimen.submitter_id AS submitter_specimen_id,
							   specimen.class        AS specimen_class,
							   specimen.legacy_type  AS legacy_specimen_type,
							   specimen.type         AS speciment_type,
							   specimen.tissue_source,
							   specimen.tumour_normal_designation,
							   donor.id                				AS donor_donor_id,
							   donor.submitter_id      				AS submitter_donor_id,
							   donor.gender,
						  	   donor.study_id          				AS donor_study_id,
							   donor_info.info                 		AS donor_info,
						   	   sample_info.info                 	AS sample_info,
							   specimen_info.info               	AS specimen_info

        FROM SAMPLE
        INNER JOIN (SELECT  analysis_file_join.*,
                            sampleset.analysis_id,
                            sampleset.sample_id      			AS sampleset_sample_id
                    FROM sampleset
                    INNER JOIN (
                        SELECT * FROM (
                            SELECT * FROM analysis WHERE analysis.study_id = studyId AND analysis.state = ANY(analysisState)
                            ORDER BY analysis.id ASC
                            LIMIT pageLimit
                            OFFSET pageOffset
                        ) AS filtered_analysis
                        JOIN (   SELECT       filtered_file.id          AS file_id,
                                              filtered_file.analysis_id AS file_analysis_id,
                                              filtered_file.study_id    AS file_study_id,
                                              filtered_file.name,
                                              filtered_file.size,
                                              filtered_file.md5,
                                              filtered_file.access,
                                              filtered_file.type       AS file_type,
                                              filtered_file.data_type,
                                              info.info
                                      FROM (SELECT * FROM file WHERE file.study_id = studyId ) AS filtered_file
                                      JOIN info ON filtered_file.id = info.id AND info.id_type = 'File')
                                      AS file_info
                        ON filtered_analysis.id = file_info.file_analysis_id
                    ) AS analysis_file_join
                    ON analysis_file_join.id = sampleset.analysis_id )
                    AS analysis_file_sampleset_join
                    ON analysis_file_sampleset_join.sampleset_sample_id = sample.id
        LEFT JOIN info sample_info ON analysis_file_sampleset_join.sampleset_sample_id = sample_info.id AND sample_info.id_type = 'Sample'
        INNER JOIN specimen ON specimen.id = sample.specimen_id
        LEFT JOIN info AS specimen_info ON specimen_info.id = specimen.id AND specimen_info.id_type = 'Specimen'
        INNER JOIN donor ON donor.id = specimen.donor_id
        LEFT JOIN info AS donor_info ON  donor_info.id = donor.id AND donor_info.id_type = 'Donor'
        ORDER BY analysis_file_sampleset_join.analysis_id ASC ;

end;
$$
language plpgsql;
