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

package bio.overture.song.server.model.analysis;

import bio.overture.song.server.model.Metadata;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.enums.Constants;
import bio.overture.song.server.model.enums.TableAttributeNames;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.List;

import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.server.model.enums.Constants.SEQUENCING_READ_TYPE;
import static bio.overture.song.server.model.enums.Constants.VARIANT_CALL_TYPE;
import static bio.overture.song.server.model.enums.Constants.validate;

@MappedSuperclass
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
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
public abstract class AbstractAnalysis extends Metadata {

    @Id
    @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
    private String analysisId;

    @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
    private String study;

    @Column(name = TableAttributeNames.STATE, nullable = false)
    private String analysisState = UNPUBLISHED.name();

    @Transient
    private List<CompositeEntity> sample;

    @Transient
    private List<FileEntity> file;

    @Column(name = TableAttributeNames.TYPE, nullable = false)
    abstract public String getAnalysisType();

    public void setAnalysisState(String state) {
        validate(Constants.ANALYSIS_STATE, state);
        this.analysisState=state;
    }

    public void setWith(@NonNull AbstractAnalysis a){
      setAnalysisId(a.getAnalysisId());
      setAnalysisState(a.getAnalysisState());
      setFile(a.getFile());
      setStudy(a.getStudy());
      setSample(a.getSample());
      setInfo(a.getInfo());
    }
}

