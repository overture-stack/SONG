
package org.icgc.dcc.sodalite.server.model.json.create;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "createEntity"
})
public class CreateEntityMessage {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("createEntity")
    private CreateEntity createEntity;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("createEntity")
    public CreateEntity getCreateEntity() {
        return createEntity;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("createEntity")
    public void setCreateEntity(CreateEntity createEntity) {
        this.createEntity = createEntity;
    }

    public CreateEntityMessage withCreateEntity(CreateEntity createEntity) {
        this.createEntity = createEntity;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public CreateEntityMessage withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
