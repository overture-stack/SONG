package org.icgc.dcc.sodalite.server.config;

import java.util.HashMap;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.utils.JsonDocUtils;
import org.icgc.dcc.sodalite.server.model.utils.JsonSchemaUtils;
import org.icgc.dcc.sodalite.server.validation.SchemaValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Data
public class ValidationConfig {
	
	private static String[] schemaList = {
			"schemas/create-entity-message.json",
			"schemas/create-study-message.json",
			"schemas/register-sequencingread-message.json",
			"schemas/register-variantcall-message.json",
			"schemas/update-entity-message.json",
			"schemas/update-sequencingread-message.json",
			"schemas/update-variantcall-message.json"
	};

	@Bean
	public SchemaValidator schemaValidator() {
		return new SchemaValidator();
	}
	
  @Bean
  @SneakyThrows
  public Map<String, JsonSchema> schemaCache() {
    val cache = new HashMap<String, JsonSchema>();
    // TODO: Arrays.stream(schemaList)
    for (val schema : schemaList) {
    	log.debug("Loading schema {}", schema);
    	JsonNode node = JsonDocUtils.getJsonNodeFromClasspath(schema);
    	cache.put(JsonSchemaUtils.getSchemaId(node), JsonSchemaUtils.getJsonSchema(node));
    }
    for (val s : cache.keySet()) {
    	log.info(s);
    }
    return cache;
  }  
}
