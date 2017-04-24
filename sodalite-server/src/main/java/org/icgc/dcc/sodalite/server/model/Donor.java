package org.icgc.dcc.sodalite.server.model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
@Builder
@Value
public class Donor {
	String id;
	String study_id;
	String submitter_id;
	Gender gender;
	
	 @JsonCreator
	  public Donor(@JsonProperty("id") String id,
	               @JsonProperty("study_id") String study_id,
	               @JsonProperty("submitter_id") String submitter_id,
	               @JsonProperty("gender") Gender gender) {
	    this.id = id;
	    this.study_id= study_id;
	    this.submitter_id = submitter_id;
	    this.gender=gender;
	  }
};

