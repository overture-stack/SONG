
package org.icgc.dcc.sodalite.server.model.json.create;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "donorSubmitterId",
    "donorGender"
})
public class Donor {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donorSubmitterId")
    private String donorSubmitterId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donorGender")
    private Donor.DonorGender donorGender;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donorSubmitterId")
    public String getDonorSubmitterId() {
        return donorSubmitterId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donorSubmitterId")
    public void setDonorSubmitterId(String donorSubmitterId) {
        this.donorSubmitterId = donorSubmitterId;
    }

    public Donor withDonorSubmitterId(String donorSubmitterId) {
        this.donorSubmitterId = donorSubmitterId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donorGender")
    public Donor.DonorGender getDonorGender() {
        return donorGender;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donorGender")
    public void setDonorGender(Donor.DonorGender donorGender) {
        this.donorGender = donorGender;
    }

    public Donor withDonorGender(Donor.DonorGender donorGender) {
        this.donorGender = donorGender;
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

    public Donor withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    public enum DonorGender {

        MALE("male"),
        FEMALE("female"),
        UNSPECIFIED("unspecified");
        private final String value;
        private final static Map<String, Donor.DonorGender> CONSTANTS = new HashMap<String, Donor.DonorGender>();

        static {
            for (Donor.DonorGender c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private DonorGender(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Donor.DonorGender fromValue(String value) {
            Donor.DonorGender constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
