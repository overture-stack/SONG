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
 *
 */
package org.icgc.dcc.song.server.repository.mapper;

import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.enums.UploadStates;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;
import static org.icgc.dcc.song.server.repository.AttributeNames.CREATED_AT;
import static org.icgc.dcc.song.server.repository.AttributeNames.ERRORS;
import static org.icgc.dcc.song.server.repository.AttributeNames.ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.PAYLOAD;
import static org.icgc.dcc.song.server.repository.AttributeNames.STATE;
import static org.icgc.dcc.song.server.repository.AttributeNames.STUDY_ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.UPDATED_AT;

public class UploadMapper implements ResultSetMapper<Upload> {

  @Override
  public Upload map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
    return Upload.create(rs.getString(ID),
        rs.getString(STUDY_ID),
        getState(rs),
        rs.getString(ERRORS),
        rs.getString(PAYLOAD),
        rs.getTimestamp(CREATED_AT).toLocalDateTime(),
        rs.getTimestamp(UPDATED_AT).toLocalDateTime());
  }

  private static UploadStates getState(ResultSet r) throws SQLException {
    return resolveState(r.getString(STATE));
  }



}
