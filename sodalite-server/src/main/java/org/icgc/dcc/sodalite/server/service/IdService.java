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

import org.icgc.dcc.common.core.util.UUID5;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.earnstone.id.Generator;
import com.google.common.base.Joiner;

@Service
public class IdService {

	public static final String DONOR_ID_PREFIX = "DO";
	public static final String SPECIMEN_ID_PREFIX = "SP";
	public static final String SAMPLE_ID_PREFIX = "SA";
	
	/**
	 * Dependencies
	 */
  @Autowired
	private Generator generator;
  
  protected String identifier() {
    long id = generator.nextId();
    return Long.toString(id, 36).toUpperCase();  	
  }

  public String generateDonorId() {
  	return String.format("%s%s", DONOR_ID_PREFIX, identifier());
  }

  public String generateSpecimenId() {
  	return String.format("%s%s", SPECIMEN_ID_PREFIX, identifier());
  }
  
  public String generateSampleId() {
  	return String.format("%s%s", SAMPLE_ID_PREFIX, identifier());
  }

  /**
   * Copied from metadata service:
   * https://github.com/icgc-dcc/dcc-metadata/blob/develop/dcc-metadata-server/src/main/java/org/icgc/dcc/metadata/server/service/EntityService.java#L69
   * 
   * Object id 
   * @param parts
   * @return
   */
  private static String generateObjectId(String... parts) {
    return UUID5.fromUTF8(UUID5.getNamespace(), Joiner.on('/').join(parts)).toString();
  }
}
