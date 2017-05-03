package org.icgc.dcc.sodalite.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Potentially extract a Validator interface if we want to pursue a Strategy pattern of multiple
 * validation rules or something
 *
 */
@Slf4j
public class SchemaValidator {
	
	@Autowired
	private Map<String, JsonSchema> schemaCache;
	
	public String test() {
		return Integer.toString(schemaCache.size());
	}
	
	@SneakyThrows
	public ValidationResponse validate(String schemaId, JsonNode payloadRoot) {
		if (schemaCache.containsKey(schemaId)) {
			val schema = schemaCache.get(schemaId);
			val results = schema.validate(payloadRoot);
			val response = new ValidationResponse(results);
			log.info(response.getValidationErrors());
			
			Thread.sleep(2500);
			
			return response;
		}
		else {
			// log to database
			throw new IllegalArgumentException("Internal Error: could not find specified schema " + schemaId);
		}
	}
}
