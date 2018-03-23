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

import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;

import static java.lang.String.format;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.VARIANT_CALL;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.resolveAnalysisType;
import static org.icgc.dcc.song.server.repository.AttributeNames.ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.STATE;
import static org.icgc.dcc.song.server.repository.AttributeNames.STUDY_ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.TYPE;

public class AnalysisMapper implements ResultSetMapper<Analysis> {

  @Override
  @SneakyThrows
  public Analysis map(int index, ResultSet r, StatementContext ctx) {
    val id = r.getString(ID);
    val study = r.getString(STUDY_ID );
    val type = r.getString(TYPE);
    val state = r.getString(STATE);

    val analysisType = resolveAnalysisType(type);
    if (analysisType == SEQUENCING_READ) {
      return SequencingReadAnalysis.create(id, study, state);
    }else if (analysisType == VARIANT_CALL) {
      return VariantCallAnalysis.create(id, study, state);
    }
    throw new IllegalStateException(
        format("Cannot process the analysisType '%s(%s)'", analysisType.name(), analysisType));
  }


}
