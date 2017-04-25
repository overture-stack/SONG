
package org.icgc.dcc.sodalite.server.model.json.update.entity;

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
    "entityUpdate"
})
public class EntityUpdateMessage {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entityUpdate")
    private EntityUpdate entityUpdate;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entityUpdate")
    public EntityUpdate getEntityUpdate() {
        return entityUpdate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entityUpdate")
    public void setEntityUpdate(EntityUpdate entityUpdate) {
        this.entityUpdate = entityUpdate;
    }

    public EntityUpdateMessage withEntityUpdate(EntityUpdate entityUpdate) {
        this.entityUpdate = entityUpdate;
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

    public EntityUpdateMessage withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
