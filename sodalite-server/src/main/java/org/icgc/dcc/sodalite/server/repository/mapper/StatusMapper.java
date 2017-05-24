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
package org.icgc.dcc.sodalite.server.repository.mapper;

import org.icgc.dcc.sodalite.server.model.SubmissionStatus;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StatusMapper implements ResultSetMapper<SubmissionStatus> {

  public SubmissionStatus map(int index, ResultSet rs, StatementContext ctx) throws SQLException { // I prefer braces on
                                                                                                   // next line when
                                                                                                   // declaring
                                                                                                   // exception throws
                                                                                                   // in method
                                                                                                   // signature - Dušan
    SubmissionStatus status = new SubmissionStatus();
    status.withUploadId(rs.getString("id"))
        .withStudyId(rs.getString("study_id"))
        .withState(rs.getString("state"))
        .withPayload(rs.getString("payload"))
        .withCreatedAt(rs.getTimestamp("created_at").toLocalDateTime())
        .withUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

    String errorString = rs.getString("errors");
    if (errorString == null) {
      errorString = "";
    }
    String[] errors = errorString.split("\\|");
    for (val e : errors) {
      status.withError(e);
    }
    return status;
  }

}
