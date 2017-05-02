/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.sodalite.server.service;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

import org.icgc.dcc.sodalite.server.validation.SchemaValidator;
import org.icgc.dcc.sodalite.server.validation.ValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

@Slf4j
@Service
public class ValidationService {

	@Autowired
	private SchemaValidator validator;
	
  @Autowired
  private StatusService statusService;
  
	protected static final ObjectMapper mapper = new ObjectMapper();
	
  @Async
  public void validate(String schemaId, String studyId, String uploadId, String payload) {
  	val scrubbed = payload.replaceAll("[\\r\\n]+", "");
  	try {
  		statusService.log(studyId, uploadId, scrubbed);
  	}
  	catch(UnableToExecuteStatementException jdbie) {
  		log.error(jdbie.getCause().getMessage());
  		throw new RepositoryException(jdbie.getCause());
  	}
 
  	try {
  		JsonNode jsonNode = mapper.reader().readTree(scrubbed);
  		val response = validator.validate(schemaId, jsonNode);
  		
  		if (response.isValid()) {
  			statusService.updateAsValid(studyId, uploadId);
  		} 
  		else {
  			statusService.updateAsInvalid(studyId, uploadId, response.getValidationErrors());
  		}
  	}
  	catch(JsonProcessingException jpe) {
  		log.error(jpe.getMessage());
  		statusService.updateAsInvalid(studyId, uploadId, String.format("Invalid JSON document submitted: %s", jpe.getMessage()));
  	}
  	catch(Exception e) {
  		log.error(e.getMessage());
  		statusService.updateAsInvalid(studyId, uploadId, String.format("Unknown processing problem: %s", e.getMessage()));
  	}
  }

}
