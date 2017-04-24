package org.icgc.dcc.sodalite.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Specimen {
  String id;
  String donor_id;
  String submitter_id;
  SpecimenClass class_;
  SpecimenType type;
  
  @JsonCreator
  public Specimen(@JsonProperty("id") String id,
               @JsonProperty("donor_id") String donor_id,
               @JsonProperty("submitter_id") String submitter_id,
               @JsonProperty("class") SpecimenClass class_,
               @JsonProperty("type") SpecimenType type) {
	  this.id = id;
	  this.donor_id=donor_id;
	  this.submitter_id = submitter_id;
	  this.class_ = class_;	  
	  this.type=type;
	  
  }

}

