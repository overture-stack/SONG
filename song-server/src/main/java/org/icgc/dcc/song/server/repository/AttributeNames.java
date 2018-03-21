package org.icgc.dcc.song.server.repository;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class AttributeNames {
    public static final String ID="id";
    public static final String STUDY_ID="study_id";
    public static final String TYPE="type";
    public static final String STATE="state";
    public static final String ACCESS="access";
    public static final String INFO="info";
    public static final String ID_TYPE="id_type";

    public static final String SUBMITTER_ID="submitter_id";
    public static final String GENDER="gender";

    public static final String MD5="md5";
    public static final String SIZE="size";
    public static final String NAME="name";

    public static final String SPECIMEN_ID="specimen_id";

    public static final String ALIGNED="aligned";
    public static final String ALIGNMENT_TOOL="alignment_tool";
    public static final String INSERT_SIZE="insert_size";
    public static final String LIBRARY_STRATEGY="library_strategy";
    public static final String PAIRED_END="paired_end";
    public static final String REFERENCE_GENOME="reference_genome";

    public static final String DONOR_ID="donor_id";
    public static final String CLASS="class";

    public static final String ANALYSIS_ID="analysis_id";

    public static final String ORGANIZATION="organization";
    public static final String DESCRIPTION="description";

    public static final String ERRORS="errors";
    public static final String PAYLOAD="payload";
    public static final String CREATED_AT="created_at";
    public static final String UPDATED_AT="updated_at";

    public static final String VARIANT_CALLING_TOOL="variant_calling_tool";
    public static final String MATCHED_NORMAL_SAMPLE_SUBMITTER_ID="matched_normal_sample_submitter_id";
}
