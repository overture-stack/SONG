package org.icgc.dcc.sodalite.server.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterMessageValidationTests {

	@Test
	public void validate_submit_sequencing_read_happy_path() throws Exception {
		val errors = validate("schemas/register-sequencingRead-message.json", "documents/register-sequencingread-valid.json");
		assertThat(errors.size()).isEqualTo(0);
	}

	@Test
	public void validate_submit_sequencing_read_missing_required() throws Exception {
		val errors = validate("schemas/register-sequencingRead-message.json", "documents/register-sequencingread-missing-required.json");
		assertThat(errors.size()).isEqualTo(2);
	}
	
	@Test
	public void validate_submit_sequencing_read_invalid_enum() throws Exception {
		val errors = validate("schemas/register-sequencingRead-message.json", "documents/register-sequencingread-invalid-enum.json");
		assertThat(errors.size()).isEqualTo(4);
	}
	
	@Test
	public void validate_submit_variant_call_happy_path() throws Exception {
		val errors = validate("schemas/register-variantCall-message.json", "documents/register-variantcall-valid.json");
		assertThat(errors.size()).isEqualTo(0);
	}

	@Test
	public void validate_submit_variant_call_missing_required() throws Exception {
		val errors = validate("schemas/register-variantCall-message.json", "documents/register-variantcall-missing-required.json");
		assertThat(errors.size()).isEqualTo(3);
	}
	
	@Test
	public void validate_submit_variant_call_invalid_enum() throws Exception {
		val errors = validate("schemas/register-variantCall-message.json", "documents/register-variantcall-invalid-enum.json");
		assertThat(errors.size()).isEqualTo(4);
	}
	
	protected Set<ValidationMessage> validate(String schemaFile, String documentFile) throws Exception {
		JsonSchema schema = getJsonSchemaFromClasspath(schemaFile);
		JsonNode node = getJsonNodeFromClasspath(documentFile);
		val errors = schema.validate(node);
		if (errors.size() > 0) {
			for (val msg : errors) {
				log.error(String.format("Error code %s: %s ", msg.getCode(), msg.getMessage()));
			}
		}
		return errors;
	}
		
  protected JsonSchema getJsonSchemaFromClasspath(String name) throws Exception {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    JsonSchema schema = factory.getSchema(is);
    return schema;
  }
  
  protected JsonNode getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return node;
  }
}
