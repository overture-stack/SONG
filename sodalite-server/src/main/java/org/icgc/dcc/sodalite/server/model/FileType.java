package org.icgc.dcc.sodalite.server.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FileType {

    FASTA("FASTA"),
    FAI("FAI"),
    FASTQ("FASTQ"),
    BAM("BAM"),
    BAI("BAI"),
    VCF("VCF"),
    TBI("TBI"),
    IDX("IDX"),
    XML("XML");
	
    private final String value;
    private final static Map<String, FileType> CONSTANTS = new HashMap<String, FileType>();

    static {
        for (FileType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private FileType(String value) {
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
    public static FileType fromValue(String value) {
        FileType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
