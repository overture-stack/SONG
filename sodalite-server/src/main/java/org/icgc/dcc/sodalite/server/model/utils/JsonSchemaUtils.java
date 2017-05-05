package org.icgc.dcc.sodalite.server.model.utils;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;

import lombok.SneakyThrows;
import lombok.val;

public class JsonSchemaUtils extends JsonDocUtils {
	
	@SneakyThrows
  public static String getSchemaId(JsonNode schemaRoot) {
  	// JSON Schema id field intended to contain a URI
  	val rootNode = schemaRoot.path("id");
  	if (rootNode.isMissingNode()) {
  		throw new IllegalArgumentException("Invalid JSON Schema found: schema missing mandatory id field");
  	} 
  	else {
  		return extractFromSchemaId(rootNode.asText());	
  	}
  }
  
  @SneakyThrows
  public static String extractFromSchemaId(String id) {
  	int separatorPosition = id.lastIndexOf("/");
  	if (separatorPosition < 0) {
  		throw new IllegalArgumentException(String.format("Invalid JSON Schema id found: %s", id));
  	}
  	else {
  		return id.substring(separatorPosition + 1);
  	}
  }
  
  @SneakyThrows
  public static JsonSchema getJsonSchema(JsonNode node) {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    JsonSchema schema = factory.getSchema(node);
    return schema;
  }
  
  @SneakyThrows
  public static JsonSchema getJsonSchemaFromClasspath(String fileName) {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
    JsonSchema schema = factory.getSchema(is);
    return schema;
  }
}
