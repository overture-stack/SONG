package org.icgc.dcc.sodalite.server.model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
@Builder
@Value
public class Sample {
	String id;
	String specimen_id;
	String submitter_id;
	SampleType type;
	
	 @JsonCreator
	  public Sample(@JsonProperty("id") String id,
	               @JsonProperty("specimen_id") String specimen_id,
	               @JsonProperty("submitter_id") String submitter_id,
	               @JsonProperty("type") SampleType type) {
	    this.id = id;
	    this.specimen_id= specimen_id;
	    this.submitter_id = submitter_id;
	    this.type=type;
	  }

}
