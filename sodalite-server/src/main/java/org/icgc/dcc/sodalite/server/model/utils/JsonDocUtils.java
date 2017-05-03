package org.icgc.dcc.sodalite.server.model.utils;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.base.Strings;

import lombok.SneakyThrows;
import lombok.val;

public class JsonDocUtils {
  
  @SneakyThrows
  public static JsonNode getJsonNodeFromClasspath(String fileName) {
    ObjectMapper mapper = new ObjectMapper()
        .registerModule(new ParameterNamesModule())
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule());
  	InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
  	JsonNode actualObj = mapper.readTree(is);
  	return actualObj;
  }
  
  @SneakyThrows
  public static JsonNode getNode(String jsonString) {
  	ObjectMapper mapper = new ObjectMapper()
  	    .registerModule(new ParameterNamesModule())
  	    .registerModule(new Jdk8Module())
  	    .registerModule(new JavaTimeModule());
  	return mapper.readTree(jsonString);
  }
  
  public static String getValue(JsonNode node, String key) {
  	val result = node.get(key).textValue();
  	if (Strings.isNullOrEmpty(result)) {
  		throw new IllegalArgumentException("No value found for " + key);
  	}
	  else {
	  	return result;
  	}
  }
}
