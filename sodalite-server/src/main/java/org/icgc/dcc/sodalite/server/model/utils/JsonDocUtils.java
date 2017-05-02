package org.icgc.dcc.sodalite.server.model.utils;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import lombok.SneakyThrows;
import lombok.val;

public class JsonDocUtils {
  
  @SneakyThrows
  public static JsonNode getJsonNodeFromClasspath(String fileName) {
  	ObjectMapper mapper = new ObjectMapper();
  	InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
  	JsonNode actualObj = mapper.readTree(is);
  	return actualObj;
  }
  
  @SneakyThrows
  public static JsonNode getNode(String jsonString) {
  	ObjectMapper mapper = new ObjectMapper();
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
