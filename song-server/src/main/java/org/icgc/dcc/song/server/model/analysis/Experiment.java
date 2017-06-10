package org.icgc.dcc.song.server.model.analysis;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.ToString;
import org.icgc.dcc.song.server.model.Metadata;

@ToString
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "analysisType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SequencingRead.class, name = "sequencingRead"),
        @JsonSubTypes.Type(value = VariantCall.class, name = "variantCall")
})
public abstract class Experiment extends Metadata {}
