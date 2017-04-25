package org.icgc.dcc.sodalite.server.model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class File {
	@JsonCreator
	File(@JsonProperty String id, @JsonProperty String sample_id, @JsonProperty String name,
		 @JsonProperty Long size, @JsonProperty String md5sum, @JsonProperty FileType type,
		 @JsonProperty String metadata_doc) {
		this.id=id;
		this.sample_id=sample_id;
		this.name=name;
		this.size=size;
		this.md5sum=md5sum;
		this.type=type;
		this.metadata_doc=metadata_doc;
	}
	String id;
	String sample_id;
	String name;
	Long size;
	String md5sum;
	FileType type;
	String metadata_doc;
}