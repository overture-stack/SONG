/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.icgc.dcc.song.server.model.analysis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.icgc.dcc.song.server.model.enums.Constants;

import java.util.List;

import static org.icgc.dcc.song.server.model.enums.AnalysisStates.UNPUBLISHED;
import static org.icgc.dcc.song.server.model.enums.Constants.SEQUENCING_READ_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.VARIANT_CALL_TYPE;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonTypeInfo(
        use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property="analysisType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value=SequencingReadAnalysis.class, name=SEQUENCING_READ_TYPE),
        @JsonSubTypes.Type(value=VariantCallAnalysis.class, name=VARIANT_CALL_TYPE)
})
public abstract class Analysis extends Metadata {

    private String analysisId="";
    private String study="";
    private String analysisState = UNPUBLISHED.name();

    private List<CompositeEntity> sample;
    private List<File> file;

    abstract public String getAnalysisType();

    public void setAnalysisState(String state) {
        Constants.validate(Constants.ANALYSIS_STATE, state);
        this.analysisState=state;
    }

}

